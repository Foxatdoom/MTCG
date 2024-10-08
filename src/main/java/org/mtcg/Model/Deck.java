package org.mtcg.Model;

public class Deck {

    private final Card[] cards;

    // Constructor that takes 4 Card objects
    public Deck(Card card1, Card card2, Card card3, Card card4) {
        this.cards = new Card[4]; // 4 Cards in a deck
        this.cards[0] = card1;
        this.cards[1] = card2;
        this.cards[2] = card3;
        this.cards[3] = card4;
    }

    // for testing purposes
    public Card getCard(int index) {
        if (index < 0 || index >= cards.length) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + cards.length);
        }
        return cards[index];
    }

    // for battle
    public Card getRandomCard() {
        return cards[(int) (Math.random() * cards.length)];
    }
}
