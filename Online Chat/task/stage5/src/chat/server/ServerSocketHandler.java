package chat.server;

import java.io.*;
import java.net.*;

public class ServerSocketHandler extends Thread {
    private Socket socket;
    private Server server;
    private DataInputStream input = null;
    private DataOutputStream output = null;
    private String login;
    private String currentChatWith = "";

    public ServerSocketHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        start();
    }

    public String getCurrentChatWith() {
        return currentChatWith;
    }

    @Override
    public void run() {
        try {
            input = new DataInputStream(socket.getInputStream());
            output = new DataOutputStream(socket.getOutputStream());

            output.writeUTF("Server: authorize or register");

            authentication();

            boolean chatFlag = false;
            String inter = "";

            while (!socket.isClosed()) {
                try {
                    String message = input.readUTF(); //client should send exit and close its conn
                    String[] msgParse = message.split("\\s+");

                    if (msgParse[0].contains("/")) {
                        chatFlag = false;
                    }

                    if (!"/exit".equalsIgnoreCase(message) && !Thread.interrupted()) {
                        if (msgParse[0].contains("/") &&
                                !server.getAVAILABLE_COMMANDS().contains(msgParse[0])) {
                            output.writeUTF("Server: incorrect command!");
                        } else if ("/list".equalsIgnoreCase(message)) {
                            output.writeUTF(onlineClientsList());
                        } else if ("/chat".equalsIgnoreCase(msgParse[0])) {
                            if (!server.getClientList().contains(msgParse[1])) {
                                output.writeUTF("Server: the user is not online!");
                                chatFlag = false;
                            } else {
                                currentChatWith = msgParse[1];
                                chatFlag = true;
                                inter = chatKeyCalculation(login, currentChatWith);
                            }

                            int listSize = server.getMsgList(inter).size();
                            int startVal = Math.max(0, listSize - 10);
                            if (listSize > 0) {
                                for(String e : server.getMsgList(inter).subList(startVal, listSize)) {
                                    if (e != null) {
                                        output.writeUTF(e); //initial last 10 messages
                                    }
                                }
                                server.markReadMsgs(inter);
                            }

                        } else if (chatFlag) {
                            String response = login + ": " + message;
                            server.setMsgList(inter, response);
                            String temp = currentChatWith;

                            output.writeUTF(response);

                            //if another client connected - send, otherwise (new)

                            ServerSocketHandler opponent = server.getClientList().entrySet().stream()
                                        .filter(e -> temp.equals(e.getValue()))
                                        .findFirst().get().getKey();

                            if (login.equals(opponent.getCurrentChatWith())) {
                                opponent.output.writeUTF(response);
                            } else {
                                response = "(new) " + response;
                            }

                            server.setMsgList(chatKeyCalculation(currentChatWith, login), response);

                        } else {
                            output.writeUTF("Server: use /list command to choose a user to text!");
                        }
                    } else {
                        exitServer();
                        break;
                    }
                } catch (IOException e) {
                    System.out.println(this.getName() + ": Server Handler exception: " + e +
                            " " + e.getLocalizedMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server handler: " + e);
        }
    }

    private void authentication() {
        try {
            while (!socket.isClosed()) {
                String[] clName = input.readUTF().split("\\s+");

                if ((!"/auth".equalsIgnoreCase(clName[0]) &&
                        !"/registration".equalsIgnoreCase(clName[0]) &&
                        !"/exit".equalsIgnoreCase(clName[0])) ||
                        ("/auth".equalsIgnoreCase(clName[0]) && clName.length < 3) ||
                        ("/registration".equalsIgnoreCase(clName[0]) && clName.length < 3)) {
                    output.writeUTF("Server: you are not in the chat!");
                } else if (clName[0].equalsIgnoreCase("/registration")) {
                    if (server.getClientAuth().containsKey(clName[1])) {
                        output.writeUTF("Server: this login is already taken! Choose another one.");
                    } else if (clName[2].length() < 8) {
                        output.writeUTF("Server: the password is too short!");
                        ;
                    } else {
                        server.setClientList(this, clName[1]);
                        server.setClientAuth(clName[1], String.valueOf(clName[2].hashCode()));
                        login = clName[1];
                        output.writeUTF("Server: you are registered successfully!");
                        break;
                    }
                } else if (clName[0].equalsIgnoreCase("/auth")) {
                    if (!server.getClientAuth().containsKey(clName[1])) {
                        output.writeUTF("Server: incorrect login!");
                    } else if (server.getClientPass(clName[1]).equals(String.valueOf(clName[2].hashCode()))) {
                        login = clName[1];
                        server.setClientList(this, clName[1]);
                        output.writeUTF("Server: you are authorized successfully!");
                        break;
                    } else {
                        output.writeUTF("Server: incorrect password!");
                    }
                } else {
                    exitServer();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exitServer() {
        try {
            server.deleteClientListItem(this);
            sleep(100);
            socket.close();
            if (server.getClientList().size() == 0) {
                server.interrupt();
            }
        } catch (IOException | InterruptedException e) {
            e.getMessage();
        }
    }

    private String onlineClientsList() {
            return "Server: online: " +
                    server.getClientList().values().stream()
                            .filter(e -> !e.equals(login)).reduce((k, n) -> k + n)
                            .orElse("Server: no one online");
    }

    private String chatKeyCalculation(String login1, String login2) {
            return login1 + "-" + login2;
    }

}
