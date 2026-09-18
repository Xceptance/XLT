# Virtual Threads (vThreads) Architectural Review & Root Cause Analysis

## Executive Summary

XLT supports virtual threads (Project Loom, Java 21+) to model virtual users with high concurrency and low memory overhead compared to OS platform threads. However, under load testing with virtual threads, two major failure modes have been observed:

1. **An agent occasionally does not start** (threads abort immediately or remain stuck in `Waiting` state).
2. **An agent occasionally stops doing its work** (permits cease being issued mid-test, leaving users permanently waiting).
3. **Thread dumps are not helpful** (`jstack` and standard JVM dumps show idle carrier threads and omit virtual threads).

This document details the root causes identified across the agent runtime—with primary focus on [AbstractExecutionTimer.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/AbstractExecutionTimer.java)—and outlines the concrete remediation steps required for production stability.

---

## Symptom to Root Cause Matrix

| Symptom | Primary Root Causes | Impacted Components |
| :--- | :--- | :--- |
| **Agent does not start** | • **Premature Auto-Stop Countdown**: Timer scheduled in constructor ticks before runners launch; `stopped = true` immediately aborts all vThreads on arrival.<br>• **Static Timer Leak**: Factory retains stopped timers from prior runs.<br>• **Startup Carrier Pool Starvation**: Mass monitor contention on `synchronized (this)` pins all carrier threads.<br>• **Immediate Timer Crash**: Background timer thread dies on first second due to unsynchronized `HashMap` access during concurrent registration. | • [AbstractExecutionTimer.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/AbstractExecutionTimer.java)<br>• [ExecutionTimerFactory.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/ExecutionTimerFactory.java)<br>• [SessionImpl.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/engine/SessionImpl.java) |
| **Agent stops doing work** | • **Silent `java.util.Timer` Death**: Unsynchronized read of `sessions.keySet().size()` throws `ConcurrentModificationException`, crashing the timer thread; no new permits are ever released.<br>• **Fair Semaphore Head-of-Line Blocking**: Lead vThread unparked by `Semaphore(0, true)` cannot obtain a carrier thread; entire FIFO queue blocks.<br>• **Carrier Pinning in Subsystems**: `Object.wait()` in `SynchronizingCounter` and JVM-wide singleton lock on `Thread.currentThread().getThreadGroup()`. | • [AbstractExecutionTimer.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/AbstractExecutionTimer.java)<br>• [PeriodicExecutionTimer.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/PeriodicExecutionTimer.java)<br>• [SynchronizingCounter.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/common/util/SynchronizingCounter.java)<br>• [PageStatistics.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/engine/PageStatistics.java)<br>• [TestContext.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/engine/scripting/TestContext.java) |
| **Thread dumps not helpful** | • Standard tools (`jstack`, `kill -3`, VisualVM, `ThreadMXBean`) only capture OS platform threads (the carrier pool). Virtual threads are omitted by default.<br>• Pinned or parked vThreads leave carrier threads idle in `ForkJoinPool.runWorker()`, hiding the true blockage. | JVM tooling & runtime defaults |

---

## Detailed Root Cause Analysis

### 1. The Auto-Stop Lifecycle Countdown Bug

**Location**: [AbstractExecutionTimer.java#L68-L81](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/AbstractExecutionTimer.java#L68-L81)

```java
protected AbstractExecutionTimer(final String userTypeName, final long initialDelay, final long duration, final int shutdownPeriod)
{
    waitingThreads = new HashSet<Thread>();
    sessions = new HashMap<>();

    if (duration > 0)
    {
        // start the auto-stop timer
        final Timer timer = new Timer("AbstractExecutionTimer-" + userTypeName, true);

        if (shutdownPeriod > 0)
        {
            timer.schedule(new AutoStopTimerTask(), initialDelay + duration);
        }

        timer.schedule(new AutoStopRemainingTimerTask(), initialDelay + duration + shutdownPeriod);
    }
}
```

#### The Problem
1. **Constructor-Time Delay Calculation**: `timer.schedule(task, delay)` starts counting down relative to `System.currentTimeMillis()` at the exact millisecond the timer object is instantiated inside `new LoadTest(...)`.
2. **Startup Lag**: In [AgentMain.java](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/AgentMain.java#L159-L250), substantial work occurs *after* `LoadTest` creation: RMI round-trips for master controller time synchronization (`getReferenceTimeDifference()`), watcher thread startup, statistics generator initialization, custom sampler setup, and class verification.
3. **Premature Termination**: If `duration` is short, or if initialization/classloading takes a few seconds, `AutoStopTimerTask` fires before virtual user threads reach `waitForNextExecution()`.
   - `AutoStopTimerTask` calls `stopWaitingThreads()`, setting `stopped = true`.
   - When virtual user threads finally start and call `waitForNextExecution()`:
     ```java
     synchronized (this)
     {
         if (stopped)
         {
             throw new InterruptedException("User quits voluntarily as the measurement period is over");
         }
         ...
     }
     ```
   - **Result**: Every vThread immediately throws `InterruptedException` and exits on its very first iteration without running any transactions. The agent status reports `Finished` with 0 iterations (**"Agent does not start"**).
4. **Un-cancellable Timer**: The `timer` instance is held only in a local variable. It is not stored in a field and cannot be cancelled when `stop()` or `LoadTest.abort()` is called.

---

### 2. Unsynchronized Collections & Silent Timer Thread Death

**Location**: [AbstractExecutionTimer.java#L43-L50, L97-L100, L115-L120](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/AbstractExecutionTimer.java#L43-L50) and [PeriodicExecutionTimer.java#L193](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/PeriodicExecutionTimer.java#L193)

```java
// AbstractExecutionTimer.java
private final Collection<Thread> waitingThreads = new HashSet<Thread>();
private final Map<Thread, SessionImpl> sessions = new HashMap<>();

public Collection<Thread> getThreads()
{
    return sessions.keySet();
}

private void registerCurrentThread()
{
    final Thread t = Thread.currentThread();
    waitingThreads.add(t);
    sessions.putIfAbsent(t, SessionImpl.getCurrent());
}
```

```java
// PeriodicExecutionTimer.java (ArrivalRateControllerTimerTask.run)
final int maxPermits = Math.max(releases, timer.getThreads().size());
```

#### The Problem
1. **Unsynchronized Read vs. Concurrent Write**: `getThreads()` is completely unsynchronized and returns the `keySet()` view of a non-thread-safe `java.util.HashMap`.
2. Every second, the background `PeriodicExecutionTimer` platform thread invokes `timer.getThreads().size()`.
3. Concurrently, dozens or hundreds of virtual threads call `registerCurrentThread()` (which writes via `sessions.putIfAbsent(...)`).
4. In `java.util.HashMap`, concurrent read/write during table resizing or bucket insertion causes:
   - Reading inconsistent `size` (e.g. 0), preventing permits from being released.
   - `ConcurrentModificationException` or `NullPointerException`.
5. **Silent Timer Thread Death**: Under `java.util.Timer`, if any `TimerTask.run()` throws an unchecked exception, **the `TimerThread` terminates silently**. The exception is not caught and the thread exits.
6. **Result**:
   - Once the timer thread dies, `ArrivalRateControllerTimerTask` never runs again.
   - `semaphore.release()` is never called again.
   - All virtual user threads remain permanently blocked on `timerTask.semaphore.acquire()`.
   - The agent suddenly ceases all progress mid-test (**"Agent stops doing its work"**).
   - If this occurs on second 0 or 1 during initial burst registration, the agent never issues any permits at all (**"Agent does not start"**).

---

### 3. Monitor Contention (`synchronized`) & Carrier Thread Pinning

**Location**: [AbstractExecutionTimer.java#L147-L179](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/AbstractExecutionTimer.java#L147-L179)

```java
public final void waitForNextExecution() throws InterruptedException
{
    synchronized (this)
    {
        if (stopped)
        {
            throw new InterruptedException(...);
        }
        else
        {
            registerCurrentThread();
        }
    }

    try
    {
        executeWait();
    }
    finally
    {
        synchronized (this)
        {
            unregisterCurrentThread();
            if (stopped)
            {
                throw new InterruptedException(...);
            }
        }
    }
}
```

#### The Problem
1. **Carrier Thread Pinning on Monitor Contention**: Under JDK 21, virtual threads contending on an object monitor (`synchronized`) **pin the carrier thread**. A virtual thread cannot unmount while waiting to enter a `synchronized` block or method.
2. **Carrier Pool Sizing**: The carrier thread pool (`ForkJoinPool`) defaults to `Runtime.getRuntime().availableProcessors()` (typically 4–16 OS threads).
3. **Burst Contention**: In [LoadTest.run()](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/LoadTest.java#L135), all test runner vThreads start nearly simultaneously. Each vThread immediately contends on `synchronized (this)` of the shared `AbstractExecutionTimer` instance.
4. **Cascade**:
   - 1 vThread holds `this`.
   - All remaining carrier threads pick up virtual threads that immediately block trying to enter `synchronized (this)`.
   - Every carrier thread becomes blocked/pinned on the monitor.
   - No carrier threads remain available to execute other virtual threads, initialize classes, or process unparked workers.
   - Inside `registerCurrentThread()`, [SessionImpl.getCurrent()](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/engine/SessionImpl.java#L107) executes, which in turn acquires `synchronized (holder)` and instantiates session infrastructure, further compounding lock holding times while carrier threads are starved.

---

### 4. Fair Semaphore Head-of-Line Blocking

**Location**: [PeriodicExecutionTimer.java#L106](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/PeriodicExecutionTimer.java#L106) and [RandomExecutionTimer.java#L209](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/RandomExecutionTimer.java#L209)

```java
private final Semaphore semaphore = new Semaphore(0, true); // fair semaphore
```

#### The Problem
1. **Strict FIFO Ordering**: Fair semaphores guarantee strict first-in, first-out acquisition via AQS.
2. When `semaphore.release()` is called, AQS unparks the thread at the head of the wait queue.
3. If that unparked virtual thread cannot be dispatched by the `ForkJoinPool` because all carrier threads are pinned or saturated by other blocked virtual threads, **the lead thread cannot execute to claim its permit**.
4. Because the semaphore is fair, **no subsequent thread in the queue can bypass the head**, even if permits are available.
5. The entire user pool stalls waiting on the head of the FIFO queue.

---

### 5. Running Threads Are Never Interrupted on Teardown

**Location**: [AbstractExecutionTimer.java#L212-L228](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/AbstractExecutionTimer.java#L212-L228)

```java
synchronized void stopThread(final Thread thread)
{
    if (thread.isAlive())
    {
        if (waitingThreads.contains(thread))
        {
            // the thread is currently waiting for its next turn -> can be interrupted safely
            thread.interrupt();
        }
        else
        {
            // the thread is currently executing a test case -> just mark its session as expired
            final SessionImpl sessionImpl = sessions.get(thread);
            sessionImpl.markAsExpired();
        }
    }
}
```

#### The Problem
1. When `stop()` is called (either after the shutdown period by `AutoStopRemainingTimerTask` or during `LoadTest.abort()`), it iterates over all known threads.
2. If a thread is currently executing a test case (`waitingThreads.contains(thread) == false`), the timer **only calls `sessionImpl.markAsExpired()`**. It **never calls `thread.interrupt()`**.
3. If that virtual thread is blocked in network I/O, pinned on an internal lock, or waiting in `SynchronizingCounter.awaitZero()`, it never reaches the `session.wasMarkedAsExpired()` check.
4. In [LoadTest.java#L158](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/LoadTest.java#L158), `waitForCompletion()` calls `runner.join()`, which calls `thread.join()`.
5. Because the stuck virtual thread is never interrupted, `runner.join()` hangs indefinitely until the hard process timeout safety net triggers `System.exit()`.

---

### 6. Static Timer Reuse in `ExecutionTimerFactory`

**Location**: [ExecutionTimerFactory.java#L39, L59-L92](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/ExecutionTimerFactory.java#L39)

```java
private static final Map<String, AbstractExecutionTimer> timers = new HashMap<String, AbstractExecutionTimer>();
```

#### The Problem
1. `ExecutionTimerFactory.timers` is a static map that is never cleared.
2. In embedded executions, test environments, or non-forking test runners where the JVM is reused across test runs, calling `createTimer()` for a previously used `userTypeName` returns the existing `AbstractExecutionTimer` instance.
3. The returned timer already has `stopped = true`.
4. Any new runner threads started for that user type immediately encounter `if (stopped) throw new InterruptedException(...)` in `waitForNextExecution()`.
5. The test finishes instantly with 0 executions.

---

### 7. Compounding Carrier-Pinning Multipliers in Subsystems

While `AbstractExecutionTimer` controls the scheduling gate, two major subsystem patterns in XLT cause severe carrier pinning that starves virtual threads trying to reach or exit the timer:

#### A. `Object.wait()` in `SynchronizingCounter.java`
- In [SynchronizingCounter.java#L75-L101](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/common/util/SynchronizingCounter.java#L75-L101), `awaitZero(timeout)` synchronizes on `this` and calls `wait(timeout)`.
- In [RequestQueue.java#L164](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/engine/RequestQueue.java#L164), `ongoingRequestsCount.awaitZero(WAIT_TIMEOUT)` waits up to **5 minutes** for static resource downloads.
- In Java 21, **`Object.wait()` inside a virtual thread pins the carrier thread for the entire duration of the wait**. If static resource loading stalls or is slow, carrier threads are completely locked up, leaving zero carrier threads to run other virtual threads.

#### B. JVM-Wide Singleton Lock on `ThreadGroup`
- In [PageStatistics.java#L48-L61](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/engine/PageStatistics.java#L48-L61) and [TestContext.java#L150-L165](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/engine/scripting/TestContext.java#L150-L165):
  ```java
  final ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
  synchronized (threadGroup)
  {
      stats = pageStatistics.get(threadGroup);
      ...
  }
  ```
- In Java 21, **all virtual threads belong to a single JVM-wide singleton ThreadGroup named `"VirtualThreads"`**.
- Every virtual user thread in the entire JVM locks on the **exact same monitor instance**.
- This creates massive JVM-wide monitor contention, pins carrier threads, and causes all virtual users to share a single `PageStatistics` and `TestContext` instance.

#### C. `SessionImpl.class` Lock During File I/O
- In [SessionImpl.java#L490](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/engine/SessionImpl.java#L490):
  ```java
  synchronized (SessionImpl.class)
  {
      Files.createDirectories(resultDir);
  }
  ```
- All virtual user threads concurrently synchronize on `SessionImpl.class` to create directories. File I/O inside a `synchronized` block pins carrier threads during disk operations at test startup.

---

## Why Thread Dumps Are Not Helpful

When developers take a standard thread dump (`jstack <pid>`, `kill -3`, VisualVM, or JConsole):

1. **Virtual Threads Are Omitted**: Standard JVM thread dump utilities only inspect operating system platform threads. Virtual threads are intentionally omitted because there may be hundreds of thousands of them.
2. **Carrier Threads Appear Idle**: Carrier threads in the `ForkJoinPool` appear in state `WAITING (parking)` inside `ForkJoinPool.runWorker()` waiting for tasks. They look completely healthy and idle.
3. **Dead Timers Are Invisible**: If the background `Timer` thread died from an unhandled `ConcurrentModificationException`, it is simply missing from the dump with no indication of why.
4. **How to Capture Virtual Threads**:
   - Use `jcmd <pid> Thread.dump_to_file -format=text <filename>` or `-format=json`.
   - Run the agent JVM with `-Djdk.tracePinnedThreads=full` to log a stack trace to stdout whenever a virtual thread pins its carrier thread.

---

## Remediation Plan

### Phase 1: Harden `AbstractExecutionTimer`

1. **Thread-Safe Collections**:
   - Replace `HashSet<Thread>` with `Set<Thread> waitingThreads = ConcurrentHashMap.newKeySet()`.
   - Replace `HashMap<Thread, SessionImpl>` with `ConcurrentMap<Thread, SessionImpl> sessions = new ConcurrentHashMap<>()`.
   - In `getThreads()`, return an unmodifiable view or provide an explicit `getThreadCount()` method returning `sessions.size()` without exposing raw collection views.

2. **Replace Monitor Synchronization with `ReentrantLock`**:
   - Replace `synchronized (this)` with `java.util.concurrent.locks.ReentrantLock`.
   - Virtual threads parked on `ReentrantLock.lock()` unmount gracefully without pinning their carrier threads.

3. **Decouple Auto-Stop Timer from Constructor**:
   - Do not schedule timers in `AbstractExecutionTimer`'s constructor.
   - Introduce an explicit `start()` method called by [LoadTest.run()](file:///home/rschwietzke/projects/GIT/XLT-latest/src/main/java/com/xceptance/xlt/agent/LoadTest.java#L132) when test runner threads actually begin execution.
   - Store the timer instance in a private field and cancel it explicitly in `stop()`.

4. **Catch and Log Exceptions in Background Timer Tasks**:
   - Wrap the `run()` methods of `ArrivalRateControllerTimerTask`, `UserCountControllerTimerTask`, `AutoStopTimerTask`, and `AutoStopRemainingTimerTask` in `try { ... } catch (Throwable t) { log.error("...", t); }` so unexpected exceptions do not silently kill the timer thread.
   - Consider migrating from `java.util.Timer` to `java.util.concurrent.ScheduledExecutorService`.

5. **Hard Interrupt on Final Stop**:
   - In `stop()`, if threads remain alive after `shutdownPeriod`, issue `thread.interrupt()` to break hung I/O or waiting states.

### Phase 2: Fix `ExecutionTimerFactory` Lifecycle

- Add a `public static synchronized void reset()` or `clear()` method to `ExecutionTimerFactory` to clear `timers` between test runs.
- Invoke `ExecutionTimerFactory.reset()` during `LoadTest` initialization and teardown.

### Phase 3: Eliminate Subsystem Carrier Pinning

1. **`SynchronizingCounter.java`**:
   - Replace `synchronized`/`wait()`/`notifyAll()` with `ReentrantLock` and `Condition`, or an AQS-based `CountDownLatch`/`Phaser`.
2. **`PageStatistics` & `TestContext`**:
   - Eliminate `Thread.currentThread().getThreadGroup()` locking.
   - Store `PageStatistics` and `TestContext` directly on `SessionImpl` (which is already bound to the user thread via `sessionHolder`).
3. **Session Directory Creation**:
   - Replace `synchronized (SessionImpl.class)` around `Files.createDirectories` with a concurrent directory cache or individual directory locks.
