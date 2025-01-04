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

public class POST_Handler {

    DbAccess dba;

    public POST_Handler(DbAccess dba){
        this.dba = dba;
    }

    public void call_request(String auth, String[] path_parts, StringBuilder info, StringBuilder content, MyPrintWriter writer){

        //System.out.println("post_handler");
        //System.out.println("info: "+info.toString());
        //System.out.println("content: "+content.toString());

        switch (path_parts[1]){
            case "users":
                if(path_parts.length > 2) this.edit_users(auth, path_parts[1], content, writer);
                else this.add_users(content, writer);
                break;


            case "sessions":
                this.sessions(content, writer);
                break;


            case "packages":
                this.packages(auth, content, writer);
                break;


            case "transactions":
                if(Objects.equals(path_parts[2], "packages")) this.transactions(auth, writer);
                break;


            case "battles":
                this.battles(auth, writer);
                break;


            case "tradings":
                if(path_parts.length > 2) this.spezific_tradings(auth, path_parts[1], content, writer);
                else this.tradings(auth, content, writer);
                break;

            default:
                writer.println(400, "unknown request");
        }
    }




    public void add_users(StringBuilder content, MyPrintWriter writer){

        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance
        User user = null; // Initialize player variable

        try {
            // Convert StringBuilder to String and deserialize into Player object
            user = objectMapper.readValue(content.toString(), User.class);
        } catch (IOException e) {
            writer.println(400, "Error parsing player data");
            return;
        }

        dba.POST_users(user, writer);
    }

    public void edit_users(String auth, String what_user, StringBuilder content, MyPrintWriter writer){
        // 14) todo
    }

    public void sessions(StringBuilder content, MyPrintWriter writer){
        ObjectMapper objectMapper = new ObjectMapper();
        User user = null; // Initialize player variable

        try {
            // Convert StringBuilder to String and deserialize into Player object
            user = objectMapper.readValue(content.toString(), User.class);
        } catch (IOException e) {
            writer.println(400, "Error parsing session data");
            return;
        }
        dba.POST_sessions(user, writer);
    }

    public void packages(String auth, StringBuilder content, MyPrintWriter writer){
        if(!Objects.equals(auth, "admin-mtcgToken")) writer.println(401, "Not Admin");
        // ObjectMapper instance
        ObjectMapper objectMapper = new ObjectMapper();
        Package pack = null;
        List<Card> cardList = null;

        try {
            cardList = objectMapper.readValue(content.toString(), new TypeReference<List<Card>>() {});

            // package is always 5
            if (cardList.size() == 5) {
                pack = new Package(
                    cardList.get(0),
                    cardList.get(1),
                    cardList.get(2),
                    cardList.get(3),
                    cardList.get(4)
                );

                // Print Package details
                /*for (Card card : pack.getPackageCards()) {
                    System.out.println("Card: " + card.getName() +
                            ", Type: " + card.getCard_type() +
                            ", Element: " + card.getElement_type() +
                            ", Damage: " + card.getDamage());
                }*/

            } else {
                writer.println(400, "Invalid number of cards in JSON. Expected 5 cards.");
                return;
            }
        } catch (IOException e) {
            writer.println(400, e.getMessage());
        }

        dba.POST_packages(cardList, writer);
    }

    private void transactions(String auth, MyPrintWriter writer) {
        dba.POST_transactions(auth, writer);
    }

    private void battles(String auth, MyPrintWriter writer) {
        // 17) todo
    }

    private void tradings(String auth, StringBuilder content, MyPrintWriter writer) {
        // 20) todo
    }

    private void spezific_tradings(String auth, String pathPart, StringBuilder content, MyPrintWriter writer) {
        // 20) todo
    }

}
