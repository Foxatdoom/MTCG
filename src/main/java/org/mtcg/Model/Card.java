package org.mtcg.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Random;

public class Card {
    private final String id;
    private final String name;
    private final float damage;
    private final String element_type;
    private final String card_type;

    private double hitrate = 100.0;
    private boolean lasthitmissed = false;

    // Using @JsonCreator to get curl request correctly
    @JsonCreator
    public Card(
            @JsonProperty("Id") String id,
            @JsonProperty("Name") String name,
            @JsonProperty("Damage") float damage
    ) {
        this.id = id;
        this.name = name;
        this.damage = damage;

        // if the first letters of name "Regular" (or nothing), "Fire", or "Water" -> element_type
        if(name.startsWith("Fire")){
            this.element_type = "Fire";
        }
        else if(name.startsWith("Water")){
            this.element_type = "Water";
        }
        else {
            this.element_type = "Regular";
        }

        //if last letter "Spell" -> card_type
        if(name.endsWith("Spell")){
            this.card_type = "Spell";
        }
        else {
            this.card_type = "Monster";
        }
    }

    // GETTER-Methods
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // Extra feature: hitrate system - default 100%
    public float getDamage() {
        double random = Math.random() * 100;
        if(random < this.hitrate){
            hitrate -= 10;
            lasthitmissed = false;
            return damage;
        }
        else {
            hitrate = 100;
            lasthitmissed = true;
            return 0;
        }
    }

    public String getElement_type() {
        return element_type;
    }

    public String getCard_type() {
        return card_type;
    }

    public boolean isLasthitmissed() {
        return lasthitmissed;
    }

    public String toJson(){
        return "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"Card-type\":\"" + card_type + "\",\"Element-type\":\"" + element_type + "\",\"damage\":" + damage + "}";
    }

    public String toJsonPlain(){
        return "{\"name\":\"" + name + "\",\"damage\":" + damage + "}";
    }
}
