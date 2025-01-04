package org.mtcg.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtcg.Model.User;
import org.mtcg.MyPrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DbAccess_Test {

    private DbAccess dbAccess;
    private Connection mockConnection;
    private MyPrintWriter writer;

    @BeforeEach
    void setUp() throws SQLException {
        mockConnection = mock(Connection.class);
        writer = mock(MyPrintWriter.class);
        dbAccess = new DbAccess(mockConnection);
    }

    @Test
    void GET_uid_from_auth_failed() throws SQLException {
        // Simulate SQLException when preparing statement
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Simulated failure"));

        // Call the method
        dbAccess.GET_uid_from_auth("testing-mtcgToken", writer);

        // Verify writer interaction
        verify(writer, times(1)).println(404, "user not found");
    }

    @Test
    void testPOST_users_success() throws SQLException {
        // Arrange
        User user = new User("testUser", "testPassword");
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement("INSERT INTO \"user\" (username, password, token) VALUES (?, ?, ?)"))
                .thenReturn(mockPreparedStatement);

        // Act

        dbAccess.POST_users(user, writer);

        // Assert
        verify(mockConnection, times(1)).prepareStatement("INSERT INTO \"user\" (username, password, token) VALUES (?, ?, ?)");
        verify(mockPreparedStatement, times(1)).setString(1, user.getUsername());
        verify(mockPreparedStatement, times(1)).setString(2, user.getPassword());
        verify(mockPreparedStatement, times(1)).setString(3, user.getToken());
        verify(mockPreparedStatement, times(1)).executeUpdate();
        verify(writer, times(1)).println(201, "OK");
    }
}