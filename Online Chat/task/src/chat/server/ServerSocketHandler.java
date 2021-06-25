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

            boolean chatFlag = false;
            String inter = "";
            int listSize = 0;

            while (!socket.isClosed()) {
                try {
                    String message = input.readUTF(); //client should send exit and close its conn

                    if (!server.getClientList().containsKey(login == null ? "noauth" : login)) {
                        authentication(message);
                    } else {

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
                            } else if ("/grant".equalsIgnoreCase(msgParse[0])) {
                                if (login.equalsIgnoreCase("admin")) {
                                    if (!server.getModeratorList().containsKey(msgParse[1])) {
                                        server.grantUserModeratorRole(msgParse[1]);
                                        output.writeUTF("Server: " + msgParse[1] + " is the new moderator!");
                                        server.getClientList().get(msgParse[1]).output.writeUTF("Server: you are the new moderator now!");
                                    } else {
                                        output.writeUTF("Server: this user is already a moderator!");
                                    }
                                } else {
                                    output.writeUTF("Server: you are not an admin!");
                                }
                            } else if ("/revoke".equalsIgnoreCase(msgParse[0])) {
                                if (login.equalsIgnoreCase("admin")) {
                                    if (!server.getModeratorList().containsKey(msgParse[1])) {
                                        output.writeUTF("Server: this user is not a moderator!");
                                    } else {
                                        server.revokeUserModeratorRole(msgParse[1]);
                                        output.writeUTF("Server: " + msgParse[1] + " is no longer a moderator!");
                                        server.getClientList().get(msgParse[1]).output.writeUTF("Server: you are no longer a moderator!");
                                    }
                                } else {
                                    output.writeUTF("Server: you are not an admin!");
                                }
                            } else if ("/kick".equalsIgnoreCase(msgParse[0])) {
                                if (!server.getModeratorList().containsKey(login)) {
                                    output.writeUTF("Server: you are not a moderator or an admin!");
                                } else if (login.equalsIgnoreCase(msgParse[1])) {
                                    output.writeUTF("Server: you can't kick yourself!");
                                } else if ((!server.getModeratorList().containsKey(msgParse[1]) ||
                                        "admin".equalsIgnoreCase(login)) &&
                                        !"admin".equalsIgnoreCase(msgParse[1])) {
                                    server.addClientBan(msgParse[1]);
                                    output.writeUTF("Server: " + msgParse[1] + " was kicked!");
                                    server.getClientList().get(msgParse[1]).output.writeUTF("Server: you have been kicked out of the server!");
                                    //server.getClientList().get(msgParse[1]).exitServer();
                                    server.deleteClientListItem(msgParse[1]);
                                }
                            } else if ("/unread".equalsIgnoreCase(message)) {
                                //Server: unread from: USER_A USER_B (etc)
                                output.writeUTF(server.getAllUnreadMsgsClients(login));

                            } else if ("/history".equalsIgnoreCase(msgParse[0])) {
                                if (!msgParse[1].matches("\\d+")) {
                                    output.writeUTF("Server: " + msgParse[1] + " is not a number!");
                                } else {
                                    if (inter != null) {
                                        int startVal = Math.max(0, listSize - Integer.parseInt(msgParse[1]));
                                        if (listSize > 0) {
                                            output.writeUTF("Server:");
                                            for (String e : server.getMsgList(inter).subList(startVal, Math.min(startVal + 25, listSize))) {
                                                if (e != null) {
                                                    output.writeUTF(e);
                                                }
                                            }
                                        }
                                    }
                                }
                            } else if ("/stats".equalsIgnoreCase(message)) {
                                if (inter != null) {
                                    int total = server.getMsgList(inter).size();
                                    int from = Integer.parseInt(Long.toString(server.getMsgList(inter).stream()
                                            .filter(e -> e.substring(0, currentChatWith.length()).equals(currentChatWith))
                                            .count()));
                                    String out = "Server:\n" +
                                            "Statistics with " + currentChatWith + ":\n" +
                                            "Total messages: " + total + "\n" +
                                            "Messages from " + login + ": " + (total - from) + "\n" +
                                            "Messages from " + currentChatWith + ": " + from;

                                    output.writeUTF(out);
                                }
                            } else if ("/chat".equalsIgnoreCase(msgParse[0])) {
                                if (!server.getClientList().containsKey(msgParse[1])) {
                                    output.writeUTF("Server: the user is not online!");
                                    chatFlag = false;
                                } else {
                                    currentChatWith = msgParse[1];
                                    chatFlag = true;
                                    inter = chatKeyCalculation(login, currentChatWith);
                                }
// should send the last 10 read messages and all unread messages on top of that.
// Note: the server must never send more than 25 messages
                                listSize = server.getMsgList(inter).size();
                                int startVal = Math.max(0, listSize - server.getOutputMsgsInd(inter));
                                if (listSize > 0) {
                                    for (String e : server.getMsgList(inter).subList(startVal, listSize)) {
                                        if (e != null) {
                                            output.writeUTF(e); //initial last 10 messages
                                        }
                                    }
                                    server.markReadMsgs(inter);
                                }

                            } else if (chatFlag) {
                                String response = login + ": " + message;
                                server.setMsgList(inter, response);

                                output.writeUTF(response);

                                //if another client connected - send, otherwise (new)

                                ServerSocketHandler opponent = server.getClientList().get(currentChatWith);

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

    private void authentication(String message) {
        try {
                String[] clName = message.split("\\s+");

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
                        server.setClientList(clName[1], this);
                        server.setClientAuth(clName[1], clName[2]);
                        login = clName[1];
                        output.writeUTF("Server: you are registered successfully!");
                        //break;
                    }
                } else if (clName[0].equalsIgnoreCase("/auth")) {
                    if (!server.getClientAuth().containsKey(clName[1])) {
                        output.writeUTF("Server: incorrect login!");
                    } else if (server.getClientPass(clName[1]).equals(String.valueOf(clName[2].hashCode()))) {
                        if (server.validateClientBan(clName[1])) {
                            output.writeUTF("Server: you are banned!");
                        } else {
                            login = clName[1];
                            server.setClientList(clName[1], this);
                            output.writeUTF("Server: you are authorized successfully!");
                        }
                        //break;
                    } else {
                        output.writeUTF("Server: incorrect password!");
                    }
                } else {
                    exitServer();
                    //break;
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void exitServer() {
        try {
            if (login != null) {
                server.deleteClientListItem(login);
            }
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
                    server.getClientList().keySet().stream()
                            .filter(e -> !e.equals(login)).reduce((k, n) -> k + n)
                            .orElse("Server: no one online");
    }

    private String chatKeyCalculation(String login1, String login2) {
            return login1 + "-" + login2;
    }

}
