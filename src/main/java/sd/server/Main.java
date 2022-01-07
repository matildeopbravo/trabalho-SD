package sd.server;

import sd.client.ui.Menu;

public class Main {
    public static void main(String [] args) {
        String address = "127.0.0.1";
        int port = 2500;
        if(args.length > 0) {
            address = args[0];
            if(args.length > 1)
                port = Integer.parseInt(args[1]);
        }
        Server s = new Server(port, address);
        s.start();
    }
}
