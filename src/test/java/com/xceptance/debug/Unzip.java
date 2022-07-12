package com.xceptance.debug;

import java.io.File;
import java.io.IOException;

import com.xceptance.common.util.zip.ZipUtils;

public class Unzip
{

    public static void main(String[] args)
    {
        if (args.length >= 2)
        {
            try
            {
                ZipUtils.unzipFile(new File(args[0]), new File(args[1]));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            System.err.println("Error: Need two arguments");
            System.err.println("       Unzip <zip-file> <directory>");
        }
    }

}
