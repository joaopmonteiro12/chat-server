package io.codeforall.forsome;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {

    private ServerSocket serverSocket;
    private final static int DEFAULT_PORT = 8085;
    private ExecutorService cachedPool;
    private List<ServerWorker> clientList = Collections.synchronizedList(new LinkedList<>());
    private int numberOfClients = 0;

    public void setCachedPool(ExecutorService cachedPool) {
        this.cachedPool = cachedPool;
    }

    public ExecutorService getCachedPool() {
        return cachedPool;
    }

    public List<ServerWorker> getClientList() {
        return clientList;
    }
    public static void main(String[] args) {

        try {

            int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

            ChatServer chatServer = new ChatServer();
            chatServer.setCachedPool(Executors.newCachedThreadPool());
            chatServer.listen(port);
            chatServer.getCachedPool().shutdown();

        } catch (NumberFormatException e) {
            System.err.println("Usage: ChatServer [PORT]");
            System.exit(1);
        }

    }

    private void listen(int port) {

        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("Server bind to " + getAddress());
            connectClient(serverSocket);

        } catch (IOException e) {
            System.err.println("Could not bind to port " + port);
        }

    }

    private void connectClient(ServerSocket serverSocket) {

        while (true) {
            try {
                ++this.numberOfClients;
                Socket clientSocket = serverSocket.accept();
                System.out.println("LIGOU");


                ServerWorker serverWorker = new ServerWorker(this, this.numberOfClients, clientSocket);
                this.clientList.add(serverWorker);
                this.cachedPool.submit(serverWorker);

            } catch (IOException e) {
                System.err.println("XX");
            }

        }

    }

    public void broadcastMessage(ServerWorker worker, String message) {

        System.out.println(worker.toString() + ": " + message);
        for (ServerWorker serverWorker : clientList) {
            if (serverWorker == worker) {
                continue;
            }
            serverWorker.receive(worker,message);
        }

    }

    private String getAddress() {
        if (serverSocket == null) {
            return null;
        }
        return serverSocket.getInetAddress().getHostAddress() + ":" + serverSocket.getLocalPort();
    }

}
