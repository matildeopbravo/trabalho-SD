package sd.server;

import sd.client.ClientUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
            this.serverSocket = new ServerSocket(port, 50,
                    InetAddress.getByAddress(address.getBytes(StandardCharsets.UTF_8)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        users = new HashMap<>();
        users.put("admin",new ServerUser("admin","password", true));
        voos = new HashMap<>();
        reservas = new HashMap<>();
    }

    public void start() {
        while(!serverSocket.isClosed()) {
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
        try {
            ClientUser us = ClientUser.deserialize(in);
            if(users.containsKey(us.getUserName()))
                Reply.Failure.serialize(out);
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
        try {
            ClientUser clientUser = ClientUser.deserialize(in);
            ServerUser serverUser = users.get(clientUser.getUserName());
            if(serverUser != null && serverUser.getPassword().equals(clientUser.getPassword())) {
                serverUser.setIsAuthenticated(true);
            }
        } catch (IOException e) {
            Reply.Failure.serialize(out);
        }
        return null;
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
    }
}
