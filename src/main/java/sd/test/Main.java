package sd.test;

import sd.client.Client;
import sd.client.ui.ClientUI;
import sd.packets.server.NotificacaoReply;
import sd.packets.server.ServerReply;
import sd.server.Voo;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
            System.out.println("Regista user: " + testRegistaUser(client1, client2));
            System.out.println("Efetua reserva: " + testReserva(client1, client2));
        } catch (IOException | InterruptedException e) {
            System.err.println(ClientUI.ANSI_RED + "\nServidor Desconectado" + ClientUI.ANSI_RESET);
        }
    }

    private static boolean testAutenticaUser(Client client1, Client client2) throws InterruptedException {
        Thread t1 = new Thread(() -> client1.autenticaUser("admin", "password"));
        Thread t2 = new Thread(() -> client2.autenticaUser("admin", "password"));

        t1.start();
        t2.start();

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

    private static boolean testRegistaUser(Client client1, Client client2) throws InterruptedException {
        AtomicInteger successes = new AtomicInteger();
        Thread t1 = new Thread(() -> {
            if (client1.registaUser("teste", "teste1").getStatus() == ServerReply.Status.Success) {
                successes.getAndIncrement();
            }
        });
        Thread t2 = new Thread(() -> {
            if (client2.registaUser("teste", "teste1").getStatus() == ServerReply.Status.Success) {
                successes.getAndIncrement();
            }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        return successes.get() == 1;
    }

    private static boolean testReserva(Client client1, Client client2) throws InterruptedException {
        AtomicInteger successes = new AtomicInteger();

        client1.autenticaUser("matilde", "bravo");
        client2.autenticaUser("teste", "teste1");

        Thread t1 = new Thread(() -> {
            try {
                client1.efetuaReserva(List.of("Berlim", "Lisboa"), LocalDate.now(), LocalDate.now().plusDays(1));
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<NotificacaoReply> notificacoes = client1.getNotificacoes();
            while (notificacoes.size() == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                notificacoes = client1.getNotificacoes();
            }

            if (notificacoes.get(0).getMensagem().startsWith("Os seus voos")) {
                System.out.println("Notificação: " + notificacoes.get(0).getMensagem());
                successes.getAndIncrement();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                client2.efetuaReserva(List.of("Berlim", "Lisboa"), LocalDate.now(), LocalDate.now());
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<NotificacaoReply> notificacoes = client2.getNotificacoes();
            while (notificacoes.size() == 0) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                notificacoes = client2.getNotificacoes();
            }

            if (notificacoes.get(0).getMensagem().startsWith("Os seus voos")) {
                System.out.println("Notificação: " + notificacoes.get(0).getMensagem());
                successes.getAndIncrement();
            }
        });

        t1.start(); t2.start();
        t1.join(); t2.join();

        return successes.get() == 1;
    }
}
