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
