Sink Ships is a two-player Battleship-style game implemented in Java using Swing for GUI and Sockets for networking. Players place their ships on a 10x10 grid and take turns attacking each other over a local network.

![sinkships1](https://github.com/user-attachments/assets/bfd3ab10-e6ea-4db0-a1cc-7b448e823851)
![sinkships2](https://github.com/user-attachments/assets/7091e7eb-daaf-46d6-92d2-e4f05652ae03)
![sinkships3](https://github.com/user-attachments/assets/066582fc-2774-4f02-8904-aaf12e4dcb7b)

 Features

   Interactive Swing-based GUI with two grids:
   
   Left grid: for placing your ships.

   Right grid: for attacking your opponent.

   Client-server communication via sockets using custom string-based protocol.

   Ship placement with validation (max 5 ships).

   Real-time feedback for hits, misses, win/loss alerts.

   Turn-based logic managed by server messages.

   In-game rules dialog for new players.

Run the server (in one terminal):

    java SinkShipsServer

Run the clients (open two separate terminal windows):

    java SinkShipsGUI

