package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbAccess {

    Connection connection = null;

    public DbAccess(){

        DbConnection dbConnection = new DbConnection();
        connection = null;

        try {
            // Establish database connection
            connection = dbConnection.connect();
            System.out.println("Database connection established successfully!");



        } catch (SQLException e) {
            System.out.println("Error connecting to the database: " + e.getMessage());
        }
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

    public String createUser(){
        return "creating user";
    }
}
