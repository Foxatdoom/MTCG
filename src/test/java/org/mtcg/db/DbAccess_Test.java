package org.mtcg.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mtcg.Model.Card;
import org.mtcg.Model.User;
import org.mtcg.MyPrintWriter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

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
        verify(writer, times(1)).println(201, "User created");
    }

    @Test
    void POST_sessions_success() throws SQLException {
        User user = new User("testUser", "testPassword");
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT * FROM \"user\" WHERE username = ? AND password = ?"))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("token")).thenReturn("testUser-mtcgToken");

        dbAccess.POST_sessions(user, writer);

        verify(mockPreparedStatement, times(1)).setString(1, user.getUsername());
        verify(mockPreparedStatement, times(1)).setString(2, user.getPassword());
        verify(writer, times(1)).println(200, "testUser-mtcgToken");
    }

    @Test
    void POST_sessions_failure() throws SQLException {
        User user = new User("testUser", "wrongPassword");
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT * FROM \"user\" WHERE username = ? AND password = ?"))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        dbAccess.POST_sessions(user, writer);

        verify(writer, times(1)).println(401, "Login Failed");
    }

    @Test
    void POST_packages_success() throws SQLException {
        List<Card> cards = List.of(
                new Card(UUID.randomUUID().toString(), "Card1", 10),
                new Card(UUID.randomUUID().toString(), "Card2", 20),
                new Card(UUID.randomUUID().toString(), "Card3", 30),
                new Card(UUID.randomUUID().toString(), "Card4", 40),
                new Card(UUID.randomUUID().toString(), "Card5", 50)
        );

        PreparedStatement mockPackageStmt = mock(PreparedStatement.class);
        PreparedStatement mockCardStmt = mock(PreparedStatement.class);
        PreparedStatement mockPackageCardStmt = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("INSERT INTO package (user_id) VALUES (?)", PreparedStatement.RETURN_GENERATED_KEYS))
                .thenReturn(mockPackageStmt);
        when(mockPackageStmt.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString(1)).thenReturn(UUID.randomUUID().toString());

        when(mockConnection.prepareStatement("INSERT INTO card (card_id, name, damage, element_type, card_type) VALUES (?, ?, ?, ?, ?)"))
                .thenReturn(mockCardStmt);
        when(mockConnection.prepareStatement("INSERT INTO package_card (package_id, card_id) VALUES (?, ?)"))
                .thenReturn(mockPackageCardStmt);

        dbAccess.POST_packages(cards, writer);

        verify(writer, times(1)).println(201, "Package added");
    }

    @Test
    void POST_transactions_notEnoughCoins() throws SQLException {
        PreparedStatement mockCheckCoinsStmt = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT coins FROM \"user\" WHERE user_id = ? AND coins > 4"))
                .thenReturn(mockCheckCoinsStmt);
        when(mockCheckCoinsStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        dbAccess.POST_transactions(UUID.randomUUID().toString(), writer);

        verify(writer, times(1)).println(403, "Not enough money");
    }

    @Test
    void GET_cards_noCardsFound() throws SQLException {
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT * FROM card WHERE owner = ?"))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        dbAccess.GET_cards(UUID.randomUUID().toString(), writer);

        verify(writer, times(1)).println(404, "user has no cards or user not found");
    }

    @Test
    void GET_deck_noDeckFound() throws SQLException {
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT card_id FROM deck WHERE user_id = ?"))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        dbAccess.GET_deck(UUID.randomUUID().toString(), writer);

        verify(writer, times(0)).println(anyInt(), anyString()); // No output expected
    }

    @Test
    void PUT_users_success() throws SQLException {
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement("UPDATE \"user\" SET name = ?, bio = ?, image = ? WHERE user_id = ?"))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);

        dbAccess.PUT_users(UUID.randomUUID().toString(), "testName", "testBio", "testImage", writer);

        verify(writer, times(1)).println(200, "User changed successfully");
    }

    @Test
    void PUT_users_failure() throws SQLException {
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);

        when(mockConnection.prepareStatement("UPDATE \"user\" SET name = ?, bio = ?, image = ? WHERE user_id = ?"))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(0);

        dbAccess.PUT_users(UUID.randomUUID().toString(), "testName", "testBio", "testImage", writer);

        verify(writer, times(1)).println(403, "Failed to change user information");
    }

    @Test
    void GET_stats_userNotFound() throws SQLException {
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT elo, games_played FROM \"user\" WHERE user_id = ?"))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        dbAccess.GET_stats(UUID.randomUUID().toString(), writer);

        verify(writer, times(1)).println(404, "user not found");
    }

    @Test
    void GET_scoreboard_success() throws SQLException {
        PreparedStatement mockPreparedStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement("SELECT elo, username FROM \"user\" ORDER BY elo DESC"))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
        when(mockResultSet.getInt("elo")).thenReturn(1000).thenReturn(900);
        when(mockResultSet.getString("username")).thenReturn("User1").thenReturn("User2");

        dbAccess.GET_scoreboard(writer);

        verify(writer, times(1)).println(200, "Scoreboard found", "[{\"elo\":1000,\"username\":\"User1\"},{\"elo\":900,\"username\":\"User2\"}]");
    }
}