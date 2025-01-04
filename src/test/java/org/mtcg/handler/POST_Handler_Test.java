package org.mtcg.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;
import org.mtcg.Model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class POST_Handler_Test {

    private POST_Handler post;
    private MyPrintWriter writer;

    @BeforeEach
    void setUp() throws SQLException {
        writer = mock(MyPrintWriter.class);
        post = new POST_Handler(null);
    }



    @Test
    void call_request_failed() throws SQLException {
        //todo
    }


}