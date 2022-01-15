package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ListaReservasPacket extends ClientPacket {
    public ListaReservasPacket() {
        super();
    }

    public static ListaReservasPacket from(DataInputStream in) {
        return new ListaReservasPacket();
    }

    @Override
    public Operation getType() {
        return Operation.ListaReservas;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        // Pacote vazio
    }
}
