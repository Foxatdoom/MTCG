package org.mtcg.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mtcg.Model.Card;
import org.mtcg.Model.Package;
import org.mtcg.Model.User;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public String POST_users(User user){ //aka register

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
            return "HTTP/1.1 201 - OK";

        } catch (SQLException e) {
            if (e.getMessage().startsWith("ERROR: duplicate key")) {
                return "HTTP/1.1 405 - User already exists";
            }
            else {
                return "HTTP/1.1 405 - " + e.getMessage();
            }
        }
    }

    public String POST_sessions(User user){ //aka login


        String username = user.getUsername();
        String password = user.getPassword();

        String sql = "SELECT * FROM \"user\" WHERE username = ? AND password = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) return "HTTP/1.0201 - " + resultSet.getString("token");
            else return "HTTP/1.1 401 - Login failed";
        } catch (SQLException e) {
            return "HTTP/1.1 400 - " + e.getMessage();
        }
    }

    public String POST_packages(Package p){
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.set(i, p.getSpecificCard(i));
        }



        // Inserting empty package

        int package_id = 0;
        int card_id = 0;

        String sql = "INSERT INTO package (user_id) VALUES (?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, null);
            preparedStatement.executeUpdate();

            // Retrieve the generated package id
            ResultSet rs_package = preparedStatement.getGeneratedKeys();
            rs_package.next();
            package_id = rs_package.getInt(1);

        } catch (SQLException e) {
            return "HTTP/1.1 405 - " + e.getMessage();
        }

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

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, name);
                preparedStatement.setFloat(2, damage);
                preparedStatement.setString(3, element_type);
                preparedStatement.setString(4, card_type);
                preparedStatement.executeUpdate();

                // Retrieve the generated card id
                ResultSet rs_card = preparedStatement.getGeneratedKeys();
                rs_card.next();
                card_id = rs_card.getInt(1);

            } catch (SQLException e) {
                return "HTTP/1.1 405 - " + e.getMessage();
            }

            // inserting package card (connection). how to get id ?????????????


            String sql_package_card = "INSERT INTO package_card (package_id, card_id) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setFloat(0, package_id); // set SERIAL ????
                preparedStatement.setFloat(1, card_id);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                return "HTTP/1.1 405 - " + e.getMessage();
            }
        }
            return "HTTP/1.1 201 - OK";
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
