package org.mtcg;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

public class MyPrintWriter extends PrintWriter {
    public MyPrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    @Override
    public void println(String s) {
        super.println(s);
        System.out.print("\n"+ s +"\n");
    }
}
