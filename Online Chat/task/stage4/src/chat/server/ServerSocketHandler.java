package chat.server;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class ServerSocketHandler extends Thread {
    private Socket socket;
    private Server server;
    private DataInputStream input = null;
    private DataOutputStream output = null;
    private String clName;

    public ServerSocketHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        start();
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            output.writeUTF("Server: write your name");

            while (true) {
                clName = input.readUTF();
                if (server.getClientList().containsValue(clName)) {
                    output.writeUTF("Server: this name is already taken! Choose another one.");
                } else {
                    server.setClientList(this, clName);
                    break;
                }
            }

            int listSize = server.getMsgList().size();
            int startVal = Math.max(0, listSize - 10);
            if (listSize > 0) {
                for(String e : new ArrayList<>(server.getMsgList()).subList(startVal, listSize)) {
                    if (e != null) {
                        output.writeUTF(e); //initial last 10 messages
                    }
                }
            }

            while (!socket.isClosed()) {
                try {
                    String message = input.readUTF(); //client should send exit and close its conn

                    if (!"/exit".equalsIgnoreCase(message) && !Thread.interrupted()) {
                        String response = clName + ": " + message;
                        server.setMsgList(response);
                        for (ServerSocketHandler ssh : server.getClientList().keySet()) {
                            ssh.output.writeUTF(response);
                        }
                    } else {
                        server.deleteClientListItem(this);
                        sleep(100);
                        socket.close();
                        if (server.getClientList().size() == 0) {
                            server.interrupt();
                        }
                        break;
                    }
                } catch (IOException e) {
                    System.out.println("Server Handler exception: " + e);
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Server handler: " + e);
        } finally {
        }
    }

}
