package org.mtcg.Model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class Model_Test {

    @Test
    void User_getInfo() {
        User u = new User("john", "john123");
        assertEquals("john", u.getUsername());
        assertEquals("john123", u.getPassword());
        assertEquals("john-mtcgToken", u.getToken());
    }

    @Test
    void Card_getInfo(){
        Card c = new Card("999f0dc7-37b5-426e-994e-43fc3ac83c08", "FireWitch", 15.0f);
        assertEquals("999f0dc7-37b5-426e-994e-43fc3ac83c08",c.getId());
        assertEquals("FireWitch",c.getName());
        assertEquals(15.0f, c.getDamage());
        assertEquals("Monster", c.getCard_type());
        assertEquals("Fire", c.getElement_type());
    }

    @Test
    void Card_toJson(){
        Card c = new Card("999f0dc7-37b5-426e-994e-43fc3ac83c08", "FireWitch", 15.0f);
        assertEquals("{\"id\":\"999f0dc7-37b5-426e-994e-43fc3ac83c08\",\"name\":\"FireWitch\",\"Card-type\":\"Monster\",\"Element-type\":\"Fire\",\"damage\":"+15.0f+"}", c.toJson());
    }

    @Test
    void Package_getCard(){
        Card c1 = new Card("0", "WaterGoblin", 5f);
        Card c2 = new Card("1", "Slime", 1f);
        Card c3 = new Card("2", "DarkSpell", 20f);
        Card c4 = new Card("3", "???", 0f);
        Card c5 = new Card("4", "NormalElf", 3f);
        Package p = new Package(c1,c2,c3,c4,c5);

        assertEquals("Slime", p.getSpecificCard(1).getName());
        assertEquals("Spell", p.getSpecificCard(2).getCard_type());
        assertEquals(5f, p.getSpecificCard(0).getDamage());
    }

    // won't test stack because it is too similar to deck
    @Test
    void Deck(){
        Card c1 = new Card("0", "WaterGoblin", 5f);
        Card c2 = new Card("1", "Slime", 1f);
        Card c3 = new Card("2", "DarkSpell", 20f);
        Card c4 = new Card("4", "NormalElf", 3f);
        Deck d = new Deck(c1,c2,c3,c4);

        String s = "[{\"name\":\"WaterGoblin\",\"damage\":5.0},{\"name\":\"Slime\",\"damage\":1.0},{\"name\":\"DarkSpell\",\"damage\":20.0},{\"name\":\"NormalElf\",\"damage\":3.0}]";

        assertEquals("DarkSpell", d.getCard(2).getName());
        assertEquals(s, d.toJson(true));
    }
}