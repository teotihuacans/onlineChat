package chat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ServerSocketHandler extends Thread {
    private Socket socket;
    private int clientId;

    public ServerSocketHandler(Socket socket, int clientId) {
        this.socket = socket;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        try {
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            //long clientId = Thread.currentThread().getId();
            System.out.printf("Client %d connected!\n", clientId);

            while (!socket.isClosed()) {
                String message = input.readUTF(); //client should send exit and close its conn
                if (!"/exit".equalsIgnoreCase(message) && !Thread.interrupted()) {
                    System.out.println("Client " + clientId + " sent: " + message);
                    System.out.println("Sent to client " + clientId + ": Count is " +
                            message.split("\\s+").length);
                    output.writeUTF("Count is " + message.split("\\s+").length);
                } else {
                    socket.close();
                    System.out.println("Client " + clientId + " disconnected!");
                }
            }
        } catch (IOException e) {
            System.out.println(Thread.currentThread().getId() + ", " +
                    Thread.currentThread().getName() + "stopped. " + e);
        }
    }
}
