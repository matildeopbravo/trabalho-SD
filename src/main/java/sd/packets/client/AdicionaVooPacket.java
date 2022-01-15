package sd.packets.client;

import sd.Operation;
import sd.server.VooTabelado;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class AdicionaVooPacket extends ClientPacket {
    private final VooTabelado vooTabelado;

    public VooTabelado getVooTabelado() {
        return this.vooTabelado;
    }

    public AdicionaVooPacket(VooTabelado vooTabelado) {
        super();
        this.vooTabelado = vooTabelado;
    }

    public static AdicionaVooPacket from(DataInputStream in) throws IOException {
        VooTabelado v = VooTabelado.deserialize(in);
        return new AdicionaVooPacket(v);
    }

    @Override
    public Operation getType() {
        return Operation.AdicionaVoo;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        vooTabelado.serialize(out);
    }
}
