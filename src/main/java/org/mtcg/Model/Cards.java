package org.mtcg.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Cards {
    private String id;
    private String name;
    private float damage;
    private String element_type; // not mentioned in curl??????????????
    private String card_type;

    // Using @JsonCreator to get curl request correctly
    @JsonCreator
    public Cards(
            @JsonProperty("Id") String id,
            @JsonProperty("Name") String name,
            @JsonProperty("Damage") float damage
    ) {
        this.id = id;
        this.name = name;
        this.damage = damage;
    }

    // GETTER-Methods
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public float getDamage() {
        return damage;
    }

    public String getElement_type() {
        return element_type;
    }

    public String getCard_type() {
        return card_type;
    }
}
