import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TicTacToeServer {

    private static String[] board = new String[9];
    private static String turn = "X";
    private static Player playerX;
    private static Player playerO;
    private static volatile boolean gameActive = true;

    public static void main(String[] args) throws Exception {
        int port = 8901;
        
        // Allow custom port via command line
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port number. Using default port 8901.");
            }
        }
        
        ServerSocket listener = new ServerSocket(port);
        System.out.println("Tic Tac Toe Server is Running on port " + port + "...");
        System.out.println("Server will accept multiple games. Press Ctrl+C to stop.");
        
        // Keep server running indefinitely
        while (true) {
            try {
                System.out.println("\n=== Waiting for players to connect for a new game ===");
                
                // Reset game state for new game
                gameActive = true;
                turn = "X";
                
                // Wait for Player X to connect
                playerX = new Player(listener.accept(), "X");
                System.out.println("Player X connected from " + playerX.socket.getInetAddress());
                playerX.output.println("WELCOME X");
                playerX.output.println("MESSAGE Waiting for opponent to connect...");

                // Wait for Player O to connect
                playerO = new Player(listener.accept(), "O");
                System.out.println("Player O connected from " + playerO.socket.getInetAddress());
                playerO.output.println("WELCOME O");
                
                playerX.output.println("MESSAGE Both players connected. Game starting!");
                playerO.output.println("MESSAGE Both players connected. X starts first.");

                // Link players
                playerX.setOpponent(playerO);
                playerO.setOpponent(playerX);

                // Initialize board
                for (int i = 0; i < 9; i++) {
                    board[i] = String.valueOf(i + 1);
                }

                // Start the game threads
                playerX.start();
                playerO.start();
                
                // Give initial turn to Player X
                playerX.output.println(getBoardState());
                playerX.output.println("TURN");

                playerO.output.println(getBoardState());
                playerO.output.println("WAIT");

                // Wait for this game to finish before accepting new players
                playerX.join();
                playerO.join();
                
                System.out.println("Game ended. Ready for next game.");

            } catch (Exception e) {
                System.out.println("Game error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Gets the current board state as a comma-separated string
     */
    public static synchronized String getBoardState() {
        return String.join(",", board);
    }

    /**
     * Checks if a move is valid
     */
    public static synchronized boolean isValidMove(int location, Player player) {
        if (!gameActive) {
            return false;
        }
        
        if (player.playerMark.equals(turn) &&
            location >= 0 && location <= 8 &&
            board[location].equals(String.valueOf(location + 1))) 
        {
            return true;
        }
        return false;
    }

    /**
     * Apply a move to the board
     */
    public static synchronized void applyMove(int location, String mark) {
        board[location] = mark;
        turn = mark.equals("X") ? "O" : "X";
    }

    /**
     * Represents a player as a thread
     */
    static class Player extends Thread {
        Socket socket;
        String playerMark;
        BufferedReader input;
        PrintWriter output;
        Player opponent;
        volatile boolean connected = true;

        public Player(Socket socket, String mark) {
            this.socket = socket;
            this.playerMark = mark;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (Exception e) {
                System.out.println("Player creation failed: " + e);
            }
        }

        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        public void run() {
            try {
                String command;
                while (gameActive && (command = input.readLine()) != null) {
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.split(" ")[1]);
                        
                        if (isValidMove(location, this)) {
                            // Apply move
                            applyMove(location, playerMark);
                            
                            System.out.println("Player " + playerMark + " moved to position " + (location + 1));
                            
                            // Check for winner
                            String winner = checkWinner();
                            if (winner != null) {
                                // Game over
                                gameActive = false;
                                
                                // Send final board state BEFORE game result messages
                                String finalBoard = getBoardState();
                                output.println(finalBoard);
                                if (opponent.connected) {
                                    opponent.output.println(finalBoard);
                                }
                                
                                if (winner.equals("draw")) {
                                    output.println("DRAW");
                                    if (opponent.connected) {
                                        opponent.output.println("DRAW");
                                    }
                                    System.out.println("Game ended in a draw.");
                                } else {
                                    // Send victory/defeat messages based on who won
                                    if (winner.equals(playerMark)) {
                                        output.println("VICTORY " + winner);
                                        if (opponent.connected) {
                                            opponent.output.println("DEFEAT " + winner);
                                        }
                                    } else {
                                        output.println("DEFEAT " + winner);
                                        if (opponent.connected) {
                                            opponent.output.println("VICTORY " + winner);
                                        }
                                    }
                                    System.out.println("Player " + winner + " wins!");
                                }
                                
                                break; // End game
                            } else {
                                // Continue game
                                String currentBoard = getBoardState();
                                
                                output.println(currentBoard);
                                output.println("WAIT");
                                
                                if (opponent.connected) {
                                    opponent.output.println(currentBoard);
                                    opponent.output.println("TURN");
                                }
                            }
                        } else {
                            if (!turn.equals(playerMark)) {
                                output.println("INVALID Not your turn.");
                            } else {
                                output.println("INVALID Invalid move. Position already taken or out of range.");
                            }
                            output.println("TURN"); // Re-issue turn command
                        }
                    }
                }
            } catch (SocketException e) {
                System.out.println("Player " + playerMark + " disconnected.");
            } catch (Exception e) {
                System.out.println("Player " + playerMark + " error: " + e.getMessage());
            } finally {
                connected = false;
                gameActive = false;
                
                // Notify opponent of disconnection
                if (opponent != null && opponent.connected) {
                    opponent.output.println("MESSAGE Opponent disconnected. You win by default!");
                    opponent.output.println("VICTORY " + opponent.playerMark);
                }
                
                try { 
                    socket.close(); 
                } catch (Exception e) {
                    System.out.println("Error closing socket: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Check for winner or draw
     * Returns "X", "O", "draw", or null if game is not over
     */
    static synchronized String checkWinner() {
        // Check all winning combinations
        int[][] winPatterns = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
            {0, 4, 8}, {2, 4, 6}              // Diagonals
        };
        
        for (int[] pattern : winPatterns) {
            String pos1 = board[pattern[0]];
            String pos2 = board[pattern[1]];
            String pos3 = board[pattern[2]];
            
            if (pos1.equals(pos2) && pos2.equals(pos3)) {
                if (pos1.equals("X")) {
                    return "X";
                } else if (pos1.equals("O")) {
                    return "O";
                }
            }
        }

        // Check for draw - all positions filled
        boolean boardFull = true;
        for (int i = 0; i < 9; i++) {
            if (board[i].equals(String.valueOf(i + 1))) {
                boardFull = false;
                break;
            }
        }
        
        if (boardFull) {
            return "draw";
        }
        
        return null; // Game is not over
    }
}