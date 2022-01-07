package sd.client.ui;

import sd.client.Client;
import sd.exceptions.PermissionDeniedException;
import sd.server.Reply;
import sd.server.Voo;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Scanner;
public class ClientUI {
        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_BLACK = "\u001B[30m";
        public static final String ANSI_RED = "\u001B[31m";
        public static final String ANSI_GREEN = "\u001B[32m";
        public static final String ANSI_YELLOW = "\u001B[33m";
        public static final String ANSI_BLUE = "\u001B[34m";
        public static final String ANSI_PURPLE = "\u001B[35m";
        public static final String ANSI_CYAN = "\u001B[36m";
        public static final String ANSI_WHITE = "\u001B[37m";
        public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/y");
        private static Scanner scin;

        private Client client;


        public ClientUI(String address, int port) throws IOException {
            this.client = new Client(address,port);
            scin = new Scanner(System.in);
        }

    public static void changeScanner() {
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
                "Adiciona Voo",
                "Encerra Dia",
        });

        //menu.setPreCondition(1, ()-> !client.isAutenticado() );
        //menu.setPreCondition(2, ()->  !client.isAutenticado() );

        menu.setHandler(1, this::registar);
        menu.setHandler(2, this::autenticar);
        menu.setHandler(4, this::cancelarReserva);
        menu.setHandler(5, this::listaVoos);
        menu.setHandler(6, this::adicionaVoo);
        menu.run();
    }

    private void adicionaVoo() {
            System.out.print("Origem: ");
            String origem = scin.nextLine();
            System.out.print("Destino: ");
            String destino = scin.nextLine();
            System.out.print("Capacidade: ");
            long capacidade = Long.parseLong(scin.nextLine());
            System.out.print("Data: ");
            LocalDate data = LocalDate.parse(scin.nextLine(), formatter );

        try {
            if(client.adicionaVoo(origem,destino,capacidade,data)) {
                System.out.println("Voo adicionado com sucesso");
            }
            else {
                System.out.println("Esta funcionalidade apenas está disponivel para admins");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void listaVoos() throws PermissionDeniedException {
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
