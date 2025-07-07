import java.io.*;
import java.net.*;
import java.util.*;

public class SinkShipsServer {
    private static List<Player> players = new ArrayList<>();
    private static boolean isGameStarted = false;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(5555);
        System.out.println("Serveri startoj, presim lojetaret!!");

        // Accept two players to start the game
        while (players.size() < 2) {
            Socket playerSocket = serverSocket.accept();
            Player player = new Player(playerSocket); //krijohet per çdo lojtar qe lidhet me serverin
            players.add(player);
            System.out.println("Player " + players.size() + " connected.");
        }

        // Startimi i lojes
        isGameStarted = true;

        // Mesazhet e para per lojetaret
        players.get(0).output.writeUTF("RadhaJote");
        players.get(1).output.writeUTF("Prit");

        // Dergimi i PlayerGrid tek lojetari tjeter
        sendShipGridsToOpponent();

        // Koordinatat e sulmeve
        while (isGameStarted) {
            for (Player currentPlayer : players) { // Cdo here nje lojetari eshte current Player
                String attackMessage = currentPlayer.input.readUTF();
                String[] messageParts = attackMessage.split(" ");
                String action = messageParts[0];//pjesa e pare eshte Hit or Miss
                String coordinates = messageParts[1];

                //Procesimi
                boolean hit = processAttack(currentPlayer, coordinates);

                // Dergimi i rezultatit te lojetari i perkates
                if (hit) {
                    currentPlayer.output.writeUTF("Hit " + coordinates);
                } else {
                    currentPlayer.output.writeUTF("Miss " + coordinates);
                }

                // Radha kalohet tek lojetari tjeter
                Player opponent = players.get(1 - players.indexOf(currentPlayer));// lojetaret jan me index 0 ose 1 dhe perdorim logjken 1-0 ose 1-1 per kalimin e radhes
                currentPlayer.output.writeUTF("Prit");
                opponent.output.writeUTF("RadhaJote");
            }
        }

        serverSocket.close();
    }

    // Dergimi i playerGrid tek kundershtari
    private static void sendShipGridsToOpponent() {
        for (int i = 0; i < players.size(); i++) {
            Player currentPlayer = players.get(i);
            Player opponent = players.get(1 - i);

            //logjigka si ne SinkShipsGUI krijojme nje Strinng me 100 karakter
            StringBuilder gridMessage = new StringBuilder();
            for (int row = 0; row < 10; row++) {
                for (int col = 0; col < 10; col++) {
                    gridMessage.append(opponent.playerShips[row][col] ? "1" : "0");
                }
            }
            try { // Dergimi e gridit
                currentPlayer.output.writeUTF("OpponentGrid " + gridMessage.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // procesin e sulmit dhe kontrollin nese nje sulm ështe i suksesshem hit apo mis
    // perfshin edhe logjiken e mbylljes se lojes nese njeri nga lojtaret e ka humbur të gjitha anijet
    private static boolean processAttack(Player attacker, String coordinates) {
        Player opponent = players.get(1 - players.indexOf(attacker));
        int row = coordinates.charAt(0) - 'A'; // marrja e vleres numerike e karakterit per rresht
        int col = Integer.parseInt(coordinates.substring(1)) - 1; //marrja e vleres per kolon

        if (opponent.playerShips[row][col]) { //nese ka anije ne pozita eshte true
            opponent.playerShips[row][col] = false; //pas sulmit kjo blere behet false

            // kontrollimi i numrit te anijeve
            if (opponent.getRemainingShips() == 0) {
                try {
                    attacker.output.writeUTF("Win");
                    opponent.output.writeUTF("Lose");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isGameStarted = false; // Ndalim lojen
            }

            return true;
        } else {
            return false;
        }
    }
    //klasa per krijimin e lojetarit
    static class Player {
        Socket socket;
        DataInputStream input;
        DataOutputStream output;
        boolean[][] playerShips = new boolean[10][10]; //grid 10x10 qe permban vlera true ose false

        public Player(Socket socket) throws IOException {
            this.socket = socket;
            this.input = new DataInputStream(socket.getInputStream());
            this.output = new DataOutputStream(socket.getOutputStream());
            String shipGridMessage = input.readUTF(); //leximi i mesazhit qe permban gridin e anijeve
            if (shipGridMessage.startsWith("ShipGrid ")) {
                String grid = shipGridMessage.substring(9); // Remove prefix, marrim vetem 100 karakter
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        this.playerShips[i][j] = grid.charAt(i * 10 + j) == '1';//pozita e duhur e karakterit prej grid 10x10 ne string 100 karakter
                    }

            }   System.out.println("Received ship grid from player.");
        } else {
            System.out.println("Invalid ship grid message from player.");
        }

        }  public int getRemainingShips(){
            int count = 0;
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    if (playerShips[i][j]) count++;
                }
            }
            return count;
        }

//
//        public void setShip(int row, int col) {
//            playerShips[row][col] = true;
        }
    }

