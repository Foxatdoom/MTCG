package org.mtcg.Model;

import java.util.ArrayList;
import java.util.List;

public class Deck {

    private List<Card> cards;

    // Constructor that takes 4 Card objects
    public Deck(Card card1, Card card2, Card card3, Card card4) {
        cards = new ArrayList<Card>();
        cards.add(card1);
        cards.add(card2);
        cards.add(card3);
        cards.add(card4);
    }

    public Deck(){
        cards = new ArrayList<Card>();
    }

    // for testing purposes
    public Card getCard(int index) {
        if (index < 0 || index >= cards.size()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + cards.size());
        }
        return cards.get(index);
    }

    // for battle
    public Card getRandomCard() {
        return cards.get((int) (Math.random() * cards.size()));
    }

    public void addcard(Card c){
        if(cards.size() < 4) cards.add(c);
    }

    public String toJson(){
        String output = "[";
        for (int i = 0; i < cards.size(); i++) {
            output += cards.get(i).toJson();
            if(i+1 != cards.size()) output += ",";
        }
        output += "]";
        return output;
    }
}
