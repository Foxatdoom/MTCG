package org.mtcg.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtcg.Model.Card;
import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class POST_HandlerTest {

    private MyPrintWriter writer;
    private POST_Handler handler;
    private DbAccess dbAccess;

    @BeforeEach
    void setUp() throws SQLException {
        dbAccess = mock(DbAccess.class);
        handler = new POST_Handler(dbAccess);
        writer = mock(MyPrintWriter.class);
    }

    @Test
    void calculate_spell_dmg(){
        Card c1 = new Card("1", "WaterSpell", 10f);
        Card c2 = new Card("2", "NormalSpell", 10f);
        float c1_dmg;
        float c2_dmg;
        c1_dmg = handler.calculate_card_dmg(c1, c2, true);
        c2_dmg = handler.calculate_card_dmg(c2, c1, true);

        assertEquals(5, c1_dmg);
        assertEquals(20, c2_dmg);
    }

    @Test
    void calculate_monster_dmg(){
        Card c1 = new Card("1", "Knight", 30f);
        Card c2 = new Card("2", "FireElf", 10f);
        float c1_dmg;
        float c2_dmg;
        c1_dmg = handler.calculate_card_dmg(c1, c2, false);
        c2_dmg = handler.calculate_card_dmg(c2, c1, false);

        assertEquals(30, c1_dmg);
        assertEquals(10, c2_dmg);
    }

    @Test
    void calculate_special_encounters_dmg(){
        Card c1 = new Card("1", "WaterSpell", 10f);
        Card c2 = new Card("2", "Knight", 10f);
        Card c3 = new Card("3", "Kraken", 10f);
        float c12_dmg;
        float c13_dmg;
        c12_dmg = handler.calculate_card_dmg(c1, c2, true);
        c13_dmg = handler.calculate_card_dmg(c1, c3, true);

        assertEquals(999, c12_dmg);
        assertEquals(0, c13_dmg);
    }
}