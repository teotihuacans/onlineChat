package chat.server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Server extends Thread implements Serializable
{
    private static final transient int PORT = 34523;
    private static final transient String ADDRESS = "127.0.0.1";
    private static final transient String INITIAL_MSG = "Server started!";
    private transient ConcurrentHashMap<ServerSocketHandler, String> clientList = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, String> clientAuth = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, List<String>> msgList = new ConcurrentHashMap<>();
    private transient List<String> AVAILABLE_COMMANDS = List.of("/list", "/exit", "/chat");
    private transient String filename = "Server.data";

    public ConcurrentHashMap<ServerSocketHandler, String> getClientList() {
        return clientList;
    }

    public void setClientList(ServerSocketHandler clPipe, String clName) {
        clientList.put(clPipe, clName);
    }

    public void deleteClientListItem(ServerSocketHandler clPipe) {
        clientList.remove(clPipe);
    }

    public ConcurrentHashMap<String, String> getClientAuth() {
        return clientAuth;
    }

    public void setClientAuth(String login, String password) {
        clientAuth.put(login, password);
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

    public List<String> getAVAILABLE_COMMANDS() {
        return AVAILABLE_COMMANDS;
    }

    public Server() {
        File bchStore = new File(filename);
        try {
            if (bchStore.exists()) {
                //ConcurrentHashMap<String, String>
                clientAuth = ((Server) SerializationUtils.deserialize(filename)).getClientAuth();
                //ConcurrentHashMap<String, List<String>>
                msgList = ((Server) SerializationUtils.deserialize(filename)).getMsgList();
                System.out.println("Recovered: \n" + clientAuth);
                System.out.println("Recovered msgs: \n" + msgList);
                //clientAuth.addAll(clientAuthRecovered);
            }
        } catch (IOException | ClassNotFoundException e) {
            e.getCause();
        }
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


