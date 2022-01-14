package sd.client.ui;

import sd.client.Client;
import sd.exceptions.PermissionDeniedException;

import java.io.Console;
import java.io.IOException;
import java.util.*;

public class Menu {

    // Interfaces auxiliares

    /**
     * Functional interface para handlers.
     */
    public interface Handler {
        void execute() throws PermissionDeniedException;
    }

    /**
     * Functional interface para pré-condições.
     */
    public interface PreCondition {
        boolean validate();
    }

    // Varíável de classe para suportar leitura
    private static final Scanner is = new Scanner(System.in);

    // Variáveis de instância
    private String titulo;                  // Titulo do menu (opcional)
    private List<String> opcoes;            // Lista de opções
    private List<PreCondition> disponivel;  // Lista de pré-condições
    private List<Handler> handlers;         // Lista de handlers

    // Construtor

    /**
     * Constructor vazio para objectos da classe Menu.
     * <p>
     * Cria um menu vazio, ao qual se podem adicionar opções.
     */
    public Menu() {
        this.titulo = "Menu";
        this.opcoes = new ArrayList<>();
        this.disponivel = new ArrayList<>();
        this.handlers = new ArrayList<>();
    }

    /**
     * Constructor para objectos da classe Menu (com título e List de opções).
     * <p>
     * Cria um menu de opções sem event handlers.
     * Utilização de listas é útil para definir menus dinâmicos.
     *
     * @param titulo O titulo do menu
     * @param opcoes Uma lista de Strings com as opções do menu.
     */
    public Menu(String titulo, List<String> opcoes) {
        this.titulo = titulo;
        this.opcoes = new ArrayList<>(opcoes);
        this.disponivel = new ArrayList<>();
        this.handlers = new ArrayList<>();
        this.opcoes.forEach(s -> {
            this.disponivel.add(() -> true);
            this.handlers.add(() -> System.out.println("\n Opção não implementada!"));
        });
    }

    public Menu(List<String> opcoes) {
        this("Menu", opcoes);
    }

    public Menu(String titulo, String[] opcoes) {
        this(titulo, Arrays.asList(opcoes));
    }

    /**
     * Constructor para objectos da classe Menu (sem título e com array de opções).
     * <p>
     * Cria um menu de opções sem event handlers.
     * Utilização de arrays é útil para definir menus estáticos. P.e.:
     * <p>
     * new Menu(String[]{
     * "Opção 1",
     * "Opção 2",
     * "Opção 3"
     * })
     *
     * @param opcoes Um array de Strings com as opções do menu.
     */
    public Menu(String[] opcoes) {
        this(Arrays.asList(opcoes));
    }

    // Métodos de instância

    /**
     * Adicionar opções a um Menu.
     *
     * @param name A opção a apresentar.
     * @param p    A pré-condição da opção.
     * @param h    O event handler para a opção.
     */
    public void option(String name, PreCondition p, Handler h) {
        this.opcoes.add(name);
        this.disponivel.add(p);
        this.handlers.add(h);
    }

    /**
     * Correr o menu multiplas vezes.
     * <p>
     * Termina com a opção 0 (zero).
     */
    public void run(Client c) {
        int op;

        do {
            if (c.isAutenticado()) {
                System.out.println(ClientUI.ANSI_BOLD + "User " + c.getUserAutenticado().getUserName() + " autenticado" + ClientUI.ANSI_RESET);
            } else {
                System.out.println(ClientUI.ANSI_BOLD + "User não autenticado" + ClientUI.ANSI_RESET);
            }

            show();
            op = readOption();

            if (op > 0 && !this.disponivel.get(op - 1).validate()) {
                // TODO: Warning
                System.out.println("O servidor vai dar erro mas podes sempre tentar");
            }

            try {
                try {
                    this.handlers.get(op - 1).execute();
                } catch (NoSuchElementException e) {
                    ClientUI.changeScanner();
                }
                // ctrl-d por exemplo
                catch (IndexOutOfBoundsException e) {
                    return;
                }
            } catch (PermissionDeniedException e) {
                // TODO: Logger
                System.out.println("Server: Nenhum User Autenticado");
            }
        } while (op != 0);
    }

    /**
     * Método que regista uma uma pré-condição numa opção do menu.
     *
     * @param i índice da opção (começa em 1)
     * @param b pré-condição a registar
     */
    public void setPreCondition(int i, PreCondition b) {
        this.disponivel.set(i - 1, b);
    }

    /**
     * Método para registar um handler numa opção do menu.
     *
     * @param i indice da opção  (começa em 1)
     * @param h handlers a registar
     */
    public void setHandler(int i, Handler h) {
        this.handlers.set(i - 1, h);
    }

    // Métodos auxiliares

    /**
     * Apresentar o menu
     */
    private void show() {
        System.out.println("\n" + ClientUI.ANSI_YELLOW + ClientUI.ANSI_BOLD + "*** " + this.titulo + " ***" + ClientUI.ANSI_RESET);
        for (int i = 0; i < this.opcoes.size(); i++) {
            String option = (i + 1) + " - ";
            if (this.disponivel.get(i).validate()) {
                option = ClientUI.ANSI_BOLD + option + ClientUI.ANSI_GREEN + this.opcoes.get(i) + ClientUI.ANSI_RESET;
            } else {
                option = option + ClientUI.ANSI_RED + this.opcoes.get(i) + ClientUI.ANSI_RESET;
            }
            System.out.println(option);
        }
    }

    /**
     * Ler uma opção válida
     */
    private int readOption() {
        int op;
        do {
            System.out.print(ClientUI.ANSI_BOLD + ClientUI.ANSI_YELLOW + "❯ " + ClientUI.ANSI_RESET + ClientUI.ANSI_BOLD +
                    "Opção: " + ClientUI.ANSI_RESET);
            try {
                try {
                    String line = is.nextLine();
                    op = Integer.parseInt(line);
                } catch (NoSuchElementException e) {
                    return 0;
                }
            } catch (NumberFormatException e) {
                op = -1;
            }
            if (op < 0 || op > this.opcoes.size()) {
                System.out.println(ClientUI.ANSI_RED + ClientUI.ANSI_BOLD + "Opção Inválida!" + ClientUI.ANSI_RESET);
                op = -1;
            }
        } while (op == -1);
        return op;
    }
}
