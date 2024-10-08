package org.mtcg.server;

import org.mtcg.db.DbAccess;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class HttpServer {
    public static void main(String[] args) {

        int port = 10001; // The port you want your server to listen on

        try (ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Server is listening on port " + port);

            while (true) { // The Server is listening to Client requests
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start(); // socket starts when client gets and accepts a request
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    DbAccess dba;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input)); // -> curl script

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true)
        ) {

            // Request info
            StringBuilder info = new StringBuilder();
            String line;
            while (!(line = reader.readLine()).isBlank()) {
                info.append(line).append("\n");
            }

            // Request Content
            StringBuilder content = new StringBuilder();
            while (reader.ready()) {
                content.append((char) reader.read());
            }

            // Find the first '/' and the first space after it
            int start = info.indexOf("/") + 1;  // Start just after the first '/'
            int end = info.indexOf(" ", start);  // Find the first space after the '/'

            // Extract the substring
            String client_request = info.substring(start, end);

            String[] requestParts = client_request.split("[/?]"); // for requests like "transactions/packages" and "deck?format=plain"

            dba = new DbAccess();

            String response_to_client = "";

            //System.out.println("request: "+requestParts[0]);

            switch(requestParts[0]){
                case "users": //aka player

                    if(requestParts.length == 2){ // for showing current user

                    }
                    else { // creating user
                        String db_response = dba.createUser(content);
                        System.out.println(db_response);
                        response_to_client = db_response;
                    }
                    break;

                case "sessions":
                    break;
                case "packages":
                    break;
                case "transactions":
                    break;
                case "cards":
                    break;
                case "deck":
                    break;
                case "stats":
                    break;
                case "scoreboard":
                    break;
                case "battles":
                    break;
                case "tradings":
                    break;
                default:
                    response_to_client = "404 Not Found";
                    break;
            }

            // Print request information
            //System.out.println("Request info:" + info + "\n");
            //System.out.println("Request Content:" + content + "\n");

            writer.println(response_to_client); // Anser to the Client

        } catch (Exception e) {
            System.out.println("\nAn error occurred :(\n");
            e.printStackTrace();
        } finally {
            try {
                if (dba != null) {
                    dba.close();
                }
                socket.close();
                //System.out.println("Socket closed");
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}