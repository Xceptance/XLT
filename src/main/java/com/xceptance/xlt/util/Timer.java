/*
 * Copyright (c) 2005-2025 Xceptance Software Technologies GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.xceptance.xlt.util;

public class Timer
{
    private final long ms;
    
    private long stoppedMs; 
                    
    private boolean stopped = false;
    
    private Timer()
    {
        ms = System.currentTimeMillis();
    }
    
    public static Timer start()
    {
        return new Timer();
    }
    
    public Timer stop()
    {
        stoppedMs = System.currentTimeMillis();
        
        if (stopped)
        {
            throw new IllegalArgumentException("Stopped called twice");
        }
        
        stopped = true;
        
        return this;
    }
    
    public long runtimeMillis()
    {
        return stoppedMs - ms;
    }
    
    public String get(final String msg)
    {
        return String.format(msg + " - %,d ms", runtimeMillis());
    }
}
