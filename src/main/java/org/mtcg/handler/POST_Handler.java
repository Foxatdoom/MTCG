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

public class POST_Handler {

    DbAccess dba;

    public POST_Handler(DbAccess dba){
        this.dba = dba;
    }

    public void call_request(StringBuilder info, StringBuilder content, MyPrintWriter writer){

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

    public void users(StringBuilder content, MyPrintWriter writer){

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

    public void packages(StringBuilder content, MyPrintWriter writer){
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
                for (Card card : pack.getPackageCards()) {
                    System.out.println("Card: " + card.getName() +
                            ", Type: " + card.getCard_type() +
                            ", Element: " + card.getElement_type() +
                            ", Damage: " + card.getDamage());
                }

            } else {
                writer.println(400, "Invalid number of cards in JSON. Expected 5 cards.");
                return;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }



        dba.POST_packages(cardList, writer);
    }

}
