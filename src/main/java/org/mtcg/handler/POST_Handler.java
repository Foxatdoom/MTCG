package org.mtcg.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.User;
import org.mtcg.db.DbAccess;

import java.io.IOException;

public class POST_Handler {

    DbAccess dba;

    public POST_Handler(DbAccess dba){
        this.dba = dba;
    }

    public String call_request(StringBuilder info, StringBuilder content){

        //System.out.println("calling request");

        String response_to_client = "";

        int start = info.indexOf("/") + 1;  // Start just after the first '/'
        int end = info.indexOf(" ", start);  // Find the first space after the '/'

        // Extract the substring
        String method = info.substring(start, end); // /users,/deck,...

        System.out.println("method: " + method);
        System.out.println("content: " + content);

        String[] requestParts = method.split("[/?]"); // for requests like "transactions/packages" and "deck?format=plain"

        if(requestParts.length == 1){
            switch (requestParts[0]){
                case "users":
                    response_to_client = this.users(content);
                    break;


                case "sessions":
                    break;


                case "packages":
                    break;


                case "transactions":
                    break;


                case "battles":
                    break;


                case "tradings":
                    break;
            }
        }
        else {
            // special requests like "transactions/packages" and "deck?format=plain"
        }

        return response_to_client;
    }

    public String users(StringBuilder content){

        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance
        User user = null; // Initialize player variable

        try {
            // Convert StringBuilder to String and deserialize into Player object
            user = objectMapper.readValue(content.toString(), User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return "HTTP/1.0400 - Error parsing player data";
        }

        return dba.POST_users(user);
    }

}
