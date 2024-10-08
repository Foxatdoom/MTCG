package org.mtcg.Model;

public class Package {

    private final Card[] cards;

    public Package(Card card1, Card card2, Card card3, Card card4, Card card5) {
        this.cards = new Card[5]; // 5 Cards in a Package
        this.cards[0] = card1;
        this.cards[1] = card2;
        this.cards[2] = card3;
        this.cards[3] = card4;
        this.cards[4] = card5;
    }

    public Card[] getPackageCards() {
        return cards;
    }
}
