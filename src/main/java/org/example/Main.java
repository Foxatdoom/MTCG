package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        DbConnection dbConnection = new DbConnection();

        try {
            // Verbindung zur Datenbank herstellen
            Connection connection = dbConnection.connect();
            System.out.println("Datenbankverbindung erfolgreich hergestellt!");

            // Hier kannst du die Verbindung verwenden, z.B. SQL-Befehle ausführen

            // Verbindung schließen, wenn fertig
            connection.close();
        } catch (SQLException e) {
            System.out.println("Fehler bei der Verbindung zur Datenbank: " + e.getMessage());
        }


    }
}