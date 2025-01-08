package org.mtcg.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.Card;
import org.mtcg.Model.Deck;
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
                this.add_users(content, writer);
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

        ObjectMapper objectMapper = new ObjectMapper(); // Create ObjectMapper instance
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
            // Convert StringBuilder to User object
            user = objectMapper.readValue(content.toString(), User.class);
        } catch (IOException e) {
            writer.println(400, "Error parsing session data");
            return;
        }
        dba.POST_sessions(user, writer);
    }

    public void packages(String auth, StringBuilder content, MyPrintWriter writer){
        if(!Objects.equals(auth, "admin-mtcgToken")) writer.println(401, "Not Admin");
        ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper instance
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

    public void transactions(String auth, MyPrintWriter writer) {
        String uid = dba.GET_uid_from_auth(auth, writer);
        dba.POST_transactions(uid, writer);
    }

    public float calculate_card_dmg(Card c1, Card c2, boolean spell_involved){
        float p1_damage = c1.getDamage();

        if(spell_involved){
            if(Objects.equals(c1.getElement_type(), "Water") && Objects.equals(c2.getName().endsWith("Knight"), true)) p1_damage= 999f;

            else if(Objects.equals(c1.getCard_type(), "Spell") && Objects.equals(c2.getName().endsWith("Kraken"), true)) p1_damage = 0f;

            else if(Objects.equals(c1.getElement_type(), "Water") && Objects.equals(c2.getElement_type(), "Fire")) p1_damage = (c1.getDamage()*2);
            else if(Objects.equals(c1.getElement_type(), "Fire") && Objects.equals(c2.getElement_type(), "Water")) p1_damage = (c1.getDamage()*0.5f);

            else if(Objects.equals(c1.getElement_type(), "Fire") && Objects.equals(c2.getElement_type(), "Regular")) p1_damage = (c1.getDamage()*2);
            else if(Objects.equals(c1.getElement_type(), "Regular") && Objects.equals(c2.getElement_type(), "Fire")) p1_damage = (c1.getDamage()*0.5f);

            else if(Objects.equals(c1.getElement_type(), "Regular") && Objects.equals(c2.getElement_type(), "Water")) p1_damage = (c1.getDamage()*2);
            else if(Objects.equals(c1.getElement_type(), "Water") && Objects.equals(c2.getElement_type(), "Regular")) p1_damage = (c1.getDamage()*0.5f);
        }
        else {
            if(Objects.equals(c1.getName().endsWith("Goblin"), true) && Objects.equals(c2.getName().endsWith("Dragon"), true)) p1_damage = 0;
            else if(Objects.equals(c1.getName().endsWith("Wizzard"), true) && Objects.equals(c2.getName().endsWith("Ork"), true)) p1_damage = 0;
            else if(Objects.equals(c1.getName().endsWith("Dragon"), true) && Objects.equals(c2.getName(), "FireElf")) p1_damage = 0;
        }

        return p1_damage;
    }


    public void battles(String auth, MyPrintWriter writer) {
        String[] info = dba.POST_battles(dba.GET_uid_from_auth(auth, writer), writer);

        if(!Objects.equals(info[0], "true")){
           return;
        }

        String uid1 = info[1];
        String uid2 = info[2];
        Deck d1 = dba.GET_deck(uid1, writer);
        Deck d2 = dba.GET_deck(uid2, writer);

        Card c1;
        Card c2;
        float dmg1 = 0;
        float dmg2 = 0;

        String log = "";

        // actual fight
        for (int i = 1; i < 101; i++) {
            log += "\n\nRound " + i + ": ";
            //choose random card
            c1 = d1.getRandomCard();
            c2 = d2.getRandomCard();
            dmg1 = c1.getDamage();
            dmg2 = c2.getDamage();

            //Spell fight -> Element matters
            if(Objects.equals(c1.getCard_type(), "Spell") | Objects.equals(c2.getCard_type(), "Spell")){
                dmg1 = this.calculate_card_dmg(c1, c2, true);
                dmg2 = this.calculate_card_dmg(c2, c1, true);
            }
            else {
                dmg1 = this.calculate_card_dmg(c1, c2, false);
                dmg2 = this.calculate_card_dmg(c2, c1, false);
            }
            log += c1.getName() + ": " + dmg1 + (c1.isLasthitmissed() ? "(missed)" : "") + "(" + c1.getDamage() + "), " + c2.getName() + ": " + dmg2 + (c1.isLasthitmissed() ? "(missed)" : "") + "(" + c2.getDamage() + ")";

            // Player 1 won round
            if(dmg1 > dmg2){
                // Player 1 won match
                if(d2.getCardCount() == 1) {
                    dba.POST_battles_setStats(uid1, 3, writer);
                    dba.POST_battles_setStats(uid2, -5, writer);
                    log += "\n=> Player 1 won the match!";
                    writer.println(200, "Player 1 Won", log);
                    return;
                }

                // user1 gets card of user2
                Card c = d2.removeCardByCardId(c2.getId());
                d1.addcard(c);
                log += "        -> Player1 ("+d1.getCardCount()+" cards) won round against Player2 ("+d2.getCardCount()+" cards)";
            }

            // Player 2 won round
            else if(dmg1 < dmg2){
                // Player 2 won match
                if(d1.getCardCount() == 1) {
                    dba.POST_battles_setStats(uid1, -5, writer);
                    dba.POST_battles_setStats(uid2, 3, writer);
                    log += "\n=> Player 2 won the match!";
                    writer.println(200, "Player 2 Won", log);
                    return;
                }

                // user2 gets card of user1
                Card c = d1.removeCardByCardId(c1.getId());
                d2.addcard(c);
                log += "        -> Player2 ("+d2.getCardCount()+" cards) won round against Player1 ("+d1.getCardCount()+" cards)";
            }
            else {
                log += " -> equal dmg - no changes";
            }
        }
        writer.println(200, "reached Round limit - elo are not changed", log);
    }

    public void tradings(String auth, StringBuilder content, MyPrintWriter writer) {
        // 20) todo
    }

    public void spezific_tradings(String auth, String pathPart, StringBuilder content, MyPrintWriter writer) {
        // 20) todo
    }

}
