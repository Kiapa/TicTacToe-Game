# Tic Tac Toe - Multiplayer Game

A networked Tic Tac Toe game with both console and GUI clients, supporting real-time multiplayer gameplay over TCP/IP.

## Features

- ✨ Real-time multiplayer gameplay
- 🎮 Two client options: Console-based and GUI-based
- 🌐 Network play over TCP/IP (LAN or Internet)
- 🎯 Clean, modern graphical interface with Swing
- 🔄 Real-time board updates for both players
- ⚡ Immediate visual feedback
- 🏆 Win/Draw detection with beautiful end-game messages

## Files

- `TicTacToeServer.java` - Game server that manages the game logic and player connections
- `TicTacToeClient.java` - Console-based client for text-based gameplay
- `TicTacToeGUI.java` - GUI-based client with modern graphical interface

## How to Play

### Step 1: Compile All Files

```bash
javac TicTacToeServer.java TicTacToeClient.java TicTacToeGUI.java
```

### Step 2: Start the Server

```bash
java TicTacToeServer
```

Or specify a custom port:
```bash
java TicTacToeServer 9000
```

The server will display:
```
Tic Tac Toe Server is Running on port 8901...
Waiting for players to connect...
```

### Step 3: Connect Two Players

You can mix and match console and GUI clients!

#### Option A: Using the GUI Client (Recommended)

```bash
java TicTacToeGUI
```

When prompted:
1. Enter the server IP address (press Enter for localhost: 127.0.0.1)
2. Enter the server port (press Enter for default: 8901)
3. Wait for both players to connect
4. Click on any available square to make your move!

#### Option B: Using the Console Client

```bash
java TicTacToeClient
```

When prompted:
1. Enter the server IP address (press Enter for localhost)
2. Enter the server port (press Enter for default 8901)
3. Wait for both players to connect
4. Enter numbers 1-9 to place your mark

## Game Rules

- Player X always goes first
- Players take turns placing their marks (X or O)
- First player to get 3 marks in a row (horizontally, vertically, or diagonally) wins
- If all 9 squares are filled with no winner, the game is a draw
- Both players see real-time board updates after each move

## Board Layout

```
 |---|---|---| 
 | 1 | 2 | 3 | 
 |-----------| 
 | 4 | 5 | 6 | 
 |-----------| 
 | 7 | 8 | 9 | 
 |---|---|---| 
```

## Network Play

### Playing on the Same Computer
- Use `127.0.0.1` or `localhost` as the server address

### Playing Over LAN
1. Start server on one computer
2. Find the server's IP address:
   - Windows: `ipconfig`
   - Linux/Mac: `ifconfig` or `ip addr`
3. Clients connect using that IP address
4. Make sure firewall allows the port (default: 8901)

### Playing Over Internet
1. Set up port forwarding on the server's router for port 8901
2. Clients use the server's public IP address
3. Consider security implications when exposing services to the internet

## GUI Features

- **Modern Design**: Clean, dark-themed interface with smooth colors
- **Visual Feedback**: 
  - X marks in red
  - O marks in blue
  - Hover effects on available squares
- **Status Updates**: Clear indication of whose turn it is
- **Message Log**: Scrollable message area showing game events
- **End Game Options**: Choose to play again or exit after each game

## Troubleshooting

### Connection Refused
- Make sure the server is running before starting clients
- Verify the IP address and port are correct
- Check firewall settings

### Game Won't Start
- Ensure both players are connected
- Server needs exactly 2 players to start

### Port Already in Use
- Change the port number when starting the server
- Make sure no other instance is running

## Technical Details

- **Language**: Java
- **GUI Framework**: Swing
- **Network Protocol**: TCP/IP Sockets
- **Port**: 8901 (default, configurable)
- **Threading**: Multi-threaded server handling concurrent players

## Code Structure

### Server
- Manages two player threads
- Validates moves
- Checks for win/draw conditions
- Broadcasts board updates to both players

### Client (Console)
- Text-based interface
- Keyboard input for moves
- Real-time board display in terminal

### Client (GUI)
- Swing-based graphical interface
- Mouse-click input for moves
- Visual board with colored marks
- Scrollable message log

## Future Enhancements

Potential improvements:
- [ ] Support for multiple concurrent games
- [ ] Player statistics and leaderboard
- [ ] Replay game feature
- [ ] Spectator mode
- [ ] Chat functionality
- [ ] Sound effects
- [ ] Animation effects for moves and wins
- [ ] AI opponent option

## License

Free to use and modify for educational purposes.

---

Enjoy playing! 🎮
