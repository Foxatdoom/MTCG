package org.mtcg.server;

import org.mtcg.db.DbAccess;
import org.mtcg.handler.ResponseHandler;
import org.mtcg.handler.Router;

import java.io.*;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;

public class HttpServer {
    public static void main(String[] args) {

        int port = 10001;

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            serverSocket.setReuseAddress(true); // Port sofort wiederverwendbar machen
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class ClientHandler implements Runnable {
    private final Socket socket;
    DbAccess dba;
    Router r;
    String response_to_client = "";
    ResponseHandler rh;

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





            boolean use_corrected_project = true;




            if(use_corrected_project){
                r = new Router();
                response_to_client =  r.call_handler(info, content);


                rh = new ResponseHandler(writer);
                rh.respond(response_to_client);

            }

            else {

                // Find the first '/' and the first space after it
                int start = info.indexOf("/") + 1;  // Start just after the first '/'
                int end = info.indexOf(" ", start);  // Find the first space after the '/'
                int extra = info.indexOf(" ");

                // Extract the substring
                String client_request_1 = info.substring(0, extra); // post,get,...
                String client_request_2 = info.substring(start, end); // /users,/deck,...

                //System.out.println(client_request_1);
                String[] requestParts = client_request_2.split("[/?]"); // for requests like "transactions/packages" and "deck?format=plain"


                dba = new DbAccess();



                // dynamically calling functions
                Class<?>[] paramTypes = {StringBuilder.class, String.class};
                String db_method = client_request_1 + "_" + requestParts[0];
                //System.out.println(db_method);

                try {
                    Method method = DbAccess.class.getMethod(db_method, paramTypes);

                    Object[] methodArgs = {content, requestParts.length > 1 ? requestParts[1] : null};

                    response_to_client = (String) method.invoke(dba, methodArgs);
                } catch (NoSuchMethodException e) {
                    response_to_client = "404 Not Found";
                } catch (IllegalAccessException e) {
                    response_to_client = "403 Access denied";
                }


                // Print request information
                //System.out.println("Request info:" + info + "\n");
                //System.out.println("Request Content:" + content + "\n");
                System.out.println(response_to_client);
                writer.println(response_to_client); // Anser to the Client

            }

        } catch (Exception e) {
            System.out.println("\nAn error occurred :(");
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