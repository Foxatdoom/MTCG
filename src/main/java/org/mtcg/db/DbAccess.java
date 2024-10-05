package org.mtcg.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.Users;

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
        System.out.println("Database connection established successfully! \n");
    }

    public String close(){
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                return "Error closing the database connection: " + e.getMessage();
            }
        }
        return "Connection closed successfully";
    }



    // ---------------------------------- USING DB ---------------------------------




    public String createUser(StringBuilder userdata){
        ObjectMapper objectMapper = new ObjectMapper(); // Create an ObjectMapper instance
        Users user = null; // Initialize user variable

        try {
            // Convert StringBuilder to String and deserialize into User object
            user = objectMapper.readValue(userdata.toString(), Users.class);
        } catch (IOException e) {
            e.printStackTrace();
            return "Error parsing user data";
        }

        String username = user.getUsername();
        String password = user.getPassword();

        // Checking if user already exists
        String check = "SELECT username FROM users WHERE username = '" + username + "'";
        try {
            Statement statement = connection.createStatement(); // Create a Statement object
            ResultSet resultSet = statement.executeQuery(check); // Execute the query

            if (resultSet.next()) { // Check if a result exists
                return "405 User already exists"; // Username already exists
            }
        } catch (SQLException e) {
            e.printStackTrace(); // Handle SQL exceptions appropriately
        }

        // Inserting the user
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.executeUpdate();
            //return "User created successfully with username: " + username + " and password: " + password;
            return "200 OK";

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error creating user: " + e.getMessage();
        }
    }














    public String createCard(){
        return "creating card";
    }
}
