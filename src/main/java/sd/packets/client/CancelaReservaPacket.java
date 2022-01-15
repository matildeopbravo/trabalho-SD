package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class CancelaReservaPacket extends ClientPacket {
    private int idReserva;

    public int getIdReserva() {
        return this.idReserva;
    }

    public CancelaReservaPacket(int id) {
        super();
        this.idReserva = id;
    }

    public static CancelaReservaPacket from(DataInputStream in) throws IOException {
        int id = in.readInt();
        return new CancelaReservaPacket(id);
    }

    @Override
    public Operation getType() {
        return Operation.CancelaReserva;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(idReserva);
    }
}
