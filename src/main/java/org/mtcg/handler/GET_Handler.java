package org.mtcg.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.Card;
import org.mtcg.Model.Package;
import org.mtcg.Model.User;
import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;
import java.util.Objects;


public class GET_Handler {

    DbAccess dba;

    public GET_Handler(DbAccess dba){
        this.dba = dba;
    }

    public void call_request(String auth, String[] path_parts, StringBuilder info, StringBuilder content, MyPrintWriter writer){

        //System.out.println("post_handler");
        //System.out.println("info: "+info.toString());
        //System.out.println("content: "+content.toString());

        switch (path_parts[1]){
            case "cards":
                this.cards(auth, writer);
                break;


            case "deck":
                if(path_parts.length > 2){} //this.spezial_deck(auth, path_parts[1], content, writer);
                else this.deck(auth, writer);
                break;


            case "users":
                //this.users();
                break;


            case "stats":

                break;


            case "scoreboard":

                break;


            case "tradings":

                break;
        }
    }

    private void cards(String auth, MyPrintWriter writer) {
        System.out.println("auth: " + auth);
        if(auth == null | Objects.equals(auth, "")) writer.println(403, "Unauthorized");
        else {

            String uid = dba.GET_uid_from_auth(auth, writer);
            List<Card> cards = dba.GET_cards(uid, writer);

            if(cards == null){
                return;
            }

            // get cards in form of json
            String output = "[";
            for (int i = 0; i < cards.size(); i++) {
                output += cards.get(i).toJson();
                if(i+1 != cards.size()) output += ",";
            }
            output += "]";


            writer.println(200, "OK", output);
        }
    }

    private void deck(String auth, MyPrintWriter writer){

    }

    private void spezial_deck(){
        //todo
    }
}
