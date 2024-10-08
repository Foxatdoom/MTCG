package org.mtcg.Model;

import java.util.List;

public class Stack {

    private List<Card> stack;


    public void addCard(Card card) {
        stack.add(card);
    }
    public void removeCard(Card card) {
        stack.remove(card);
    }
}
