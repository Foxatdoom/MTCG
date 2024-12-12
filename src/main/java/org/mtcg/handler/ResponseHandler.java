package org.mtcg.handler;

import java.io.PrintWriter;

public class ResponseHandler {

    PrintWriter writer;

    public ResponseHandler(PrintWriter writer){
        this.writer = writer;
    }

    public void respond(String message){



        System.out.println("response_handler: " + message);

        // should be a JSON with jackson and ObjectMapper (maybe there should be A Response Class like User to create
        // a jackson of it !!!!!!!!!!!!!!!
        writer.println(message); // Anser to the Client
    }


}
