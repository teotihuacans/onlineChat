package chat;

import chat.client.Client;
import chat.server.Server;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        //firstStageChat();

        Server.main(null);
        Client.main(null);

    }

    public static void firstStageChat() {
        Scanner in = new Scanner(System.in);
        StringBuilder stb = new StringBuilder();
        String[] mas;
        while (in.hasNext()) {
            stb.delete(0, stb.length());
            stb.insert(0, in.nextLine());
            mas = stb.toString().split("\\s+");
            if (mas.length >= 2 &&
                    mas[1].equals("sent")) {
                System.out.println(mas[0] + ": " +
                        stb.toString().substring(stb.toString().indexOf("sent") + 5));
            }
        }
    }
}
