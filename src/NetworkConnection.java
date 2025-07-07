import java.io.*;
import java.net.*;
import java.util.function.Consumer;
public class NetworkConnection {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> messageHandler;

    //marrja e socketit dhe funksioni per trajtim e mesazheve
    public NetworkConnection(Socket socket, Consumer<String> messageHandler) throws IOException {
        this.socket = socket;
        this.messageHandler = messageHandler;

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


        Thread readerThread = new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    messageHandler.accept(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        readerThread.start();
    }


//    public NetworkConnection(boolean isServer, String host, int port, Consumer<String> messageHandler) throws IOException {
//        this.messageHandler = messageHandler;
//        if (isServer) {
//            ServerSocket serverSocket = new ServerSocket(port);
//            socket = serverSocket.accept();
//        } else {
//            socket = new Socket(host, port);
//        }
//        out = new PrintWriter(socket.getOutputStream(), true);
//        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//
//
//        Thread readerThread = new Thread(() -> {
//            try {
//                String message;
//                while ((message = in.readLine()) != null) {
//                    messageHandler.accept(message);  // Pass message to the handler
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        });
//        readerThread.start();
//    }


    public void sendMessage(String message) {
        out.println(message);
    }


    public void close() throws IOException {
        socket.close();
    }
}
