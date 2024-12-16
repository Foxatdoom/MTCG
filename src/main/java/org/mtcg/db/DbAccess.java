package org.mtcg.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.Card;
import org.mtcg.Model.Package;
import org.mtcg.Model.User;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

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

    public void POST_users(User user, PrintWriter writer){ //aka register

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
            writer.println("HTTP/1.1 201\r\nContent-Type: text/plain\r\nOK");

        } catch (SQLException e) {
            if (e.getMessage().startsWith("ERROR: duplicate key")) {

                writer.println("HTTP/1.1 405\r\nContent-Type: text/plain\r\nUser already exists");
            }
            else {
                writer.println("HTTP/1.1 405\r\nContent-Type: text/plain\r\nLogin Failed");
            }
        }
    }

    public void POST_sessions(User user, PrintWriter writer){ //aka login


        String username = user.getUsername();
        String password = user.getPassword();

        String sql = "SELECT * FROM \"user\" WHERE username = ? AND password = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                writer.println("HTTP/1.1 201\r\nContent-Type: text/plain\r\n" + resultSet.getString("token"));
            }
            else{
                writer.println("HTTP/1.1 401\r\nContent-Type: text/plain\r\nLogin Failed");
            }
        } catch (SQLException e) {
            writer.println("HTTP/1.1 400\r\nContent-Type: text/plain\r\n" + e.getMessage());
        }
    }

    public void POST_packages(List<Card> cards, PrintWriter writer){

        String card_id = "";
        String package_id = "";

        // Inserting package with no owner yet (user_id set to null)
        String sql = "INSERT INTO package (user_id) VALUES (?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            preparedStatement.setNull(1, java.sql.Types.OTHER); // Set NULL for user_id
            preparedStatement.executeUpdate();

            // Retrieve the generated package ID
            try (ResultSet rs_package = preparedStatement.getGeneratedKeys()) {
                if (rs_package.next()) {  // Move to the first row
                    package_id = rs_package.getString(1); // Get the first column of the result
                } else {
                    throw new SQLException("No generated keys returned for package.");
                }
            }

        } catch (SQLException e) {
            writer.println("HTTP/1.1 405\r\nContent-Type: text/plain\r\n" + e.getMessage() + " (by inserting package)");
            return;
        }

        // System.out.println(package_id);

        String name = "";
        float damage = 0f;
        String element_type = "";
        String card_type = "";

        // inserting 5 cards
        for (int i = 0; i < 5; i++) {
            name = cards.get(i).getName();
            damage = cards.get(i).getDamage();
            element_type = cards.get(i).getElement_type();
            card_type = cards.get(i).getCard_type();

            String sql_card = "INSERT INTO card (name, damage, element_type, card_type) VALUES (?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql_card, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, name);
                preparedStatement.setFloat(2, damage);
                preparedStatement.setString(3, element_type);
                preparedStatement.setString(4, card_type);
                preparedStatement.executeUpdate();

                // Retrieve the generated package ID
                try (ResultSet rs_card = preparedStatement.getGeneratedKeys()) {
                    if (rs_card.next()) {  // Move to the first row
                        card_id = rs_card.getString(1); // Get the first column of the result
                    } else {
                        throw new SQLException("No generated keys returned for card.");
                    }
                }

            } catch (SQLException e) {
                writer.println("HTTP/1.1 405\r\nContent-Type: text/plain\r\n" + e.getMessage() + " (by inserting cards)");
                return;
            }

            // inserting package card (connection). how to get id ?????????????
            UUID pi = UUID.fromString(package_id);
            UUID ci = UUID.fromString(card_id);

            String sql_package_card = "INSERT INTO package_card (package_id, card_id) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql_package_card)) {
                preparedStatement.setObject(1, pi);
                preparedStatement.setObject(2, ci);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                writer.println("HTTP/1.1 405\r\nContent-Type: text/plain\r\n" + e.getMessage() + " (by inserting package_cards)");
                return;
            }
        }
        writer.println("HTTP/1.1 201 - OK");
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
