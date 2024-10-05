package org.mtcg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private String connectionString = "jdbc:postgresql://localhost:5432/mtcg_db?user=postgres&password=admin";// mtcg_db ist db name

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(connectionString);
    }
}
