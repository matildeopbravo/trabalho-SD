package sd.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ClientUser {
    private String username;
    private String password;

    public ClientUser(String username, String password) {
        this.username = username;
        this.password = password;
    }


    public void serialize(DataOutputStream out) throws IOException {
            out.writeUTF(username);
            out.writeUTF(password);
    }

    public static ClientUser deserialize(DataInputStream in) throws IOException {
        return new ClientUser(in.readUTF(),in.readUTF());
    }

    public String getUserName() {
        return username;
    }
}
