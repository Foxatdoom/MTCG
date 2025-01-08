package org.mtcg.db;

import org.mtcg.Model.Card;
import org.mtcg.Model.Deck;
import org.mtcg.Model.Stack;
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
    }

    // for junit tests
    public DbAccess(Connection connection) {
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
    }

    // ---------------------------------- USING DB ---------------------------------
    // ------------ POST --------------

    public void POST_users(User user, MyPrintWriter writer){ //aka register

        String username = user.getUsername();
        String password = user.getPassword();
        String token = user.getToken();

        // Inserting User
        String sql = "INSERT INTO \"user\" (username, password, token) VALUES (?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, token);
            preparedStatement.executeUpdate();
            writer.println(201,"User created");

        } catch (SQLException e) {
            if (e.getMessage().startsWith("ERROR: duplicate key")) {

                writer.println(409, "User already exists");
            }
            else {
                writer.println(400, "Login Failed");
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
            writer.println(400,e.getMessage() + " (by inserting package)");
            return;
        }

        String name = "";
        float damage = 0f;
        String element_type = "";
        String card_type = "";

        // creating 5 cards
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
                writer.println(400,e.getMessage() + " (by inserting cards)");
                return;
            }

            UUID pi = UUID.fromString(package_id);

            // add these cards to the package
            String sql_package_card = "INSERT INTO package_card (package_id, card_id) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql_package_card)) {
                preparedStatement.setObject(1, pi);
                preparedStatement.setObject(2, cid);
                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                writer.println(400,e.getMessage() + " (by inserting package_cards)");
                return;
            }
        }
        writer.println(201, "Package added");
    }

    public void POST_transactions(String user_id, MyPrintWriter writer) {
        UUID uid = UUID.fromString(user_id);
        UUID package_id = null;

        try {
            connection.setAutoCommit(false); // Begin transaction

            // Step 1: Check if user has >= 5 coins
            String sql_check_coins = "SELECT coins FROM \"user\" WHERE user_id = ? AND coins > 4";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql_check_coins)) {
                preparedStatement.setObject(1, uid);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        writer.println(403, "Not enough money");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Step 2: Check if packages are available
            String sql_check_packages = "SELECT COUNT(*) FROM package WHERE user_id IS NULL";
            try (PreparedStatement packageStmt = connection.prepareStatement(sql_check_packages)) {
                try (ResultSet rs = packageStmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        writer.println(403, "No packages available");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Step 3: Decrease user's coins and assign a package
            String sql_decrease_coins = "UPDATE \"user\" SET coins = coins - 5 WHERE user_id = ?";
            String sql_assign_package = "UPDATE package SET user_id = ? WHERE package_id = (" +
                    "SELECT package_id FROM package WHERE user_id IS NULL LIMIT 1) RETURNING package_id";

            try (PreparedStatement stmtDecreaseCoins = connection.prepareStatement(sql_decrease_coins);
                 PreparedStatement stmtAssignPackage = connection.prepareStatement(sql_assign_package)) {

                stmtDecreaseCoins.setObject(1, uid);
                if (stmtDecreaseCoins.executeUpdate() == 0) {
                    writer.println(403, "Failed to decrease coins.");
                    connection.rollback();
                    return;
                }

                stmtAssignPackage.setObject(1, uid);
                try (ResultSet rs = stmtAssignPackage.executeQuery()) {
                    if (rs.next()) {
                        package_id = (UUID) rs.getObject("package_id");
                    } else {
                        writer.println(403, "No available packages to assign.");
                        connection.rollback();
                        return;
                    }
                }
            }

            // Step 4: Assign cards to user
            String sql_assign_user_to_cards = "UPDATE card SET owner = ? WHERE card_id IN (" +
                    "SELECT card_id FROM package_card WHERE package_id = ?)";
            try (PreparedStatement stmtUpdateCard = connection.prepareStatement(sql_assign_user_to_cards)) {
                stmtUpdateCard.setObject(1, uid);
                stmtUpdateCard.setObject(2, package_id);
                if (stmtUpdateCard.executeUpdate() == 0) {
                    writer.println(403, "Failed to update card owner.");
                    connection.rollback();
                    return;
                }
            }

            connection.commit(); // Commit transaction
            writer.println(201, "Package successfully bought");

        } catch (SQLException e) {
            try {
                connection.rollback(); // Roll back transaction on error
            } catch (SQLException rollbackEx) {
                writer.println(500, "Rollback failed: " + rollbackEx.getMessage());
            }
            writer.println(400, e.getMessage());
        } finally {
            try {
                connection.setAutoCommit(true); // Reset auto-commit
            } catch (SQLException e) {
                writer.println(500, "Failed to reset auto-commit: " + e.getMessage());
            }
        }
    }

    public String[] POST_battles(String user_id, MyPrintWriter writer) {
        UUID uid = UUID.fromString(user_id);
        UUID user_1 = null;

        String[] info = new String[3];

        // Check if user is already in a battle
        String sql_check_if_user_in_battle = "SELECT user_1 FROM battle WHERE user_1 IS NOT NULL AND user_2 IS NULL";
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_check_if_user_in_battle)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    user_1 = (UUID) resultSet.getObject("user_1");
                }
            }
        } catch (SQLException e) {
            writer.println(400, "Error checking user in battle");
            return null; // Exit early on error
        }

        if (user_1 == null) {
            // Create a new battle
            String sql = "INSERT INTO battle (user_1) VALUES (?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setObject(1, uid);
                preparedStatement.executeUpdate();
                writer.println(201, "Battle created. Searching for Player...");
                info[0] = "false";
            } catch (SQLException e) {
                writer.println(400, "Battle creation Failed");
                return null;
            }
        } else {
            // Update existing battle with user_2
            String sql = "UPDATE battle SET user_2 = ? WHERE user_1 = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setObject(1, uid);
                preparedStatement.setObject(2, user_1);
                preparedStatement.executeUpdate();
                writer.println(200, "Battle can start");

                info[0] = "true";
                info[1] = user_1.toString();
                info[2] = uid.toString();
            } catch (SQLException e) {
                writer.println(400, "Battle start Failed");
                return null;
            }
        }
        return info;
    }


    public void POST_battles_setStats(String user_id, int elo_changes, MyPrintWriter writer){
        UUID uid = UUID.fromString(user_id);

        String sql_update_user = "UPDATE \"user\" SET elo = elo + ?, games_played = games_played+1 WHERE user_id = ?";

        try (PreparedStatement stmtUpdateUser = connection.prepareStatement(sql_update_user)) {

            stmtUpdateUser.setInt(1, elo_changes);
            stmtUpdateUser.setObject(2, uid);
            int rowsUpdated = stmtUpdateUser.executeUpdate();

            if (rowsUpdated == 0) {
                writer.println(403, "Failed to change user stats");
                return;
            }
        } catch (SQLException e) {
            writer.println(400, e.getMessage());
            return;
        }
        //writer.println(200, "Stats changed successfully");

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

    public Stack GET_cards(String user_id, MyPrintWriter writer){
        Stack s = new Stack();
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

                // Iterate through ResultSet
                do {
                    Card c = new Card(resultSet.getString("card_id"), resultSet.getString("name"), resultSet.getFloat("damage"));
                    s.addCard(c);
                } while (resultSet.next());  // Continue iterating as long as there are more rows
            }
        }
        catch (SQLException e) {
            writer.println(400, "sql error (get uid)");
            return null;
        }
        return s;
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
            writer.println(400,  e.getMessage() + " (get users)");
            return null;
        }
        return u;
    }

    public void GET_stats(String user_id, MyPrintWriter writer){
        UUID uid = UUID.fromString(user_id);

        String sql_get_elo = "SELECT elo, games_played FROM \"user\" WHERE user_id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_get_elo)) {
            preparedStatement.setObject(1, uid);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Check if the resultSet contains any rows
                if (!resultSet.next()) {
                    writer.println(404, "user not found");
                }
                else {
                    writer.println(200, "Elo found", "[\"elo\":" + resultSet.getString("elo") + ", \"games_played:\":" + resultSet.getString("games_played") + "]");
                }
            }
        }
        catch (SQLException e) {
            writer.println(400,  e.getMessage() + " (get elo)");
        }
    }

    public void GET_scoreboard(MyPrintWriter writer){
        String sql_get_scoreboard = "SELECT elo, username FROM \"user\" ORDER BY elo DESC";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql_get_scoreboard)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                // Build JSON string manually
                StringBuilder jsonBuilder = new StringBuilder();
                jsonBuilder.append("[");
                boolean first = true;

                while (resultSet.next()) {
                    if (!first) {
                        jsonBuilder.append(",");
                    } else {
                        first = false;
                    }

                    jsonBuilder.append("{")
                            .append("\"elo\":").append(resultSet.getInt("elo")).append(",")
                            .append("\"username\":\"").append(resultSet.getString("username")).append("\"")
                            .append("}");
                }

                jsonBuilder.append("]");
                String jsonString = jsonBuilder.toString();

                writer.println(200, "Scoreboard found", jsonString);
            }
        }
        catch (SQLException e) {
            writer.println(400,  e.getMessage() + " (get elo)");
            return;
        }
    }

    public String GET_tradings(StringBuilder data, String additional_request){
        return "GET_tradings";
    }

    // ------------ PUT --------------
    public void PUT_deck(String user_id, String[] content, MyPrintWriter writer){
        UUID uid = UUID.fromString(user_id);
        int rowsAffected = 0;

        // inserted into deck if the user not already have 4 cards assigned to his deck
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
            writer.println(403, "failed. original: ...");
        }
        else writer.println(201, "Deck successfully created");
    }

    public void PUT_users(String user_id, String name, String bio, String image, MyPrintWriter writer){
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
