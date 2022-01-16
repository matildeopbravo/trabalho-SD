package sd.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientTest {
    public static void main(String [] args) throws IOException {
        Socket s = new Socket("localhost",2500);
        //DataOutputStream ds = new DataOutputStream(s.getOutputStream());
        //ds.writeUTF("Bom dia");
        //System.out.println(s.getLocalPort());

    }
}
