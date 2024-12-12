package org.mtcg.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.Card;
import org.mtcg.Model.Package;
import org.mtcg.Model.User;
import org.mtcg.db.DbAccess;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class POST_Handler {

    DbAccess dba;

    public POST_Handler(DbAccess dba){
        this.dba = dba;
    }

    public void call_request(StringBuilder info, StringBuilder content, PrintWriter writer){

        //System.out.println("calling request");

        String response_to_client = "";

        int start = info.indexOf("/") + 1;  // Start just after the first '/'
        int end = info.indexOf(" ", start);  // Find the first space after the '/'

        // Extract the substring
        String method = info.substring(start, end); // /users,/deck,...

        //System.out.println("method: " + method);
        //System.out.println("content: " + content);

        String[] requestParts = method.split("[/?]"); // for requests like "transactions/packages" and "deck?format=plain"

        if(requestParts.length == 1){
            switch (requestParts[0]){
                case "users":
                    this.users(content, writer);
                    break;


                case "sessions":
                    this.sessions(content, writer);
                    break;


                case "packages":
                    this.packages(content, writer);
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
    }

    public void users(StringBuilder content, PrintWriter writer){

        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance
        User user = null; // Initialize player variable

        try {
            // Convert StringBuilder to String and deserialize into Player object
            user = objectMapper.readValue(content.toString(), User.class);
        } catch (IOException e) {
            writer.println("HTTP/1.1 400\r\nContent-Type: text/plain\r\nError parsing player data");
            return;
        }

        dba.POST_users(user, writer);
    }

    public void sessions(StringBuilder content, PrintWriter writer){
        ObjectMapper objectMapper = new ObjectMapper();
        User user = null; // Initialize player variable

        try {
            // Convert StringBuilder to String and deserialize into Player object
            user = objectMapper.readValue(content.toString(), User.class);
        } catch (IOException e) {
            writer.println("HTTP/1.1 400\r\nContent-Type: text/plain\r\nError parsing session data");
            return;
        }
        dba.POST_sessions(user, writer);
    }

    public void packages(StringBuilder content, PrintWriter writer){
        ObjectMapper objectMapper = new ObjectMapper();
        List<Card> cards;

        // I also need to create Element and type based in the name of the card !!!!!!!

        try {
            // Convert StringBuilder to String and deserialize into a list of Card objects
            cards = objectMapper.readValue(content.toString(), new TypeReference<List<Card>>() {});
        } catch (IOException e) {
            writer.println("HTTP/1.1 400\r\nContent-Type: text/plain\r\nError parsing card data");
            return;
        }

        Package p;

        try {
            // Convert StringBuilder to String and deserialize into Player object
            p = objectMapper.readValue(content.toString(), Package.class);
        } catch (IOException e) {
            writer.println("HTTP/1.1 400\r\nContent-Type: text/plain\r\nError parsing package data");
            return;
        }



        dba.POST_packages(p, writer);
    }

}
