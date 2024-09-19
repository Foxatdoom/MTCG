package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

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

                // necessary?
                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true)
        ) {
            StringBuilder request = new StringBuilder();
            String line;
            while (!(line = reader.readLine()).isBlank()) {
                request.append(line).append("\n");
            }

            // Read the request body
            StringBuilder body = new StringBuilder();
            while (reader.ready()) {
                body.append((char) reader.read());
            }


            // Print request information
            System.out.println("Request received:\n" + request);
            System.out.println("Request body:\n" + body);

            // ----------------- Access to db -----------------------------
            dba = new DbAccess();
            String cu = dba.createUser();
            System.out.println(cu);


            //String response = "to user?";
            //writer.println(response);

        } catch (IOException e) {
            e.printStackTrace();
        } finally { // closes socket + db connection
            try {
                dba.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}