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

            Thread readTr = new Thread(() -> {
                while(!Thread.currentThread().isInterrupted()) {
                    readAndOutputMessage(input);
                }
                //System.out.println("Client read thread interrupted");
            });

            readTr.start();
            readTr.join(100);

            //sentMessage(output, INITIAL_MSG);
            System.out.println(INITIAL_MSG);

            String temp;
            while (true) {
                if (!sentMessage(output, scanner.nextLine())) {
                    break;
                }
            }

            readTr.interrupt();
            //socket.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private boolean sentMessage(DataOutputStream out, String message) {
        try {
            out.writeUTF(message);
            if ("/exit".equalsIgnoreCase(message)) {
                //for server exit also should be sent to close socket
                return false;
            }
        } catch (EOFException | SocketException ignored) {
            System.out.println("Got exception in client, exited.");
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Can't write the message!");
        }
        return true;
    }

    private void readAndOutputMessage(DataInputStream in) {
            try {
                System.out.println(in.readUTF());
            } catch (EOFException | SocketException ignored) {
                return;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Can't read the message!");
            }
    }

    public static void main(String[] args) throws InterruptedException {
        //do nothing
        Client cl = new Client();
        cl.start();
        cl.join(1000L);
    }
}
