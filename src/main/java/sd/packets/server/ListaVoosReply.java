package sd.packets.server;

import sd.server.VooTabelado;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListaVoosReply extends ServerReply {
    private final List<VooTabelado> voos;

    public ListaVoosReply(int id, Status status, List<VooTabelado> voos) {
        super(id, status);
        this.voos = voos;
    }


    public static ListaVoosReply from(int id, Status status, DataInputStream in) throws IOException {
        int size = in.readInt();
        List<VooTabelado> lista = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            lista.add(VooTabelado.deserialize(in));
        }

        return new ListaVoosReply(id, status, lista);
    }

    @Override
    ServerPacketType getType() {
        return ServerPacketType.ListaVoos;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(voos.size());
        for (VooTabelado v : voos) {
            v.serialize(out);
        }
    }
}
