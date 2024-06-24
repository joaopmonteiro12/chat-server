package io.codeforall.forsome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client {

    public static void main(String[] args) {

        ExecutorService singleExecutor = Executors.newSingleThreadExecutor();
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));



        try {
            System.out.print("Host: ");
            String hostName = reader.readLine();
            System.out.print("Server port: ");
            int serverPort = Integer.parseInt(reader.readLine());
            Socket clientSocket = new Socket(hostName,serverPort);

            if (clientSocket != null) {
                System.out.println("Welcome to the chat!");
            }

            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            Runnable receiveMessages = () -> {
                try {
                    String serverMessage = in.readLine();
                    System.out.println(serverMessage);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            };

            while(clientSocket.isBound()){
                System.out.print("You: ");
                String message = reader.readLine();
                out.println(message);

                singleExecutor.submit(receiveMessages);

                if(clientSocket == null){
                    break;
                }

            }

            singleExecutor.close();
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e){
            System.err.println("Unable to obtain input");
        }

    }


}
