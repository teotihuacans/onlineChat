package chat.server;
import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Server extends Thread implements Serializable
{
    private static final transient int PORT = 34523;
    private static final transient String ADDRESS = "127.0.0.1";
    private static final transient String INITIAL_MSG = "Server started!";
    private static final transient int BANINTERVAL = 5;
    private transient ConcurrentHashMap<String, ServerSocketHandler> clientList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> clientAuth = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<String>> msgList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Enum<Roles>> moderatorList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, LocalDateTime> clientBan = new ConcurrentHashMap<>();
    private transient List<String> AVAILABLE_COMMANDS = List.of("/list", "/exit", "/chat", "/grant", "/revoke", "/kick", "/unread", "/history", "/stats");
    private transient String filename = "Server.data";

    public Server() {
        File bchStore = new File(filename);
        try {
            if (bchStore.exists()) {
                clientAuth = ((Server) SerializationUtils.deserialize(filename)).getClientAuth();
                msgList = ((Server) SerializationUtils.deserialize(filename)).getMsgList();
                moderatorList = ((Server) SerializationUtils.deserialize(filename)).getModeratorList();
                clientBan = ((Server) SerializationUtils.deserialize(filename)).getClientBan();
                //System.out.println("Recovered: \n" + clientAuth);
                //System.out.println("Recovered msgs: \n" + msgList);
            } else {
                clientAuth.put("admin", String.valueOf("12345678".hashCode()));
                moderatorList.put("admin", Roles.ADMIN);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.getCause();
        }
    }

    public ConcurrentHashMap<String, ServerSocketHandler> getClientList() {
        return clientList;
    }

    public void setClientList(String clName, ServerSocketHandler clPipe) {
        clientList.put(clName, clPipe);
    }

    public void deleteClientListItem(String clName) {
        clientList.remove(clName);
    }

    public ConcurrentHashMap<String, String> getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(String login, String password) {
        clientAuth.put(login, String.valueOf(password.hashCode()));
    }

    public String getClientPass(String login) {
        return clientAuth.getOrDefault(login, "");
    }

    public ConcurrentHashMap<String, List<String>> getMsgList() {
        return msgList;
    }

    public List<String> getMsgList(String loginKey) {
        return msgList.getOrDefault(loginKey, new ArrayList<>());
    }

    public void setMsgList(String loginKey, String message) {
        List<String> lst = msgList.getOrDefault(loginKey, new ArrayList<>());
        lst.add(message);
        msgList.put(loginKey, lst);
    }

    public void markReadMsgs(String loginKey) {
        List<String> lst = msgList.getOrDefault(loginKey, new ArrayList<>()).stream()
                .map(e -> "(new)".equals(e.substring(0, 5)) ? e.substring(6) : e)
                .collect(Collectors.toList());
        msgList.put(loginKey, lst);
    }

    public String getAllUnreadMsgsClients(String login) {
        return msgList.entrySet().stream().filter(k -> k.getKey().matches(login + "[-]\\w+"))
                .filter(v -> v.getValue().stream().anyMatch(e -> "(new)".equals(e.substring(0, 5))))
                .map(k -> k.getKey().substring(login.length() + 1))
                .reduce((k, n) -> "Server: unread from: " + k + " " + n)
                .orElse("Server: no one unread");
    }

    public int getUnreadMsgsCount(String loginKey) {
        return Integer.parseInt(Long.toString(msgList.getOrDefault(loginKey, new ArrayList<>()).stream()
                .filter(e -> "(new)".equals(e.substring(0, 5))).count()));
    }

    public int getOutputMsgsInd(String loginKey) {
        int unread = getUnreadMsgsCount(loginKey);

        return unread > 25 ? 25 : unread + Math.min(25 - unread, 10);
    }

    public List<String> getAVAILABLE_COMMANDS() {
        return AVAILABLE_COMMANDS;
    }

    public ConcurrentHashMap<String, Enum<Roles>> getModeratorList() {
        return moderatorList;
    }

    public void grantUserModeratorRole(String login) {
        moderatorList.put(login, Roles.MODERATOR);
    }

    public void revokeUserModeratorRole(String login) {
        moderatorList.remove(login);
    }

    public ConcurrentHashMap<String, LocalDateTime> getClientBan() {
        return clientBan;
    }

    public void addClientBan(String login) {
        clientBan.put(login, LocalDateTime.now());
    }

    public Boolean validateClientBan(String login) {
        if (clientBan.containsKey(login)) {
            if (clientBan.get(login).plusMinutes(BANINTERVAL).isBefore(LocalDateTime.now())) {
                clientBan.remove(login);
                return false;
            }
            return true;
        }

        return false;
    }

@Override
    public void run() {
        try (
                ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))
        ) {
            server.setSoTimeout(2500);
            //System.out.println(INITIAL_MSG);

            while (!server.isClosed() && !this.isInterrupted()) {
                ServerSocketHandler newClient = new ServerSocketHandler(server.accept(), this);
            }

        } catch (SocketTimeoutException e) {
            //System.out.println("Got socket time out exception.");
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("Server finished");
            try {
                SerializationUtils.serialize(this, filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Server srv = new Server();
        srv.start();
        srv.join(1000);
    }
}


