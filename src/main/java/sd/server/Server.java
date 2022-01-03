package sd.server;

import sd.client.ClientUser;
import sd.client.Operation;
import sd.exceptions.UserJaExisteException;

import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class Server implements  Runnable{
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

    @Override
    public void run() {
        while(true) {
            try {
                Socket s = serverSocket.accept();
                DataInputStream in = new DataInputStream(s.getInputStream());
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                try {
                    Operation op = Operation.values()[in.readInt()];
                    op.callHandleMethod(in, s);
                }
                catch (ArrayIndexOutOfBoundsException e ) {
                    Reply.InvalidFormat.serialize(out);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void registaUser(DataInputStream in, Socket s) {
        DataOutputStream out;
        try {
            OutputStream os = s.getOutputStream();
            out = new DataOutputStream(os);
        }
        catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            ClientUser us = ClientUser.deserialize(in);
            if(users.containsKey(us.getUserName()))
                Reply.Failure.serialize(out);
            else {
                ServerUser su = new ServerUser(us,s);
                users.put(su.getUserName(),su);
                Reply.Success.serialize(out);
            }
        }
        catch (IOException e) {
            Reply.InvalidFormat.serialize(out);
        }
    }

    public static void autenticaUser(DataInputStream in, Socket s) {
        // associa socket ao user

    }
    public static void efetuaReserva(DataInputStream in, Socket s) {

    }
    public static void mudaOrigem(DataInputStream in, Socket s) {

    }

    public static void mudaDestino(DataInputStream in, Socket s) {

    }
    public static void mudaCapacidade(DataInputStream in, Socket s) {

    }
    public static void encerraDia(DataInputStream in, Socket s) {

    }
    public static void listaVoos(DataInputStream in, Socket s) {

    }
}
