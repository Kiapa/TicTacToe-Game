import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;

public class TicTacToeGUI extends JFrame {
    
    private JButton[] buttons = new JButton[9];
    private JLabel statusLabel;
    private JTextArea messageArea;
    private String myMark = "";
    private boolean myTurn = false;
    private boolean gameActive = true;
    
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    private static final Color BACKGROUND_COLOR = new Color(44, 62, 80);
    private static final Color BUTTON_COLOR = new Color(52, 73, 94);
    private static final Color BUTTON_HOVER = new Color(71, 85, 105);
    private static final Color X_COLOR = new Color(231, 76, 60);
    private static final Color O_COLOR = new Color(52, 152, 219);
    private static final Color TEXT_COLOR = new Color(236, 240, 241);
    
    public TicTacToeGUI() {
        setTitle("Tic Tac Toe - Multiplayer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        initializeUI();
        connectToServer();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BACKGROUND_COLOR);
        
        // Status panel at top
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusLabel = new JLabel("Connecting to server...");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 18));
        statusLabel.setForeground(TEXT_COLOR);
        statusPanel.add(statusLabel);
        add(statusPanel, BorderLayout.NORTH);
        
        // Game board panel (3x3 grid)
        JPanel boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3, 10, 10));
        boardPanel.setBackground(BACKGROUND_COLOR);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        for (int i = 0; i < 9; i++) {
            final int index = i;
            buttons[i] = new JButton(String.valueOf(i + 1));
            buttons[i].setFont(new Font("Arial", Font.BOLD, 48));
            buttons[i].setFocusPainted(false);
            buttons[i].setBackground(BUTTON_COLOR);
            buttons[i].setForeground(Color.LIGHT_GRAY);
            buttons[i].setBorder(BorderFactory.createLineBorder(BACKGROUND_COLOR, 2));
            buttons[i].setPreferredSize(new Dimension(120, 120));
            
            // Hover effect
            buttons[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (buttons[index].isEnabled() && myTurn) {
                        buttons[index].setBackground(BUTTON_HOVER);
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    if (buttons[index].isEnabled()) {
                        buttons[index].setBackground(BUTTON_COLOR);
                    }
                }
            });
            
            buttons[i].addActionListener(e -> makeMove(index));
            buttons[i].setEnabled(false);
            boardPanel.add(buttons[i]);
        }
        
        add(boardPanel, BorderLayout.CENTER);
        
        // Message area at bottom
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setBackground(BACKGROUND_COLOR);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        messageArea = new JTextArea(5, 30);
        messageArea.setEditable(false);
        messageArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        messageArea.setBackground(new Color(33, 47, 61));
        messageArea.setForeground(TEXT_COLOR);
        messageArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        JScrollPane scrollPane = new JScrollPane(messageArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(BACKGROUND_COLOR, 2));
        messagePanel.add(scrollPane, BorderLayout.CENTER);
        
        add(messagePanel, BorderLayout.SOUTH);
        
        pack();
    }
    
    private void connectToServer() {
        String serverAddress = JOptionPane.showInputDialog(
            this,
            "Enter Server IP Address:",
            "Connect to Server",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (serverAddress == null || serverAddress.trim().isEmpty()) {
            serverAddress = "127.0.0.1";
        }
        
        String portStr = JOptionPane.showInputDialog(
            this,
            "Enter Server Port:",
            "Connect to Server",
            JOptionPane.QUESTION_MESSAGE
        );
        
        int port = 8901;
        if (portStr != null && !portStr.trim().isEmpty()) {
            try {
                port = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                addMessage("Invalid port. Using default 8901.");
            }
        }
        
        final String finalAddress = serverAddress;
        final int finalPort = port;
        
        new Thread(() -> {
            try {
                addMessage("Connecting to " + finalAddress + ":" + finalPort + "...");
                socket = new Socket(finalAddress, finalPort);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);
                
                addMessage("Connected! Waiting for game to start...");
                
                String line;
                while (gameActive && (line = in.readLine()) != null) {
                    processServerMessage(line);
                }
                
            } catch (Exception e) {
                addMessage("Error: " + e.getMessage());
                JOptionPane.showMessageDialog(this, 
                    "Failed to connect to server: " + e.getMessage(),
                    "Connection Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }
    
    private void processServerMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            if (message.startsWith("WELCOME")) {
                myMark = message.split(" ")[1];
                statusLabel.setText("You are Player " + myMark);
                addMessage("=== You are Player " + myMark + " ===");
                
            } else if (message.startsWith("MESSAGE")) {
                String msg = message.substring(8);
                addMessage(">>> " + msg);
                
            } else if (message.matches("[XO0-9, ]+") && message.contains(",")) {
                // Board state update
                updateBoard(message);
                
            } else if (message.startsWith("TURN")) {
                myTurn = true;
                statusLabel.setText("Your Turn, Player " + myMark + "!");
                statusLabel.setForeground(new Color(46, 204, 113));
                enableButtons(true);
                addMessage("=== YOUR TURN ===");
                
            } else if (message.startsWith("WAIT")) {
                myTurn = false;
                statusLabel.setText("Waiting for opponent...");
                statusLabel.setForeground(new Color(241, 196, 15));
                enableButtons(false);
                addMessage(">>> Waiting for opponent's move...");
                
            } else if (message.startsWith("VICTORY")) {
                gameActive = false;
                statusLabel.setText("🎉 YOU WON! 🎉");
                statusLabel.setForeground(new Color(46, 204, 113));
                addMessage("\n╔═══════════════════════════════╗");
                addMessage("║   🎉 CONGRATULATIONS! 🎉      ║");
                addMessage("║         YOU WON!              ║");
                addMessage("╚═══════════════════════════════╝");
                enableButtons(false);
                showEndGameDialog("Congratulations! You Won!", "Victory!");
                
            } else if (message.startsWith("DEFEAT")) {
                gameActive = false;
                String winner = message.split(" ")[1];
                statusLabel.setText("Game Over - Player " + winner + " Wins");
                statusLabel.setForeground(X_COLOR);
                addMessage("\n╔═══════════════════════════════╗");
                addMessage("║      Game Over - You Lost     ║");
                addMessage("║   Player " + winner + " wins!         ║");
                addMessage("╚═══════════════════════════════╝");
                enableButtons(false);
                showEndGameDialog("Player " + winner + " wins!", "Game Over");
                
            } else if (message.startsWith("DRAW")) {
                gameActive = false;
                statusLabel.setText("Game Over - Draw!");
                statusLabel.setForeground(new Color(241, 196, 15));
                addMessage("\n╔═══════════════════════════════╗");
                addMessage("║      Game Over - Draw!        ║");
                addMessage("║     Well played both!         ║");
                addMessage("╚═══════════════════════════════╝");
                enableButtons(false);
                showEndGameDialog("It's a draw! Well played both!", "Draw");
                
            } else if (message.startsWith("INVALID")) {
                addMessage(">>> " + message);
            }
        });
    }
    
    private void updateBoard(String boardState) {
        String[] positions = boardState.split(",");
        if (positions.length == 9) {
            for (int i = 0; i < 9; i++) {
                String value = positions[i];
                buttons[i].setText(value);
                
                if (value.equals("X")) {
                    buttons[i].setForeground(X_COLOR);
                    buttons[i].setEnabled(false);
                } else if (value.equals("O")) {
                    buttons[i].setForeground(O_COLOR);
                    buttons[i].setEnabled(false);
                } else {
                    buttons[i].setForeground(Color.LIGHT_GRAY);
                    if (myTurn) {
                        buttons[i].setEnabled(true);
                    }
                }
            }
        }
    }
    
    private void makeMove(int position) {
        if (myTurn && gameActive && buttons[position].isEnabled()) {
            out.println("MOVE " + position);
            myTurn = false;
            enableButtons(false);
            addMessage("You placed " + myMark + " at position " + (position + 1));
        }
    }
    
    private void enableButtons(boolean enable) {
        for (int i = 0; i < 9; i++) {
            if (buttons[i].getText().matches("[0-9]")) {
                buttons[i].setEnabled(enable);
            }
        }
    }
    
    private void addMessage(String message) {
        messageArea.append(message + "\n");
        messageArea.setCaretPosition(messageArea.getDocument().getLength());
    }
    
    private void showEndGameDialog(String message, String title) {
        int choice = JOptionPane.showOptionDialog(
            this,
            message + "\n\nWould you like to play again?",
            title,
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE,
            null,
            new String[]{"Play Again", "Exit"},
            "Play Again"
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
            new TicTacToeGUI();
        } else {
            System.exit(0);
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> new TicTacToeGUI());
    }
}
