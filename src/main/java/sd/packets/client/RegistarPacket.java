package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class RegistarPacket extends ClientPacket {
    private final String username;
    private final String password;

    public RegistarPacket(String username, String password) {
        super();
        this.username = username;
        this.password = password;
    }

    public static RegistarPacket from(DataInputStream in) throws IOException {
        String username = in.readUTF();
        String password = in.readUTF();

        return new RegistarPacket(username, password);
    }

    @Override
    Operation getType() {
        return Operation.Registar;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeUTF(username);
        out.writeUTF(password);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
