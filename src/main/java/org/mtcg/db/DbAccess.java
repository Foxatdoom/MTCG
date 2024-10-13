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


    // ------------ POST --------------

    public String POST_users(StringBuilder data, String additional_request){ //aka register
        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance
        User user = null; // Initialize player variable

        try {
            // Convert StringBuilder to String and deserialize into Player object
            user = objectMapper.readValue(data.toString(), User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return "HTTP 400 - Error parsing player data";
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

    public String POST_sessions(StringBuilder data, String additional_request){ //aka login
        ObjectMapper objectMapper = new ObjectMapper();
        User user = null; // Initialize player variable

        try {
            // Convert StringBuilder to String and deserialize into Player object
            user = objectMapper.readValue(data.toString(), User.class);
        } catch (IOException e) {
            e.printStackTrace();
            return "HTTP 400 - Error parsing player data";
        }

        String username = user.getUsername();
        String password = user.getPassword();

        String sql = "SELECT * FROM \"user\" WHERE username = ? AND password = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return "HTTP 201 - " + resultSet.getString("token");
            else return "HTTP 401 - Login failed";
        } catch (SQLException e) {
            return "HTTP 400 - " + e.getMessage();
        }
    }

    public String POST_packages(StringBuilder data, String additional_request){
        return "POST_packages";
    }

    public String POST_transactions(StringBuilder data, String additional_request){
        return "POST_transactions";
    }

    public String POST_battles(StringBuilder data, String additional_request){
        return "POST_battles";
    }

    public String POST_tradings(StringBuilder data, String additional_request){
        return "POST_tradings";
    }


    // ------------ GET --------------

    public String GET_cards(StringBuilder data, String additional_request){
        return "GET_cards";
    }

    public String GET_deck(StringBuilder data, String additional_request){
        return "GET_deck";
    }

    public String GET_users(StringBuilder data, String additional_request){
        return "GET_users";
    }

    public String GET_stats(StringBuilder data, String additional_request){
        return "GET_stats";
    }

    public String GET_scoreboard(StringBuilder data, String additional_request){
        return "GET_scoreboard";
    }

    public String GET_tradings(StringBuilder data, String additional_request){
        return "GET_tradings";
    }


    // ------------ PUT --------------

    public String PUT_deck(StringBuilder data, String additional_request){
        return "PUT_deck";
    }

    public String PUT_users(StringBuilder data, String additional_request){
        return "PUT_users";
    }


    // ------------ DELETE --------------
    public String DELETE_tradings(StringBuilder data, String additional_request){
        return "DELETE_tradings";
    }
}
