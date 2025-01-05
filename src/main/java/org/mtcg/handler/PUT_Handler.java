package org.mtcg.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.Card;
import org.mtcg.Model.Package;
import org.mtcg.Model.User;
import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class PUT_Handler {

    DbAccess dba;

    public PUT_Handler(DbAccess dba){
        this.dba = dba;
    }

    public void call_request(String auth, String[] path_parts, StringBuilder info, StringBuilder content, MyPrintWriter writer){

        //System.out.println("post_handler");
        //System.out.println("info: "+info.toString());
        //System.out.println("content: "+content.toString());

        switch (path_parts[1]){
            case "deck":
                this.deck(auth, content, writer);
                break;

            case "users":

                break;

            default:
                writer.println(400, "unknown request");
        }
    }

    private void deck(String auth, StringBuilder content, MyPrintWriter writer){

        String cleanedContent = content.toString().replaceAll("[\"\\[\\]]", "");
        // Split by comma
        String[] card_list = cleanedContent.split("\\s*,\\s*");

        if(card_list.length != 4) {
            writer.println(400, "Bad request");
            return;
        }

        String uid = dba.GET_uid_from_auth(auth, writer);
        dba.PUT_deck(uid, card_list, writer);
    }
}
