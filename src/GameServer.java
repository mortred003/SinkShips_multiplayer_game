//import java.io.IOException;
//import java.net.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class GameServer {
//    private ServerSocket serverSocket;
//    private List<NetworkConnection> connections;
//
//    public GameServer(int port) throws IOException {
//        serverSocket = new ServerSocket(port);
//        connections = new ArrayList<>();
//    }
//
//    public void start() {
//        System.out.println("Server is waiting for connections...");
//        while (connections.size() < 2) {  // Only accept two players
//            try {
//                Socket playerSocket = serverSocket.accept();
//                NetworkConnection connection = new NetworkConnection(playerSocket, this::handleMessage);
//                connections.add(connection);
//                System.out.println("Player " + connections.size() + " connected.");
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        // Game logic to start when both players are connected
//        startGame();
//    }
//
//    private void handleMessage(String message) {
//        // Process messages from both players here
//        System.out.println("Received message: " + message);
//    }
//
//    private void startGame() {
//        // Logic to start the game after both players are connected
//        System.out.println("Both players are connected. Game starting...");
//    }
//
//public class Main {
//    public static void main(String[] args) {
//        try {
//            GameServer server = new GameServer(5555);  // Port 5555 for the server
//            server.start();  // Start the server and wait for connections
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}}
