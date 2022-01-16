package sd.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

// a unica informacao sobre o user que o cliente. O servidor guarda um client user
public class ClientUser {
    private final String username;
    private final String password;

    public ClientUser(String username, String password) {
        this.username = username;
        this.password = password;
    }
    private ClientUser(String username ) {
        this.username = username;
        this.password = null;
    }

    public void serialize(DataOutputStream out) throws IOException {
            out.writeUTF(username);
            out.writeUTF(password);
    }
    public static ClientUser deserialize(DataInputStream in) throws IOException {
        return new ClientUser(in.readUTF(),in.readUTF());
    }
    public void serializeWithoutPassword(DataOutputStream out) throws IOException {
        out.writeUTF(username);
    }
    public static ClientUser deserealizeWithoutPasswd(DataInputStream in) throws IOException {
        return new ClientUser(in.readUTF());
    }

    public String getUserName() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
