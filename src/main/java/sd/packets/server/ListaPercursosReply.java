package sd.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListaPercursosReply extends ServerReply {
    private final List<String> percurso;

    public ListaPercursosReply(int id, Status status, List<String> percurso) {
        super(id, status);
        this.percurso = percurso;
    }

    public static ListaPercursosReply from(int id, Status status, DataInputStream in) throws IOException {
        int size = in.readInt();
        List<String> percurso = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            percurso.add(in.readUTF());
        }

        return new ListaPercursosReply(id, status, percurso);
    }

    @Override
    ServerPacketType getType() {
        return ServerPacketType.ListaPercursos;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(percurso.size());
        for (String c : percurso) {
            out.writeUTF(c);
        }
    }
}
