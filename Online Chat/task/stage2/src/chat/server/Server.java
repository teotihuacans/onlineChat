package chat.server;
import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Server extends Thread
{
    private static final int PORT = 34523;
    private static final String ADDRESS = "127.0.0.1";
    private static final String INITIAL_MSG = "Server started!";
    private static boolean STOPSERVER = false;
    private Scanner scanner = new Scanner(System.in);

    public static void stopServer(boolean value) {
        STOPSERVER = value;
    }

    @Override
    public void run() {
        //try (ServerSocket server = new ServerSocket(PORT)) {
        try (ServerSocket server = new ServerSocket(PORT, 50, InetAddress.getByName(ADDRESS))) {
            while (!STOPSERVER) {
                try (
                        Socket socket = server.accept(); // accepting a new client
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream())
                ) {

                    new Thread(() -> {
                        while (!STOPSERVER) {
                            readAndOutputMessage(input);
                        }
                    }).start();

                    try {
                        //System.out.println("Check");
                        Server.currentThread().wait(3000);
                    } catch (Exception ignored) {
                    }

                    sentMessage(output, INITIAL_MSG);

                    /*String msg = "";
                    while (scanner.hasNext()) { //Всегда будет на консоли next, нужен символ или /n
                        msg += scanner.nextLine() + "\n";
                    }*/

                    while (true) {
                        if (!sentMessage(output, scanner.nextLine())) {
                            break;
                        }
                    }

                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean sentMessage(DataOutputStream out, String message) {
        try {
            out.writeUTF(message);
            /*if (!message.matches(".+")) {
                return false;
            }*/
        } catch (EOFException | SocketException ignored) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't write the message!");
        }
        return true;
    }

    private void readAndOutputMessage(DataInputStream in) {
        while (true) {
            try {
                System.out.println(/*"Server read: " +*/ in.readUTF());
                /*String msg = in.readUTF();
                if (msg.matches(".+")) {
                    System.out.println(*//*"Server read: " +*//* msg);
                } else {
                    return;
                }*/
            } catch (EOFException | SocketException ignored) {
                return;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Can't read the message!");
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Server srv = new Server();
        srv.start();
        srv.join(3000L);
    }
}


