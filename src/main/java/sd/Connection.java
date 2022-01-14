package sd;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetAddress;

public class Connection {
    private InetAddress add;
    private int port;
    private DataInputStream in;
    private DataOutputStream out;

    public Connection(DataInputStream in, DataOutputStream out) {
        this.in = in;
        this.out = out;
    }
}
