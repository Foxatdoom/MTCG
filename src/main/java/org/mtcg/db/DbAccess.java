package org.mtcg.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.User;

import java.io.IOException;
import java.sql.*;

public class DbAccess {

    Connection connection = null;

    public DbAccess() throws SQLException {
        DbConnection dbConnection = new DbConnection();
        connection = null;
        try {
            connection = dbConnection.connect(); // Establish db connection
        } catch (SQLException e) {
            throw new SQLException(e);
        }
        //System.out.println("Database connection established successfully! \n");
    }

    public void close() throws SQLException {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new SQLException(e);
            }
        }
        //System.out.println("DB Connection closed");
    }



    // ---------------------------------- USING DB ---------------------------------

    public String createUser(StringBuilder playerdata){
        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance
        User user = null; // Initialize player variable

        try {
            // Convert StringBuilder to String and deserialize into Player object
            user = objectMapper.readValue(playerdata.toString(), User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error parsing player data";
        }

        String username = user.getUsername();
        String password = user.getPassword();
        String token = user.getToken();

        // Inserting the Player
        String sql = "INSERT INTO \"user\" (username, password, token) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, token);
            preparedStatement.executeUpdate();
            //return "Player created successfully with name: " + name + " and password: " + password;
            return "HTTP 201 - OK";

        } catch (SQLException e) {
            if (e.getMessage().startsWith("ERROR: duplicate key")) {
                return "HTTP 405 - User already exists";
            }
            else {
                return "HTTP 405 - " + e.getMessage();
            }

        }
    }














    public String createCard(){
        return "creating card";
    }
}
