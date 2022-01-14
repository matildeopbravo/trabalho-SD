package sd.client.ui;

import sd.client.Client;
import sd.exceptions.PermissionDeniedException;
import sd.server.*;

import java.io.Console;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
    public static final String ANSI_BOLD = "\u001B[1m";
    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/y");
    private static Scanner scin;

    private final Client client;

    public ClientUI(String address, int port) throws IOException {
        this.client = new Client(address, port);
        scin = new Scanner(System.in);
    }

    public static void changeScanner() {
        scin = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Bem Vindo Sistema De Reservas de Voos ");
        this.menuPrincipal();
        System.out.println("Até breve...");
    }

    private void menuPrincipal() {
        Menu menu = new Menu(new String[]{
                "Registar",
                "Login",
                "Logout",
                "Efetuar Reserva",
                "Cancelar Reserva",
                "Lista Reservas",
                "Lista de voos",
                "Adiciona Voo",
                "Encerra Dia",
        });

        // isto deixa na mesma executar a acao, so fica a vermelho se nao satisfizer a condicao
        menu.setPreCondition(2, () -> !client.isAutenticado());
        menu.setPreCondition(3, client::isAutenticado);
        menu.setPreCondition(4, client::isAutenticado);
        menu.setPreCondition(5, client::isAutenticado);
        menu.setPreCondition(6, () -> client.isAutenticado() && client.isAdmin());
        menu.setPreCondition(7, () -> client.isAutenticado());
        menu.setPreCondition(8, () -> client.isAutenticado() && client.isAdmin());
        menu.setPreCondition(9, () -> client.isAutenticado() && client.isAdmin());
        //menu.setPreCondition(2, ()->  !client.isAutenticado() );

        menu.setHandler(1, this::registar);
        menu.setHandler(2, this::autenticar);
        menu.setHandler(3, this::logout);
        menu.setHandler(4, this::efetuarReserva);
        menu.setHandler(5, this::cancelarReserva);
        menu.setHandler(6, this::listaReservas);
        menu.setHandler(7, this::listaVoos);
        menu.setHandler(8, this::adicionaVoo);
        //menu.setHandler(9, this::encerraDia);
        menu.run(client);
    }

    private void efetuarReserva() {
        System.out.print("Locais de Passagem Separados Por Vírgula: ");
        String locais = scin.nextLine();
        System.out.print("Data Inicial (D/M/Y) : ");
        LocalDate dataInit = LocalDate.parse(scin.nextLine(),formatter);
        System.out.print("Data Final (D/M/Y) : ");
        LocalDate dataFin = LocalDate.parse(scin.nextLine(),formatter);

        try {
            int res  = client.efetuaReserva(List.of(locais),dataInit,dataFin);
            if (res > 0) {
                System.out.println("Reserva Nº " + res);
            } else {
                System.out.println("Reserva não pode ser realizada");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void listaReservas() {
            List<Reserva> l = client.pedeListaReservas();
            l.forEach(System.out::println);

    }

    private void logout() {
        if(client.getUserAutenticado() != null) {
            Reply r = client.fazLogout();
            System.out.println(r);
        }
        else {
            System.out.println("Tem que estar autenticado para fazer logout");
        }
    }
     private String prettyReadLine(String prompt, String color, boolean password) {
        System.out.print(ClientUI.ANSI_BOLD + color + "❯ " + ClientUI.ANSI_RESET + ClientUI.ANSI_BOLD +
                prompt + ": " + ClientUI.ANSI_RESET);
        if (password) {
            Console console = System.console();
            if (console != null) {
                return new String(console.readPassword());
            }
        }
        return scin.nextLine();
    }

    private String prettyReadLine(String prompt) {
        return prettyReadLine(prompt, ANSI_YELLOW, false);
    }

    private void adicionaVoo() {
        String origem = prettyReadLine("Origem");
        String destino = prettyReadLine("Destino");
        System.out.print("Capacidade: ");
        long capacidade = Long.parseLong(prettyReadLine("Capacidade"));
        //System.out.print("Data: ");
        //LocalDate data = LocalDate.parse(scin.nextLine(), formatter);

        try {
            if (client.adicionaVoo(origem, destino, capacidade )) {
                System.out.println("Voo adicionado com sucesso");
            } else {
                System.out.println("Esta funcionalidade apenas está disponivel para admins");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cancelarReserva() {
        int num;
        boolean start = true;
        do {
            if (!start)
                System.out.println(ANSI_BOLD + ANSI_RED + "Não é possível cancelar essa reserva" + ANSI_RESET);

            start = false;
            num = Integer.parseInt(prettyReadLine("Número da reserva"));
        } while (!client.cancelaReserva(num).equals(Reply.Success));
        System.out.println(ANSI_BOLD + "Reserva Nº " + num + " cancelada com sucesso!" + ANSI_RESET);
    }

    private void listaVoos() throws PermissionDeniedException {
        try {
            List<VooTabelado> l = client.pedeListaVoos();
            Table<VooTabelado> tabelaVoos = new Table<>();
            tabelaVoos.addColumn("Origem", VooTabelado::getOrigem);
            tabelaVoos.addColumn("Destino", VooTabelado::getDestino);
            tabelaVoos.addColumn("Capacidade", v -> String.valueOf(v.getCapacidade()));
            tabelaVoos.addItems(l);
            tabelaVoos.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void autenticar() {
        boolean start = true;
        String username;
        String password;
        do {
            if (!start) System.out.println("Credenciais Inválidas");
            username = prettyReadLine("Username");
            password = prettyReadLine("Password", ANSI_YELLOW, true);
            start = false;
        } while (!client.autenticaUser(username, password).equals(Reply.Success));
    }

    private void registar() {
        String username;
        String password;
        do {
            username = prettyReadLine("Username");
            password = prettyReadLine("Password", ANSI_YELLOW, true);
        } while (!client.registaUser(username, password).equals(Reply.Success));
        System.out.println("Utilizador Registado Com sucesso");
    }
}
