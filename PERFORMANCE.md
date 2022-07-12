# TODO
* Fast String test and benchmark

## 8 Cores - AMD - Only Real Cores
20191024-143543

### Original
8 cores
42,704,293 records read - 64,555 ms - 661,500 lines/s
8 + 8 cores
42,704,293 records read - 61,270 ms

### Optimized
All - 8+8 cores
42,704,293 records read - 44,426 ms - 961,246 lines/s
All - 8 cores
42,704,293 records read - 50,342 ms - 848,284 lines/s
All - 4 cores
42,704,293 records read - 67,916 ms - 628,781 lines/s

No merge rules
42,704,293 records read - 27,680 ms - 1,542,785 lines/s

No merge rules, no statistics processor
42,704,293 records read - 14,283 ms - 2,989,869 lines/s
42,704,293 records read - 14,035 ms - 3,042,700 lines/s

No merge rules, no statistics processor, no RequestData.setUrl
42,704,293 records read - 9,328 ms - 4,578,076 lines/s

No merge rules, no statistics processor, RequestData.setUrl - no hashCodeOfUrlWithoutFragment
42,704,293 records read - 12,421 ms - 3,438,072 lines/s

No merge rules, no statistics processor, RequestData.setUrl - no UrlUtils.retrieveHostFromUrl
42,704,293 records read - 9,914 ms - 4,307,474 lines/s


### No request data url handling
RequestData.setUrl

## Fails
* Changing `AbstractDataProcessorBasedReportProvider` to use FastHashMap, seems to save on cpu, but the overall runtimes will be longer

## Tuned RequestData to stay with XltCharBuffer instead of copying it
Branch: 02-requestdata-xltcharbuffer

No merge rules, no statistics processor, no code here (only xltcharbuffer stays)
42,704,293 records read - 9,543 ms - 4,474,934 lines/s
42,704,293 records read - 9,297 ms - 4,593,341 lines/s

No merge rules, no statistics processor
42,704,293 records read - 12,187 ms - 3,504,086 lines/s
42,704,293 records read - 11,774 ms - 3,627,000 lines/s
42,704,293 records read - 12,154 ms - 3,513,600 lines/s

No merge rules
42,704,293 records read - 26,865 ms - 1,589,588 lines/s
42,704,293 records read - 26,537 ms - 1,609,236 lines/s

## Single chunk queue take instead of draining the queue
Branch: 03-single-chunks and 02!

No merge rules, no statistics processor
42,704,293 records read - 10,617 ms - 4,022,256 lines/s

No merge rules
42,704,293 records read - 22,592 ms - 1,890,240 lines/s

## Chunksize 1000
No merge rules, no statistics processor
42,704,293 records read - 10,098 ms - 4,228,985 lines/s

No merge rules


## thread reader, parser, no thread statistics
### No statistics
1/1
42,704,293 records read - 32,641 ms - 1,308,302 lines/s
42,704,293 records read - 34,665 ms - 1,231,914 lines/s

2/1
42,704,293 records read - 19,089 ms - 2,237,115 lines/s

4/4
42,704,293 records read - 11,624 ms - 3,673,804 lines/s

8/4
42,704,293 records read - 9,291 ms - 4,596,308 lines/s

8/6
42,704,293 records read - 9,865 ms - 4,328,869 lines/s

8/8
42,704,293 records read - 9,857 ms - 4,332,382 lines/s


## stats proc, 20191024-143543
8 cores, 8 reader, 1 parser, 1000 chunk, 100 queue

EMPTY
42,704,293 records read - 34,035 ms - 1,254,717 lines/s

ALL
42,704,293 records read - 68,121 ms - 626,889 lines/s

com.xceptance.xlt.report.providers.GeneralReportProvider
42,704,293 records read - 38,510 ms - 1,108,914 lines/s

com.xceptance.xlt.report.providers.TransactionsReportProvider
42,704,293 records read - 35,351 ms - 1,208,008 lines/s

com.xceptance.xlt.report.providers.ActionsReportProvider
42,704,293 records read - 36,966 ms - 1,155,232 lines/s

com.xceptance.xlt.report.providers.RequestsReportProvider
42,704,293 records read - 37,523 ms - 1,138,083 lines/s

com.xceptance.xlt.report.providers.CustomTimersReportProvider
42,704,293 records read - 35,628 ms - 1,198,616 lines/s

com.xceptance.xlt.report.providers.ErrorsReportProvider
42,704,293 records read - 36,652 ms - 1,165,129 lines/s

com.xceptance.xlt.report.providers.ResponseCodesReportProvider
42,704,293 records read - 37,076 ms - 1,151,804 lines/s

com.xceptance.xlt.report.providers.ConfigurationReportProvider
42,704,293 records read - 34,626 ms - 1,233,301 lines/s

com.xceptance.xlt.report.providers.AgentsReportProvider
42,704,293 records read - 36,219 ms - 1,179,058 lines/s

com.xceptance.xlt.report.providers.TestReportConfigurationReportProvider
42,704,293 records read - 35,439 ms - 1,205,008 lines/s

com.xceptance.xlt.report.providers.EventsReportProvider
42,704,293 records read - 36,061 ms - 1,184,224 lines/s

com.xceptance.xlt.report.providers.CustomValuesReportProvider
42,704,293 records read - 37,624 ms - 1,135,028 lines/s

com.xceptance.xlt.report.providers.ContentTypesReportProvider
42,704,293 records read - 40,441 ms - 1,055,965 lines/s

com.xceptance.xlt.report.providers.HostsReportProvider
42,704,293 records read - 36,576 ms - 1,167,550 lines/s

com.xceptance.xlt.report.providers.SummaryReportProvider
42,704,293 records read - 39,646 ms - 1,077,140 lines/s

com.xceptance.xlt.report.providers.PageLoadTimingsReportProvider
42,704,293 records read - 35,372 ms - 1,207,291 lines/s


# StatisticsProcessor Tuning
No Merge Rules, 20191024-143543
42,704,293 records read - 23,149 ms - 1,844,758 lines/s
42,704,293 records read - 22,747 ms - 1,877,359 lines/s

Seriell StatProc, sync
42,704,293 records read - 53,528 ms - 797,794 lines/s

Sync per ReportProvider per line
42,704,293 records read - 26,388 ms - 1,618,322 lines/s

Sync per ReportProvider per line, Providers randomly shuffled
42,704,293 records read - 27,508 ms - 1,552,432 lines/s

Sync per ReportProvider per full loop, Providers randomly shuffled
42,704,293 records read - 22,370 ms - 1,908,998 lines/s

Softlock per provider with queue what still to do, do next when none free
42,704,293 records read - 21,599 ms - 1,977,142 lines/s

Let the provider run the loop, queued soft lock with next entry
when currently locked (1)
42,704,293 records read - 22,245 ms - 1,919,725 lines/s
42,704,293 records read - 22,001 ms - 1,941,016 lines/s

Like (1) but Thread.yield when not free provider
42,704,293 records read - 20,470 ms - 2,086,189 lines/s
42,704,293 records read - 20,148 ms - 2,119,530 lines/s

# Stats with or without own threads
Classic Stats Threads, one thread per provider, no merge rules
==============================================================
42,704,293 records read - 22,493 ms - 1,898,559 lines/s

        208.645,30 msec task-clock                #    5,963 CPUs utilized          
           311.478      context-switches          #    0,001 M/sec                  
   655.155.732.719      cycles                    #    3,140 GHz                      (39,94%)
 1.048.974.283.241      instructions              #    1,60  insn per cycle         
                                                  #    0,34  stalled cycles per insn  (39,92%)
    34.006.183.611      stalled-cycles-frontend   #    5,19% frontend cycles idle     (39,95%)
   359.375.603.152      stalled-cycles-backend    #   54,85% backend cycles idle      (39,99%)
    21.364.376.353      cache-references          #  102,396 M/sec                    (40,08%)
     5.243.155.767      cache-misses              #   24,542 % of all cache refs      (40,10%)
   429.223.425.440      L1-dcache-loads           # 2057,192 M/sec                    (40,02%)
    12.120.208.619      L1-dcache-load-misses     #    2,82% of all L1-dcache hits    (40,02%)
   175.579.638.114      branches                  #  841,522 M/sec                    (40,01%)
     1.263.227.622      branch-misses             #    0,72% of all branches          (39,97%)

      34,989482464 seconds time elapsed

     201,708117000 seconds user
       7,774115000 seconds sys
       
And merge rules
===============================================================
42,704,293 records read - 38,501 ms - 1,109,174 lines/s
42,704,293 records read - 37,887 ms - 1,127,149 lines/s

        319.260,64 msec task-clock                #    6,221 CPUs utilized          
           377.797      context-switches          #    0,001 M/sec                  
   931.307.551.056      cycles                    #    2,917 GHz                      (39,99%)
 1.903.752.005.049      instructions              #    2,04  insn per cycle         
                                                  #    0,27  stalled cycles per insn  (40,11%)
    22.577.128.323      stalled-cycles-frontend   #    2,42% frontend cycles idle     (40,12%)
   513.323.807.086      stalled-cycles-backend    #   55,12% backend cycles idle      (40,17%)
    33.668.521.254      cache-references          #  105,458 M/sec                    (40,11%)
     5.782.807.052      cache-misses              #   17,176 % of all cache refs      (40,02%)
   822.641.114.835      L1-dcache-loads           # 2576,707 M/sec                    (39,90%)
    16.182.954.800      L1-dcache-load-misses     #    1,97% of all L1-dcache hits    (39,81%)
   314.412.853.543      branches                  #  984,816 M/sec                    (39,88%)
     1.731.027.333      branch-misses             #    0,55% of all branches          (39,88%)

      51,320441270 seconds time elapsed

     310,978187000 seconds user
       9,173575000 seconds sys

====================================================================
Parser also do stats, compareAndSet, no merge rules
====================================================================
42,704,293 records read - 19,550 ms - 2,184,363 lines/s
42,704,293 records read - 19,826 ms - 2,153,954 lines/s

        209.072,20 msec task-clock                #    6,891 CPUs utilized          
         2.366.407      context-switches          #    0,011 M/sec                  
   613.723.706.129      cycles                    #    2,935 GHz                      (40,01%)
 1.038.206.245.850      instructions              #    1,69  insn per cycle         
                                                  #    0,33  stalled cycles per insn  (40,03%)
    16.875.031.545      stalled-cycles-frontend   #    2,75% frontend cycles idle     (40,05%)
   344.368.348.471      stalled-cycles-backend    #   56,11% backend cycles idle      (39,99%)
    22.559.633.453      cache-references          #  107,904 M/sec                    (39,98%)
     4.482.027.074      cache-misses              #   19,867 % of all cache refs      (39,97%)
   421.800.336.362      L1-dcache-loads           # 2017,487 M/sec                    (39,98%)
    12.254.947.184      L1-dcache-load-misses     #    2,91% of all L1-dcache hits    (40,01%)
   178.378.821.606      branches                  #  853,192 M/sec                    (39,99%)
     1.140.354.696      branch-misses             #    0,64% of all branches          (39,99%)

      30,340918665 seconds time elapsed

     195,839065000 seconds user
      13,398707000 seconds sys


And merge rules
===============================================================
42,704,293 records read - 34,226 ms - 1,247,715 lines/s
42,704,293 records read - 34,896 ms - 1,223,759 lines/s

        338.892,33 msec task-clock                #    7,234 CPUs utilized          
           863.142      context-switches          #    0,003 M/sec                  
   964.289.322.387      cycles                    #    2,845 GHz                      (40,04%)
 1.990.676.418.157      instructions              #    2,06  insn per cycle         
                                                  #    0,25  stalled cycles per insn  (39,97%)
    27.599.499.726      stalled-cycles-frontend   #    2,86% frontend cycles idle     (39,97%)
   495.291.851.087      stalled-cycles-backend    #   51,36% backend cycles idle      (39,97%)
    35.762.798.614      cache-references          #  105,528 M/sec                    (39,98%)
     5.948.041.635      cache-misses              #   16,632 % of all cache refs      (39,98%)
   876.810.720.077      L1-dcache-loads           # 2587,284 M/sec                    (39,98%)
    16.619.540.226      L1-dcache-load-misses     #    1,90% of all L1-dcache hits    (40,03%)
   328.323.457.148      branches                  #  968,813 M/sec                    (40,03%)
     1.986.173.957      branch-misses             #    0,60% of all branches          (40,04%)

      46,848039457 seconds time elapsed

     325,002494000 seconds user
      14,084453000 seconds sys


