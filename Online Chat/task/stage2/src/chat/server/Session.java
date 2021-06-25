package chat.server;

import java.io.*;
import java.net.Socket;

class Session extends Thread {
    private final Socket socket;

    public Session(Socket socketForClient) {
        this.socket = socketForClient;
    }

    public void run() {
        try (
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream())
        ) {
            System.out.println("Server started!");
            for (int i = 0; i < 5; i++) {
                String msg = input.readUTF();
                output.writeUTF(msg);
            }
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}