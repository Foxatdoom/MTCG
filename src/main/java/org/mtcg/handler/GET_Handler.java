package org.mtcg.handler;

import org.mtcg.Model.Card;
import org.mtcg.Model.Deck;
import org.mtcg.Model.User;
import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;
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
                if(path_parts.length > 2) this.deck(auth, true, writer);
                else this.deck(auth, false, writer);
                break;


            case "users":
                this.users(auth, path_parts[2], writer);
                break;


            case "stats":

                break;


            case "scoreboard":

                break;


            case "tradings":

                break;

            default:
                writer.println(400, "unknown request");
        }
    }

    private void cards(String auth, MyPrintWriter writer) {
        //System.out.println("auth: " + auth);
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


            writer.println(200, "Cards found", output);
        }
    }

    private void deck(String auth, boolean is_plain_mode, MyPrintWriter writer){
        String output = "[]";
        if(auth == null | Objects.equals(auth, "")) writer.println(403, "Unauthorized");
        else {
            String uid = dba.GET_uid_from_auth(auth, writer);
            Deck d = dba.GET_deck(uid, writer);
            if(d != null) output = d.toJson(is_plain_mode);
            writer.println(200, "Deck found", output);
        }
    }

    private void users(String auth, String what_user, MyPrintWriter writer){
        if(!Objects.equals(auth, what_user += "-mtcgToken")) writer.println(403, "Unauthorized");
        else {
            String uid = dba.GET_uid_from_auth(auth, writer);
            User u = dba.GET_users(uid, writer);
            String output = u.toJson();
            writer.println(200, "User found", output);
        }
    }
}
