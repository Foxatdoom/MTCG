package org.mtcg.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;

import java.util.Map;
import java.util.Objects;

public class PUT_Handler {

    DbAccess dba;

    public PUT_Handler(DbAccess dba){
        this.dba = dba;
    }

    public void call_request(String auth, String[] path_parts, StringBuilder info, StringBuilder content, MyPrintWriter writer){
        
        switch (path_parts[1]){
            case "deck":
                this.deck(auth, content, writer);
                break;

            case "users":
                this.users(auth, path_parts[2], content, writer);
                break;

            default:
                writer.println(400, "unknown request");
        }
    }

    private void deck(String auth, StringBuilder content, MyPrintWriter writer){

        // change content to usable list of card_ids
        String cleanedContent = content.toString().replaceAll("[\"\\[\\]]", "");
        String[] card_list = cleanedContent.split("\\s*,\\s*");// Split by comma

        if(card_list.length != 4) {
            writer.println(400, "Bad request");
            return;
        }

        String uid = dba.GET_uid_from_auth(auth, writer);
        dba.PUT_deck(uid, card_list, writer);
    }

    private void users(String auth, String what_user, StringBuilder content, MyPrintWriter writer) {
        if(!Objects.equals(auth, what_user += "-mtcgToken")) writer.println(403, "Unauthorized");
        else {
            String uid = dba.GET_uid_from_auth(auth, writer);

            // Extract values from content
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> data = null;
            try {
                data = mapper.readValue(content.toString(), Map.class);
            } catch (JsonProcessingException e) {
                writer.println(400, e.getMessage());
            }

            // get variables
            String name = data.get("Name");
            String bio = data.get("Bio");
            String image = data.get("Image");

            dba.PUT_users(uid, name, bio, image, writer);
        }
    }
}
