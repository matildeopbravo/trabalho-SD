package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ListaVoosPacket extends ClientPacket {
    public ListaVoosPacket() {
        super();
    }

    public static ListaVoosPacket from(DataInputStream in) {
        return new ListaVoosPacket();
    }

    @Override
    public Operation getType() {
        return Operation.ListaVoos;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        // Pacote vazio
    }
}
