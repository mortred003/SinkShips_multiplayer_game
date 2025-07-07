import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class SinkShipsGUI extends JFrame {
    private JButton[][] playerGrid = new JButton[10][10];
    private JButton[][] attackGrid = new JButton[10][10];
    private boolean[][] playerShips = new boolean[10][10];
    private boolean[][] opponentShips = new boolean[10][10]; //me mbajt opponentShips
    private int shipsPlaced = 0;
    private boolean isPlayerTurn = true; // logjika per radh

    //komunikim me server
    private DataInputStream input;
    private DataOutputStream output;

    //assigned per perdorim permes GUI
    public SinkShipsGUI(DataInputStream input, DataOutputStream output) {
        this.input = input;
        this.output = output;

        setTitle("Sink Ships Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(1, 2));//per 2 grida

        JPanel playerPanel = createLabeledGrid(playerGrid, "Your Ships", true);
        JPanel attackPanel = createLabeledGrid(attackGrid, "Attack Grid", false);

        add(playerPanel);
        add(attackPanel);
        setSize(1000, 700);
        setVisible(true);
        showGameRules();
       // showMe();

        // thread per marrjen e masazheve nga serveri
        new Thread(this::listenForMessages).start();
    }


    private void showGameRules() {
        String rules = """
        Rregullat e Lojes - Sink Ships
        
        1. Secili lojtar ka nje fushe 10x10 dhe vendos 5 anije.
        2. Anijet vendosen duke klikuar ne fushen e majte (Your Ships).
        3. Pasi te jene vendosur te gjitha anijet, loja fillon automatikisht.
        4. Gjate lojes, klikoni ne fushen e djathte (Attack Grid) per te sulmuar kundershtarin.
        5. Lojtari që i shkaterron te gjitha anijet e kundershtarit fiton.

        ~~~~~~~~~~~~~~~~~~~~~~~~~~GL&HF~~~~~~~~~~~~~~~~~~~~~~~~~~
        """;

        JOptionPane.showMessageDialog(this, rules, "Rregullat e Lojes", JOptionPane.INFORMATION_MESSAGE);
    }
    //
    private JPanel createLabeledGrid(JButton[][] grid, String title, boolean isPlayerGrid) {
        String[] rowLabels = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));

        JPanel gridPanel = new JPanel(new GridLayout(11, 11));

        for (int row = 0; row <= 10; row++) {
            for (int col = 0; col <= 10; col++) {
                if (row == 0 && col == 0) {
                    gridPanel.add(new JLabel(""));
                } else if (row == 0) {  //numrat
                    gridPanel.add(new JLabel(String.valueOf(col), SwingConstants.CENTER));
                } else if (col == 0) { //karakteret
                    gridPanel.add(new JLabel(rowLabels[row - 1], SwingConstants.CENTER));
                } else { //ne qeliza tjere kem buttona
                    JButton btn = new JButton();
                    grid[row - 1][col - 1] = btn;
                    gridPanel.add(btn);

                    if (isPlayerGrid) { //vetem per playerGrid
                        final int r = row - 1;
                        final int c = col - 1;

                        btn.addActionListener(e -> {
                            if (shipsPlaced >= 5) {
                                JOptionPane.showMessageDialog(this, "Te gjithe anijet jane pozicionuar");
                                return;
                            }

                            if (playerShips[r][c]) { //kontrollon nese eshte true
                                JOptionPane.showMessageDialog(this, "Pozicioni eshte e zene");
                                return;
                            }

                            playerShips[r][c] = true;
                            btn.setBackground(Color.BLUE);
                            shipsPlaced++;

//                            if (shipsPlaced == 5) {
//                                JOptionPane.showMessageDialog(this, "Te gjithe anijet jane pozicionuar, Gati mu nis");
//                            }
                            if (shipsPlaced == 5) { //za pracane vo server
                                JOptionPane.showMessageDialog(this, "Te gjithe anijet jane pozicionuar, Gati mu nis");
                                sendShipGridToServer();
                            }

                        });
                    } else { //AttackGrid
                        final int r = row - 1;
                        final int c = col - 1;
                        // Perdorimi i AttackGrid
                        btn.addActionListener(e -> {
                            if (isPlayerTurn) { //nese True eshte radha e lojetarit
                                try {
                                    output.writeUTF("Attack " + (char) ('A' + r) + (c + 1)); //mesazhi dergohet ne server
                                    isPlayerTurn = false; // Switch turn after attack
                                    btn.setEnabled(false); // Disable attack button after clicking
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            } else {
                                JOptionPane.showMessageDialog(this, "Nuk eshte radha jote");
                            }
                        });
                    }
                }
            }
        }

        panel.add(gridPanel, BorderLayout.CENTER);
        return panel;
    }
    /*Dergimi i playerGrid te serveri per te ndare me kundershtarin
    * Dergojme duke ndertuar nje string cili permban 1 per anije dhe 0 per qeliza te zbrazta*/
    private void sendShipGridToServer() {
        StringBuilder gridData = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                gridData.append(playerShips[i][j] ? "1" : "0"); //logjika e krijimit te stringut
            }
        }

        try {
            output.writeUTF("ShipGrid " + gridData.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Degjimi e mesazheve qe vijne nga serveri
    private void listenForMessages() {
        try {
            while (true) {
                String message = input.readUTF();
                System.out.println("Received message: " + message);

                String[] parts = message.split(" "); //ndarja e messazheve shembul Hit A1 = [Hit][A1]
                String command = parts[0]; //comanda eshte pjesa e pare Hit, Miss ose OpponentsGrid

                if (command.equals("Hit") || command.equals("Miss")) {
                    //kontrollojme a eshte mesazhi i vlefshem
                    if (parts.length < 2) {
                        System.out.println("Measazhi pavlefshem, Jane ekspektuar koordinata per Hit/Miss.");
                        continue;
                    }

                    String coordinate = parts[1]; //koordinata jane ne array e dyt
                    int row = coordinate.charAt(0) - 'A'; //konvertojm karakterin ne integer
                    int col = Integer.parseInt(coordinate.substring(1)) - 1; //konvertojme ne indeks bazuar me zero

                    if (command.equals("Hit")) {
                        attackGrid[row][col].setBackground(Color.GREEN);
                        attackGrid[row][col].setEnabled(false);
                        opponentShips[row][col] = true;
                    } else if (command.equals("Miss")) {
                        attackGrid[row][col].setBackground(Color.GRAY);
                        attackGrid[row][col].setEnabled(false);
                    }
                } else if (command.equals("OpponentGrid")) {
                    if (parts.length < 2) {
                        System.out.println("Mesazhi pavlefshem, Ekpektuar grid data OpponentGrid");
                        continue;
                    }

                    String gridData = parts[1]; // 100 karaktere qe reprezentojne GRID
                    if (gridData.length() != 100) {
                        System.out.println("Grid data pavlefshme, jane ekspektuar 100 karaktere");
                        continue;
                    }

                    // Updjtimi e Opponents ship Grid
                    /* Perdorim String me 100 karakter si nje matric 10x10*/
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            opponentShips[i][j] = gridData.charAt(i * 10 + j) == '1';
                        }
                    }
                } else if (command.equals("RadhaJote")) {
                    isPlayerTurn = true;
                    JOptionPane.showMessageDialog(this, "Eshte radha jote per me sulmu");
                } else if (command.equals("Prit")) {
                    isPlayerTurn = false;
                    JOptionPane.showMessageDialog(this, "Eshte radha e kundershtarit!");
                }else if (command.equals("Win")) {
                    JOptionPane.showMessageDialog(this, "You win!");
                    isPlayerTurn = false;
                } else if (command.equals("Lose")) {
                    JOptionPane.showMessageDialog(this, "You lost.");
                    isPlayerTurn = false;
                } else {
                    System.out.println("Mesazhi pavlefshem, komanda " + command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try {
            Socket socket = new Socket("127.0.0.1", 5555);
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            SwingUtilities.invokeLater(() -> new SinkShipsGUI(input, output));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}






/**
 * +==============================================================+
 * | __  __      _                        _      _    _         _ |
 * ||  \/  | ___| |__  _ __ ___   ___  __| |    / \  | |__   __| ||
 * || |\/| |/ _ \ '_ \| '_ ` _ \ / _ \/ _` |   / _ \ | '_ \ / _` ||
 * || |  | |  __/ | | | | | | | |  __/ (_| |  / ___ \| |_) | (_| ||
 * ||_|  |_|\___|_| |_|_| |_| |_|\___|\__,_| /_/   \_\_.__/ \__,_||
 * +==============================================================+
 */






