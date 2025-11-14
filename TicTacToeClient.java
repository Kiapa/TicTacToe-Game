import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class TicTacToeClient {

    private static String[] board = new String[9];
    private static String myMark = "";
    private static boolean gameActive = true;

    static void printBoard() {
        System.out.println();
        System.out.println(" |---|---|---| ");
        System.out.println(" | " + board[0] + " | " + board[1] + " | " + board[2] + " | ");
        System.out.println(" |-----------| ");
        System.out.println(" | " + board[3] + " | " + board[4] + " | " + board[5] + " | ");
        System.out.println(" |-----------| ");
        System.out.println(" | " + board[6] + " | " + board[7] + " | " + board[8] + " | ");
        System.out.println(" |---|---|---| ");
        System.out.println();
    }

    /**
     * Updates the local board array from a server message
     */
    static void updateBoard(String boardState) {
        String[] parts = boardState.split(",");
        if (parts.length == 9) {
            for (int i = 0; i < 9; i++) {
                board[i] = parts[i];
            }
        }
    }

    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // Default: localhost
        int port = 8901; // Default port
        
        Scanner sc = new Scanner(System.in);
        
        System.out.println("=== Tic Tac Toe Client ===");
        System.out.println("Enter Server IP Address (press Enter for localhost 127.0.0.1):");
        String ipInput = sc.nextLine().trim();
        if (!ipInput.isEmpty()) {
            serverAddress = ipInput;
        }
        
        System.out.println("Enter Server Port (press Enter for default 8901):");
        String portInput = sc.nextLine().trim();
        if (!portInput.isEmpty()) {
            try {
                port = Integer.parseInt(portInput);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port. Using default port 8901.");
                port = 8901;
            }
        }

        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            System.out.println("Connecting to " + serverAddress + ":" + port + "...");
            socket = new Socket(serverAddress, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to server. Waiting for game to start...");

            String line;
            while (gameActive && (line = in.readLine()) != null) {
                
                if (line.startsWith("WELCOME")) {
                    myMark = line.split(" ")[1];
                    System.out.println("\n=== You are Player " + myMark + " ===");
                    
                    // Initialize empty board for display
                    for (int i = 0; i < 9; i++) {
                        board[i] = String.valueOf(i + 1);
                    }
                    
                } else if (line.startsWith("MESSAGE")) {
                    System.out.println(">>> " + line.substring(8));
                    
                } else if (line.matches("[XO0-9, ]+") && line.contains(",")) {
                    // Board state update - update immediately and check next command
                    updateBoard(line);
                    
                } else if (line.startsWith("TURN")) {
                    printBoard();
                    System.out.println("=== YOUR TURN ===");
                    System.out.print("Enter a slot number (1-9): ");
                    
                    int numInput = -1;
                    boolean validInput = false;
                    
                    while (!validInput) {
                        try {
                            if (sc.hasNextInt()) {
                                numInput = sc.nextInt();
                                sc.nextLine(); // Clear buffer
                                
                                if (numInput >= 1 && numInput <= 9) {
                                    validInput = true;
                                    out.println("MOVE " + (numInput - 1)); // Send 0-indexed move
                                } else {
                                    System.out.print("Invalid input. Enter a number between 1-9: ");
                                }
                            } else {
                                sc.nextLine(); // Clear invalid input
                                System.out.print("Invalid input. Enter a number between 1-9: ");
                            }
                        } catch (Exception e) {
                            System.out.print("Error reading input. Try again: ");
                            sc.nextLine(); // Clear buffer
                        }
                    }

                } else if (line.startsWith("WAIT")) {
                    printBoard();
                    System.out.println("\n>>> Waiting for opponent's move...");
                    
                } else if (line.startsWith("VICTORY")) {
                    System.out.println("\n╔═══════════════════════════════╗");
                    System.out.println("║   🎉 CONGRATULATIONS! 🎉      ║");
                    System.out.println("║         YOU WON!              ║");
                    System.out.println("╚═══════════════════════════════╝\n");
                    gameActive = false;
                    break;
                    
                } else if (line.startsWith("DEFEAT")) {
                    String winner = line.split(" ")[1];
                    System.out.println("\n╔═══════════════════════════════╗");
                    System.out.println("║      Game Over - You Lost     ║");
                    System.out.println("║   Player " + winner + " wins the game!    ║");
                    System.out.println("╚═══════════════════════════════╝\n");
                    gameActive = false;
                    break;
                    
                } else if (line.startsWith("DRAW")) {
                    System.out.println("\n╔═══════════════════════════════╗");
                    System.out.println("║      Game Over - Draw!        ║");
                    System.out.println("║     Well played both!         ║");
                    System.out.println("╚═══════════════════════════════╝\n");
                    gameActive = false;
                    break;
                    
                } else if (line.startsWith("INVALID")) {
                    System.out.println(">>> " + line);
                }
            }
            
            if (!gameActive) {
                System.out.println("Thanks for playing!");
            }
            
        } catch (SocketException e) {
            System.out.println("\n>>> Connection lost: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("\n>>> Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (sc != null) {
                    sc.close();
                }
                System.out.println("Disconnected from server.");
            } catch (Exception e) {
                System.out.println("Error during cleanup: " + e.getMessage());
            }
        }
    }
}