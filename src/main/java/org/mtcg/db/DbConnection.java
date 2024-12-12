package org.mtcg.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {
    private String connectionString = "jdbc:postgresql://localhost:5432/postgres?user=postgres&password=postgres";// mtcg ist db name

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(connectionString);
    }
}
