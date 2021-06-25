package chat.server;
import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Server extends Thread
{
    private static final int PORT = 34523;
    private static final String ADDRESS = "127.0.0.1";
    private static final String INITIAL_MSG = "Server started!";
    private Scanner scanner = new Scanner(System.in);
    private ConcurrentHashMap<ServerSocketHandler, String> clientList = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<String> msgList = new ConcurrentLinkedQueue();

    public ConcurrentHashMap<ServerSocketHandler, String> getClientList() {
        return clientList;
    }

    public void setClientList(ServerSocketHandler clPipe, String clName) {
        clientList.put(clPipe, clName);
    }

    public void deleteClientListItem(ServerSocketHandler clPipe) {
        clientList.remove(clPipe);
    }

    public ConcurrentLinkedQueue<String> getMsgList() {
        return msgList;
    }

    public void setMsgList(String message) {
        msgList.add(message);
    }

    @Override
    public void run() {
        try (
                ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))
        ) {
            server.setSoTimeout(12000);
            System.out.println(INITIAL_MSG);

            while (!server.isClosed() && !this.isInterrupted()) {
                ServerSocketHandler newClient = new ServerSocketHandler(server.accept(), this);
            }

        } catch (SocketTimeoutException e) {
            //System.out.println("Got socket time out exception.");
        }
        catch (IOException e) {
            e.printStackTrace();
        } finally {
            //System.out.println("Server finished");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Server srv = new Server();
        srv.start();
        srv.join(1000);
    }
}


