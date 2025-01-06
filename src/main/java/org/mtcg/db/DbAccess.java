package org.mtcg.db;

import org.mtcg.Model.Card;
import org.mtcg.Model.Deck;
import org.mtcg.Model.User;
import org.mtcg.MyPrintWriter;
import java.sql.*;
import java.util.ArrayList;
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

    // for junit tests
    public DbAccess(Connection connection) throws SQLException {
        this.connection = connection;
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

        UUID cid = null;
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
            cid = UUID.fromString(cards.get(i).getId());
            name = cards.get(i).getName();
            damage = cards.get(i).getDamage();
            element_type = cards.get(i).getElement_type();
            card_type = cards.get(i).getCard_type();

            String sql_card = "INSERT INTO card (card_id, name, damage, element_type, card_type) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql_card)) {
                preparedStatement.setObject(1, cid);
                preparedStatement.setString(2, name);
                preparedStatement.setFloat(3, damage);
                preparedStatement.setString(4, element_type);
                preparedStatement.setString(5, card_type);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                writer.println(405,e.getMessage() + " (by inserting cards)");
                return;
            }

            UUID pi = UUID.fromString(package_id);

            String sql_package_card = "INSERT INTO package_card (package_id, card_id) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql_package_card)) {
                preparedStatement.setObject(1, pi);
                preparedStatement.setObject(2, cid);
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
        UUID uid = null;
        UUID package_id = null;

        //step 1: get user_id from auth
        String sql_get_id = "SELECT * FROM \"user\" WHERE token = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_get_id)) {
            preparedStatement.setString(1, auth);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Process ResultSet
                while (resultSet.next()) {
                    user_id = resultSet.getString("user_id");
                    uid = UUID.fromString(user_id);
                }
            }
        }
        catch (SQLException e) {
            writer.println(404, "user not found");
            return;
        }

        //step 2: check if >= 5 coins
        String sql_check_coins = "SELECT coins FROM \"user\" WHERE user_id = ? AND coins > 4";


        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_check_coins)) {
            preparedStatement.setObject(1, uid);

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
                "SELECT package_id FROM package WHERE user_id IS NULL LIMIT 1) RETURNING package_id";

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
            stmtAssignPackage.setObject(1, uid);

            try (ResultSet rs = stmtAssignPackage.executeQuery()) {
                if (rs.next()) {
                    package_id = (UUID) rs.getObject("package_id");  // Retrieve the package_id as UUID



                } else {
                    writer.println(403, "No available packages to assign.");
                    return;
                }
            }

        } catch (SQLException e) {
            writer.println(400, e.getMessage());
            return;
        }

        // part 5: Assign cards to user

        String sql_assign_user_to_cards = "UPDATE card SET owner = ? WHERE card_id IN (" +
                "SELECT card_id FROM package_card WHERE package_id = ?)";

        try (PreparedStatement stmtUpdateCard = connection.prepareStatement(sql_assign_user_to_cards)) {
            stmtUpdateCard.setObject(1, uid);
            stmtUpdateCard.setObject(2, package_id);
            int rowsUpdated = stmtUpdateCard.executeUpdate();

            if (rowsUpdated == 0) {
                writer.println(403, "Failed to update card owner");
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
    public String GET_uid_from_auth(String auth, MyPrintWriter writer){
        String user_id = "";

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
        }
        return user_id;
    }

    public List<Card> GET_cards(String user_id, MyPrintWriter writer){
        List<Card> cards = new ArrayList<>();
        UUID uid = UUID.fromString(user_id);
        String sql_get_cards = "SELECT * FROM card WHERE owner = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_get_cards)) {
            preparedStatement.setObject(1, uid);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the resultSet contains any rows
                if (!resultSet.next()) {
                    writer.println(404, "user has no cards or user not found");
                    return null;
                }

                // Iterate through the ResultSet
                do {
                    Card c = new Card(resultSet.getString("card_id"), resultSet.getString("name"), resultSet.getFloat("damage"));
                    cards.add(c);
                } while (resultSet.next());  // Continue iterating as long as there are more rows
            }
        }
        catch (SQLException e) {
            writer.println(404, "sql error (get uid)");
            return null;
        }
        return cards;
    }

    public Card GET_card_with_card_id(String card_id, MyPrintWriter writer){
        Card card = null;
        UUID cid = UUID.fromString(card_id);
        String sql_get_card = "SELECT * FROM card WHERE card_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_get_card)) {
            preparedStatement.setObject(1, cid);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the resultSet contains a row
                if (resultSet.next()) {
                    card = new Card(
                            resultSet.getString("card_id"),
                            resultSet.getString("name"),
                            resultSet.getFloat("damage")
                    );
                } else {
                    writer.println(404, "Card not found with ID: " + card_id);
                    return null;
                }
            }
        } catch (SQLException e) {
            // Log the exception, and send a generic error message to the writer
            writer.println(500, "Database error: " + e.getMessage());
            return null;
        }
        return card;
    }

    public Deck GET_deck(String user_id, MyPrintWriter writer){
        UUID uid = UUID.fromString(user_id);
        Deck d = new Deck();

        String sql_get_deck = "SELECT card_id FROM deck WHERE user_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_get_deck)) {
            preparedStatement.setObject(1, uid);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the resultSet contains any rows
                if (!resultSet.next()) {
                    return null;
                }

                // Iterate through the ResultSet
                do {
                    Card c = this.GET_card_with_card_id(resultSet.getString("card_id"), writer);
                    d.addcard(c);
                } while (resultSet.next());  // Continue iterating as long as there are more rows
            }
        }
        catch (SQLException e) {
            writer.println(404, e.getMessage() + " (get deck)");
            return null;
        }
        return d;
    }

    public User GET_users(String user_id, MyPrintWriter writer){
        UUID uid = UUID.fromString(user_id);
        User u = null;
        String sql_get_user = "SELECT * FROM \"user\" WHERE user_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_get_user)) {
            preparedStatement.setObject(1, uid);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the resultSet contains any rows
                if (!resultSet.next()) {
                    writer.println(404, "user not found");
                    return null;
                }
                else {
                    u = new User(resultSet.getString("username"), resultSet.getString("password"), resultSet.getString("name"), resultSet.getString("bio"), resultSet.getString("image"));
                }
            }
        }
        catch (SQLException e) {
            writer.println(404,  e.getMessage() + " (get users)");
            return null;
        }
        return u;
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

    public void PUT_deck(String user_id, String[] content, MyPrintWriter writer){
        UUID uid = UUID.fromString(user_id);
        int rowsAffected = 0;
        //System.out.println("deck_content: " + content.toString());

        String sql_put_deck = "INSERT INTO deck (user_id, card_id) " +
                "SELECT ?, ? " +
                "WHERE NOT EXISTS ( " +
                "  SELECT 1 " +
                "  FROM deck " +
                "  WHERE user_id = ? " +
                "  GROUP BY user_id " +
                "  HAVING COUNT(*) >= 4 " +
                ")";


        for (int i = 0; i < content.length; i++) {
            //System.out.println("card-id: " + content[i]);
            UUID card_id = UUID.fromString(content[i]);

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql_put_deck)) {
                preparedStatement.setObject(1, uid);
                preparedStatement.setObject(2, card_id);
                preparedStatement.setObject(3, uid);
                rowsAffected += preparedStatement.executeUpdate();

            } catch (SQLException e) {
                writer.println(400, e.getMessage() + "(put_deck)");
            }
        }
        if (rowsAffected == 0) {
            writer.println(400, "failed. original: ..."); // todo how to get original ??

        }
        else writer.println(201, "Deck successfully created");
    }

    public void PUT_users(String user_id, String name, String bio, String image, MyPrintWriter writer){
        //System.out.println("name: " + name + " bio: " + bio + " image: " + image);
        UUID uid = UUID.fromString(user_id);
        String sql_update_user = "UPDATE \"user\" SET name = ?, bio = ?, image = ? WHERE user_id = ?";

        try (PreparedStatement stmtUpdateUser = connection.prepareStatement(sql_update_user)) {

            stmtUpdateUser.setString(1, name);
            stmtUpdateUser.setString(2, bio);
            stmtUpdateUser.setString(3, image);
            stmtUpdateUser.setObject(4, uid);
            int rowsUpdated = stmtUpdateUser.executeUpdate();

            if (rowsUpdated == 0) {
                writer.println(403, "Failed to change user information");
                return;
            }
        } catch (SQLException e) {
            writer.println(400, e.getMessage());
            return;
        }
        writer.println(200, "User changed successfully");
    }


    // ------------ DELETE --------------
    public String DELETE_tradings(StringBuilder data, String additional_request){
        return "DELETE_tradings";
    }
}
