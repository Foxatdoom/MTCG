package org.mtcg.Model;

import java.util.ArrayList;
import java.util.List;

public class Stack {

    private final List<Card> stack;

    public Stack() {
        stack = new ArrayList<Card>();
    }

    public void addCard(Card card) {
        stack.add(card);
    }
    public void removeCard(Card card) {
        stack.remove(card);
    }

    public String toJson(){
        // get cards in form of json
        String output = "[";
        for (int i = 0; i < stack.size(); i++) {
            output += stack.get(i).toJson();
            if(i+1 != stack.size()) output += ",";
        }
        output += "]";
        return output;
    }
}
