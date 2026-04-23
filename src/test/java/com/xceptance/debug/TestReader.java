package com.xceptance.debug;

import java.io.ByteArrayInputStream;
import com.xceptance.common.io.ByteBufferedLineReader;

public class TestReader {
    public static void main(String[] args) throws Exception {
        byte[] data = "Hello\nWorld\r\nTest\n\nEmpty\r\n".getBytes();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteBufferedLineReader reader = new ByteBufferedLineReader(bais, 10);
        byte[] lineBuffer = new byte[100];
        int length;
        int count = 0;
        while ((length = reader.readInto(lineBuffer, 0)) != -1) {
            count++;
            System.out.println("Line " + count + ": " + length + " bytes, " + new String(lineBuffer, 0, length));
        }
        System.out.println("Done.");
    }
}
