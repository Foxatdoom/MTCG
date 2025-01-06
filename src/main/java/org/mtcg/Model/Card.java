package org.mtcg.Model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Card {
    private String id;
    private String name;
    private float damage;
    private String element_type;
    private String card_type;

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

    public float getDamage() {
        return damage;
    }

    public String getElement_type() {
        return element_type;
    }

    public String getCard_type() {
        return card_type;
    }

    public String toJson(){
        return "{\"id\":\"" + id + "\",\"name\":\"" + name + "\",\"Card-type\":\"" + card_type + "\",\"Element-type\":\"" + element_type + "\",\"damage\":" + damage + "}";
    }

    public String toJsonPlain(){
        return "{\"name\":\"" + name + "\",\"damage\":" + damage + "}";
    }
}
