package org.mtcg.db;

import org.mtcg.Model.Card;
import org.mtcg.Model.User;
import org.mtcg.MyPrintWriter;
import java.sql.*;
import java.util.List;
import java.util.UUID;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

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

    public void POST_users(User user, MyPrintWriter writer){ //aka register

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
            writer.println(201,"OK");

        } catch (SQLException e) {
            if (e.getMessage().startsWith("ERROR: duplicate key")) {

                writer.println(405, "User already exists");
            }
            else {
                writer.println(405, "Login Failed");
            }
        }
    }

    public void POST_sessions(User user, MyPrintWriter writer){ //aka login


        String username = user.getUsername();
        String password = user.getPassword();

        String sql = "SELECT * FROM \"user\" WHERE username = ? AND password = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                writer.println(200, resultSet.getString("token"));
            }
            else{
                writer.println(401, "Login Failed");
            }
        } catch (SQLException e) {
            writer.println(400, e.getMessage());
        }
    }

    public void POST_packages(List<Card> cards, MyPrintWriter writer){

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
                    writer.println(500, "No generated keys returned for package");
                }
            }

        } catch (SQLException e) {
            writer.println(405,e.getMessage() + " (by inserting package)");
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
                        writer.println(500, "No generated keys returned for card");
                    }
                }

            } catch (SQLException e) {
                writer.println(405,e.getMessage() + " (by inserting cards)");
                return;
            }

            UUID pi = UUID.fromString(package_id);
            UUID ci = UUID.fromString(card_id);

            String sql_package_card = "INSERT INTO package_card (package_id, card_id) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql_package_card)) {
                preparedStatement.setObject(1, pi);
                preparedStatement.setObject(2, ci);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                writer.println(405,e.getMessage() + " (by inserting package_cards)");
                return;
            }
        }
        writer.println(201, "Package added");
    }

    public void POST_transactions(String auth, MyPrintWriter writer){

        String user_id = "";

        //step 1: get user_id from auth
        String sql_get_id = "SELECT * FROM \"user\" WHERE token = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_get_id)) {
            preparedStatement.setString(1, auth);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Process ResultSet
                while (resultSet.next()) {
                    user_id = resultSet.getString("user_id");
                }
            }
        }
        catch (SQLException e) {
            writer.println(404, "user not found");
            return;
        }

        //step 2: check if >= 5 coins
        String sql_check_coins = "SELECT coins FROM \"user\" WHERE user_id = ? AND coins > 4";

        UUID uuid = UUID.fromString(user_id);

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_check_coins)) {
            preparedStatement.setObject(1, uuid);

            // Use executeQuery for SELECT statements
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) { // Check if a result exists
                    //System.out.println("got enough money");
                } else {
                    writer.println(403, "Not enough Money");
                    return;
                }
            }
        } catch (SQLException e) {
            writer.println(400, e.getMessage() + " (step 2)");
            return;
        }


        //step 3: check if packages available
        String sql_check_packages = "SELECT COUNT(*) FROM package WHERE user_id IS NULL";

        try (PreparedStatement packageStmt = connection.prepareStatement(sql_check_packages)) {
            try (ResultSet rs = packageStmt.executeQuery()) {
                if (rs.next()) {
                    if(rs.getInt(1) < 1){
                        writer.println(403, "No packages available");
                        return;
                    }
                }
            }
        } catch (SQLException e) {
            writer.println(400, e.getMessage() + " (step 3)");
            return;
        }

        // Step 4.1: Decrease user's coins by 5
        String sql_decrease_coins = "UPDATE \"user\" SET coins = coins - 5 WHERE token = ?";

        // Step 4.2: Assign a random package to the user
        String sql_assign_package = "UPDATE package SET user_id = ? WHERE package_id = (" +
                "SELECT package_id FROM package WHERE user_id IS NULL LIMIT 1)";

        try (PreparedStatement stmtDecreaseCoins = connection.prepareStatement(sql_decrease_coins);
             PreparedStatement stmtAssignPackage = connection.prepareStatement(sql_assign_package)) {

            // Decrease coins
            stmtDecreaseCoins.setString(1, auth);
            int rowsUpdated = stmtDecreaseCoins.executeUpdate();

            if (rowsUpdated == 0) {
                writer.println(403, "Failed to decrease coins: invalid user token or insufficient coins.");
                return;
            }

            // Assign package
            stmtAssignPackage.setObject(1, uuid);
            rowsUpdated = stmtAssignPackage.executeUpdate();

            if (rowsUpdated == 0) {
                writer.println(403, "No available packages to assign.");
                return;
            }

        } catch (SQLException e) {
            writer.println(400, e.getMessage());
            return;
        }

        writer.println(201, "Package successfully bought");
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
