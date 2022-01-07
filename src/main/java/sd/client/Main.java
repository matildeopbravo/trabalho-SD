package sd.client;

import sd.client.ui.ClientUI;
import sun.misc.Signal;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Main {
    public static void main(String[] args) throws IOException {

        String address = "127.0.0.1";
        int port = 2500;
        if(args.length > 0) {
            address = args[0];
            if(args.length > 1)
                port = Integer.parseInt(args[1]);
        }
        try {
            ClientUI ui = new ClientUI(address,port);
            ui.run();
        }
        catch(SocketException e) {

            System.err.println(ClientUI.ANSI_RED + "Servidor Desconectado" + ClientUI.ANSI_RESET );
        }
    }
}
