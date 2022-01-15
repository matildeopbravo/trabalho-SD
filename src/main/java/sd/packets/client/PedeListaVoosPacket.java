package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PedeListaVoosPacket extends ClientPacket {
    public PedeListaVoosPacket() {
        super();
    }

    public static PedeListaVoosPacket from(DataInputStream in) {
        return new PedeListaVoosPacket();
    }

    @Override
    Operation getType() {
        return Operation.ListaVoos;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        // Pacote vazio
    }
}
