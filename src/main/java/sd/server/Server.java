package sd.server;

import sd.client.ClientUser;
import sd.client.ui.Menu;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Server {
    private ServerSocket serverSocket;
    // username -> info
    private static HashMap<String, ServerUser> users;
    // id voo -> voo
    private static HashMap<String,Voo> voos;
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
        voos.put("abcd", new Voo("abcd","Londres", "Amsterdão", 2500, LocalDate.now()));
        voos.put("efgh", new Voo("efgh","Berlim", "Lisboa", 3000, LocalDate.now()));
        HashSet<Voo> s1 = new HashSet<>();
        s1.add(voos.get("efgh"));
        s1.add(voos.get("abcd"));
        Reserva r1 = new Reserva( users.get("matilde"), s1);
        reservas = new HashMap<>();
        reservas.put(r1.getId(),r1);
    }

    public static void main(String [] args) {
        Server s = new Server(2500,"127.0.0.1");
        Menu menu = new Menu();
        s.start();
    }

    public void start() {
        System.out.println("Server listening on " + serverSocket.getInetAddress().getHostAddress() +":" + serverSocket.getLocalPort() );
        while(true) {
            try {
                Socket s = serverSocket.accept();
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
            String idVoo = descobreVoo(cidades,ini,fi).getID();
            if( idVoo != null) {
                Reply.Codigo.serialize(out);
                out.writeUTF(idVoo);
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
}
