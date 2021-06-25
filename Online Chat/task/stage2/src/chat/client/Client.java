package chat.client;

import chat.server.Server;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client extends  Thread
{
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 34523;
    private static final String INITIAL_MSG = "Client started!";
    private Scanner scanner = new Scanner(System.in);

    @Override
    public void run() {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream())
        ) {

            new Thread(() -> {
                while (socket.isConnected()) {
                    readAndOutputMessage(input);
                }
            }).start();

            try {
                //System.out.println("Check");
                Server.currentThread().wait(3000);
            } catch (Exception ignored) {
            }

            sentMessage(output, INITIAL_MSG);

            //BufferedReader vin = new BufferedReader(new InputStreamReader(System.in));

            String temp;
            while (true) {
                if (!sentMessage(output, scanner.nextLine())) {
                    break;
                }
            }
            socket.close();
            Server.stopServer(true);

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
                System.out.println(/*"Client read: " +*/ in.readUTF());
                /*String msg = in.readUTF();
                if (msg.matches(".+")) {
                    System.out.println(*//*"Client read: " +*//* msg);
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
        //do nothing
        Client cl = new Client();
        cl.start();
        cl.join(3000L); //join throws exception; wait = IllegalMonitorStateException
    }
}
