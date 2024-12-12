package org.mtcg.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Package {

    private final Card[] cards;

    @JsonCreator
    public Package(
            @JsonProperty("Card1") Card card1,
            @JsonProperty("Card2") Card card2,
            @JsonProperty("Card3") Card card3,
            @JsonProperty("Card4") Card card4,
            @JsonProperty("Card5") Card card5
    ) {
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

    public Card getSpecificCard(int index){
        return cards[index];
    }
}
