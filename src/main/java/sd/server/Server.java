package sd.server;

import sd.client.ClientUser;
import sd.client.ui.ClientUI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;

public class Server {
    private ServerSocket serverSocket;
    // username -> info
    private static HashMap<String, ServerUser> users;
    // id voo -> voo
    private static HashMap<Integer,Voo> voos;
    // idReserva -> Reserva
    private static HashMap<Integer, Reserva> reservas;

    public Server(int port, String address) {
        try {
            this.serverSocket = new ServerSocket(port, 50,InetAddress.getByName(address));
        } catch (IOException e) {
            e.printStackTrace();
        }
        users = new HashMap<>();
        users.put("admin",new ServerUser("admin","password", true));
        users.put("matilde", new ServerUser("matilde","bravo", false));
        voos = new HashMap<>();
        Voo voo1 = new Voo("Londres", "Amsterdão", 2500, LocalDate.now());
        Voo voo2 = new Voo("Berlim", "Lisboa", 2500, LocalDate.now());
        Voo voo3 = new Voo("Itália", "Lisboa", 2500, LocalDate.now().plusMonths(4));
        Voo voo4 = new Voo("Porto", "Itália", 2500, LocalDate.now().plusMonths(1));
        Voo voo5 = new Voo("Itália", "França", 2500, LocalDate.now().plusMonths(2));
        Voo voo6 = new Voo("França", "Lisboa", 2500, LocalDate.now().plusMonths(3));
        Voo voo7 = new Voo("Porto", "Lisboa", 2500, LocalDate.now().plusDays(1));
        Voo voo8 = new Voo("França", "Itália", 2500, LocalDate.now().plusMonths(3));
        voos.put(0,voo1);
        voos.put(1,voo2);
        voos.put(2,voo3);
        voos.put(3,voo4);
        voos.put(4,voo5);
        voos.put(5,voo6);
        voos.put(6,voo7);
        voos.put(7,voo8);
        HashSet<Voo> s1 = new HashSet<>();
        s1.add(voos.get(0));
        s1.add(voos.get(1));
        System.out.println(Voo.lastId);
        Reserva r1 = new Reserva( users.get("matilde"), s1);
        reservas = new HashMap<>();
        reservas.put(r1.getId(),r1);
    }

    public void start() {
        System.out.println("Server listening on " + serverSocket.getInetAddress().getHostAddress() +":" + serverSocket.getLocalPort() );
        while(true) {
            try {
                Socket s = serverSocket.accept();
                System.out.println("Client "   + ClientUI.ANSI_BLUE + s.getInetAddress().getHostAddress() + ClientUI.ANSI_RESET + " connected");
                new Thread(new Worker(s)).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isAuthenticated(Socket s) {
        String address = s.getInetAddress().getHostAddress();
        // TODO
        return true;

    }

    public static void registaUser(DataInputStream in, DataOutputStream out) {
        System.out.println("Recebido pedido de registar user");
        try {
            ClientUser us = ClientUser.deserialize(in);
            System.out.println("User: " + us.getUserName() + " password: "+ us.getPassword());
            if(users.containsKey(us.getUserName())) {
                System.out.println("Uitlizador já existe");
                Reply.Failure.serialize(out);
            }
            else {
                ServerUser su = new ServerUser(us);
                users.put(su.getUserName(),su);
                Reply.Success.serialize(out);
            }
        }
        catch (IOException e) {
            Reply.InvalidFormat.serialize(out);
        }
    }

    public static ServerUser autenticaUser(DataInputStream in, DataOutputStream out) {
        ServerUser serverUser = null;
        try {
            ClientUser clientUser = ClientUser.deserialize(in);
            serverUser = users.get(clientUser.getUserName());
            if(serverUser != null && serverUser.getPassword().equals(clientUser.getPassword())) {
                Reply.Success.serialize(out);
                out.writeBoolean(serverUser.isAdmin());
                serverUser.setIsAuthenticated(true);
                return serverUser;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Reply.Failure.serialize(out);
        return serverUser;
    }
    public static void efetuaReserva(DataInputStream in, DataOutputStream out) {
        try {
            int numCidades = in.read();
            var cidades =  new ArrayList<>(numCidades);
            for(int i = 0; i < numCidades ; i++) {
                cidades.add(in.readUTF());
            }
            LocalDate ini = LocalDate.parse(in.readUTF());
            LocalDate fi = LocalDate.parse(in.readUTF());
            Voo voo = descobreVoo(cidades,ini,fi);
            if( voo != null) {
                Reply.Codigo.serialize(out);
                out.writeInt(voo.getID());
            }
            else {
                Reply.Failure.serialize(out);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Voo descobreVoo(ArrayList<Object> cidades, LocalDate ini, LocalDate fi) {
        //TODO
        return null;
    }

    public static void mudaOrigem(DataInputStream in, DataOutputStream out) {
        // TODO
    }

    public static void mudaDestino(DataInputStream in, DataOutputStream out) {
        // TODO
    }
    public static void mudaCapacidade(DataInputStream in, DataOutputStream out) {
        // TODO
    }
    public static void encerraDia(DataInputStream in, DataOutputStream out) {
        // TODO
    }
    public static void listaVoos(DataInputStream in, DataOutputStream out) {
        try {
            out.writeInt(voos.size());
            for(Voo v : voos.values()) {
                v.serialize(out);
            }
        }
        catch (IOException e) {
            Reply.Failure.serialize(out);
        }
    }

    public static void cancelaReserva(DataInputStream dataInputStream, DataOutputStream dataOutputStream) {
        int id = 0;
        try {
            id = dataInputStream.readInt();
            // TODO verificar se pretence ao user
            Reserva r = reservas.get(id);
            if(r == null )
                Reply.Failure.serialize(dataOutputStream);
            else {
                reservas.remove(id);
                Reply.Success.serialize(dataOutputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public static void adicionaVoo(DataInputStream in, DataOutputStream out) {
        try {
            Voo v = Voo.deserializeWithoutID(in);
            voos.put(v.getID(),v);
            System.out.println("Adicionado voo com id " + v.getID());
            Reply.Success.serialize(out);
        } catch (IOException e) {
            Reply.Failure.serialize(out);
            e.printStackTrace();
        }

    }

    List<List<Voo>> percursosPossiveis (String origem, String destino) {
        return percursosPossiveis(origem, destino, 3, LocalDate.now());
    }

    List<List<Voo>> percursosPossiveis (String origem, String destino, int limiteVoos, LocalDate dataAtual) {
        List<List<Voo>> percursos = new ArrayList<>();

        for (Voo voo: voos.values()) {
            if (voo.getOrigem().equals(origem) && voo.getData().isAfter(dataAtual)) {
                if (voo.getDestino().equals(destino))
                    percursos.add(new ArrayList<>(List.of(voo)));

                else if (limiteVoos > 1) {
                    List<List<Voo>> percursosPosteriores =
                            percursosPossiveis(voo.getDestino(), destino, limiteVoos-1, voo.getData());
                    for (List<Voo> percursoPosterior : percursosPosteriores) {
                        List<Voo> percursoFinal = new ArrayList<>(List.of(voo));
                        percursoFinal.addAll(percursoPosterior);
                        percursos.add(percursoFinal);
                    }
                }
            }
        }

        return percursos;
    }
}
