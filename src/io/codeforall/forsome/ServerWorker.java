package io.codeforall.forsome;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ServerWorker implements Runnable {

    private ChatServer chatServer;


    private String clientName;
    private int clientNumber;
    private Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;

    public ServerWorker(ChatServer chatServer, int identifier, Socket clientSocket) {

        this.chatServer = chatServer;
        this.clientNumber = chatServer.getClientList().size() + 1;
        this.clientName = "Client-" + this.clientNumber;
        this.clientSocket = clientSocket;
        try {
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Unable to open streams.");
        }
    }

    public String getClientName() {
        return clientName;
    }

    @Override
    public void run() {
        //defineClientName(this.identifier);
        while (clientSocket.isBound()) {
            try {
                String clientMessage = in.readLine();
                if (clientMessage == null) {
                    break;
                }
                if (clientMessage.startsWith("/")) {
                    runCommand(clientMessage);
                }

                send(this, clientMessage);

            } catch (IOException e) {
                System.err.println("Unable to receive input");
            }
        }
        try {
            this.in.close();
            this.out.close();
            clientSocket.close();
            System.out.println(this + " has disconnected!");
            this.chatServer.getClientList().remove(this.clientNumber);
        } catch (IOException e) {
            System.err.println("Unable to close streams and/or socket.");
        }
    }

    private void runCommand(String message) {

        switch (message) {
            case "/quit":
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("Unable to close");
                }
                break;
            case "/list":
                List<ServerWorker> serverWorkerList = chatServer.getClientList();
                for (ServerWorker serverWorker : serverWorkerList) {
                    out.println(serverWorker.toString());
                }
        }
    }

    private void send(ServerWorker serverWorker, String clientMessage) {
        chatServer.broadcastMessage(serverWorker, clientMessage);
    }

    public void receive(ServerWorker serverWorker, String message) {
        this.out.print(serverWorker.getClientName() + ": ");
        this.out.println(message);
    }

    @Override
    public String toString() {
        return this.clientName;
    }

    public void defineClientName(int identifier) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        this.out.print("Name: ");
        try {
            String clientName = reader.readLine();
        } catch (IOException e) {
            System.err.println("Unable to obtain input");
        }

        if (clientName != null) {
            this.clientName = clientName;
        }
    }
}
