package chat.server;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server extends Thread
{
    private static final int PORT = 34523;
    private static final String ADDRESS = "127.0.0.1";
    private static final String INITIAL_MSG = "Server started!";
    private Scanner scanner = new Scanner(System.in);
    private int clientId = 1;

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
            server.setSoTimeout(12000);
            System.out.println(INITIAL_MSG);
            while (!server.isClosed())
            {
                ServerSocketHandler newClient = new ServerSocketHandler(server.accept(), clientId++);
                //System.out.printf("Client %d connected!\n", clientId++);
                newClient.start();
                newClient.join(100);
            }

        } catch (SocketTimeoutException | InterruptedException e) {
            //System.out.println("Got socket time out exception.");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Server srv = new Server();
        srv.start();
        srv.join(1000L);
    }
}


