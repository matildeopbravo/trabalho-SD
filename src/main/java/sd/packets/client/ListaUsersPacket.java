package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ListaUsersPacket extends ClientPacket {
    public ListaUsersPacket() {
        super();
    }

    public static ListaUsersPacket from(DataInputStream in) {
        return new ListaUsersPacket();
    }

    @Override
    public Operation getType() {
        return Operation.ListaUsers;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        // Pacote vazio
    }
}
