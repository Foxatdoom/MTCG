package org.mtcg.server;

import org.mtcg.MyPrintWriter;
import org.mtcg.db.DbAccess;
import org.mtcg.handler.Router;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpServer {
    private static final int PORT = 10001;
    private static final int THREAD_POOL_SIZE = 10; // Number of threads in the pool

    public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE); // Create fixed thread pool

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setReuseAddress(true); // Make the port reusable immediately
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                threadPool.execute(new ClientHandler(clientSocket)); // Submit task to thread pool
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            threadPool.shutdown(); // Shut down the thread pool when done
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    DbAccess dba;
    Router r;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input)); // -> curl script

                OutputStream output = socket.getOutputStream();
                MyPrintWriter writer = new MyPrintWriter(output, true)
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

           // processing request
            dba = new DbAccess();
            r = new Router(dba);
            r.call_handler(info, content, writer);

        } catch (Exception e) {
            System.out.println("\nAn error occurred :(");
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                dba.close();
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}