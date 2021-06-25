package chat.client;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client
{
    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 34523;
    private static final String INITIAL_MSG = "Client started!";
    private Scanner scanner = new Scanner(System.in);

    public void run() {
        try (
                Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output  = new DataOutputStream(socket.getOutputStream())
        ) {

            //sentMessage(output, INITIAL_MSG);
            System.out.println(INITIAL_MSG);

            Thread readTr = new Thread(() -> {
                while(!Thread.currentThread().isInterrupted()) {
                    readAndOutputMessage(input);
                }
            });

            readTr.start();
            readTr.join(100);

            String temp;
            while (!Thread.currentThread().isInterrupted()) {
                if (!sentMessage(output, scanner.nextLine())) {
                    readTr.interrupt();
                    readTr.join(40);
                    break;
                }
            }

        } catch (IOException | InterruptedException e) {
            //e.printStackTrace();
        } finally {
            //System.out.println("Client finished");
        }
    }

    private boolean sentMessage(DataOutputStream out, String message) {
        try {
            out.writeUTF(message);
            if ("/exit".equalsIgnoreCase(message)) {
                //for server exit also should be sent to close socket
                return false;
            }
        } catch (EOFException | SocketException e) {
            System.out.println("Exception in client: " + e);
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

    public static void main(String[] args) {
        Client cl = new Client();
        cl.run();
    }
}