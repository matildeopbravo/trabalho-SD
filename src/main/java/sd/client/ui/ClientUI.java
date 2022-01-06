package sd.client.ui;

import sd.client.Client;
import sd.server.Reply;
import sd.server.Server;
import sd.server.Voo;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class ClientUI {
        private Client client;

        private Scanner scin;

        public ClientUI(String address, int port) throws IOException {
            this.client = new Client(address,port);
            scin = new Scanner(System.in);
        }

        public void run() throws IOException {
            System.out.println("Bem Vindo Sistema De Reservas de Voos ");
            this.menuPrincipal();
            System.out.println("Até breve...");
        }

    private void menuPrincipal() {
        Menu menu = new Menu(new String[]{
                "Registar",
                "Login",
                "Efetuar Reserva",
                "Cancelar Reserva",
                "Lista de voos",
                "Muda Origem",
                "Muda Capacidade",
                "Encerra Dia",
        });

        //menu.setPreCondition(1, ()-> !client.isAutenticado() );
        //menu.setPreCondition(2, ()->  !client.isAutenticado() );

        menu.setHandler(1, ()->registar());
        menu.setHandler(2, ()->autenticar());
        menu.setHandler(4, ()->cancelarReserva());
        menu.setHandler(5, ()->listaVoos());
        menu.run();
    }

    private void cancelarReserva() {
            System.out.print("Introduza o número da reserva a cancelar");
            int num = 0;
            boolean start = true;
            do{
                if(!start)
                    System.out.print("Não é possível cancelar essa reserva ");
                start = false;
                num = Integer.parseInt(scin.nextLine());
            } while(!client.cancelaReserva(num).equals(Reply.Success));
            System.out.println("Reserva Nº " + num + " cancelada com sucesso!");
    }

    private void listaVoos() {
        try {
            List<Voo> l = client.pedeListaVoos();
            l.forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void autenticar() {
        boolean start = true;
        String username;
        String password;
        do {
            if(!start) System.out.println("Credenciais Inválidas");
            System.out.print("Username: ");
            username = scin.nextLine();
            System.out.print("Password: ");
            password = scin.nextLine();
            start = false;
        } while(!client.autenticaUser(username,password).equals(Reply.Success));
    }

    private void registar() {
            String username;
            String password;
        do {
                System.out.print("Username: ");
                username = scin.nextLine();
                System.out.print("Password: ");
                password = scin.nextLine();
        } while(!client.registaUser(username,password).equals(Reply.Success) );
        System.out.println("Utilizador Registado Com sucesso");
    }


}
