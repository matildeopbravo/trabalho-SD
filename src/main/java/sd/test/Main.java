package sd.test;

import sd.client.Client;
import sd.client.ui.ClientUI;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        System.out.println("Executando testes...");
        String address = "127.0.0.1";
        int port = 2500;
        if (args.length > 0) {
            address = args[0];
            if (args.length > 1)
                port = Integer.parseInt(args[1]);
        }
        try {
            Client client1 = new Client(address, port);
            Client client2 = new Client(address, port);

            System.out.println("Dois clientes conectados");

            System.out.println("Autentica user: " + testAutenticaUser(client1, client2));
        } catch (IOException | InterruptedException e) {
            System.err.println(ClientUI.ANSI_RED + "\nServidor Desconectado" + ClientUI.ANSI_RESET);
        }
    }

    private static boolean testAutenticaUser(Client client1, Client client2) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            client1.autenticaUser("admin", "password");
        });
        Thread t2 = new Thread(() -> {
            client2.autenticaUser("admin", "password");
        });

        t1.start(); t2.start();

        t1.join();
        t2.join();

        boolean ret = (client1.isAutenticado() && !client2.isAutenticado()) || (client2.isAutenticado() && !client1.isAutenticado());

        if (client1.isAutenticado()) {
            client1.fazLogout();
        }
        if (client2.isAutenticado()) {
            client2.fazLogout();
        }

        return ret;
    }
}
