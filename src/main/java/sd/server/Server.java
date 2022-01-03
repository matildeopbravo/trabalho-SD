package sd.server;

import sd.client.ClientUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Server {
    private ServerSocket serverSocket;
    // username -> info
    private static HashMap<String, ServerUser> users;
    // id voo -> voo
    private static HashMap<String,Voo> voos;
    // idReserva -> Reserva
    private static HashMap<String, Reserva> reservas;

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
                serverUser.setStreams(in,out);
            }
        } catch (IOException e) {
            Reply.Failure.serialize(out);
        }
        return null;
    }
    public static void efetuaReserva(DataInputStream in, DataOutputStream out) {
        try {
            Reserva r = Reserva.deserialize(in);
            // criar a reserva e responder
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    }
}
