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
package com.xceptance.debug;

import java.io.File;
import java.io.IOException;

import com.xceptance.common.util.zip.ZipUtils;

public class Zip
{

    public static void main(String[] args)
    {
        if (args.length >= 2)
        {
            try
            {
                ZipUtils.zipDirectory(new File(args[1]), new File(args[0]));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("Error: Need two arguments");
            System.err.println("       Zip <zip-file> <directory>");
        }
    }

}
