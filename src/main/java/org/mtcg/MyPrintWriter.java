package org.mtcg;

import java.io.OutputStream;
import java.io.PrintWriter;

public class MyPrintWriter extends PrintWriter {
    public MyPrintWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public void println(int code, String s) {
        String msg = "HTTP/1.1 " + code + "\r\nContent-Type: text/plain\r\n\r\n" + "{\"message\": \"" + s + "\"}";
        super.println(msg);
        System.out.print("\n"+ msg +"\n");
    }
}
