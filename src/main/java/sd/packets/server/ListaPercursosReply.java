package sd.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ListaPercursosReply extends ServerReply {
    private final List<List<String>> percursos;

    public ListaPercursosReply(int id, Status status, List<List<String>> percursos) {
        super(id, status);
        this.percursos = percursos;
    }

    public static ListaPercursosReply from(int id, Status status, DataInputStream in) throws IOException {
        int size = in.readInt();
        List<List<String>> percursos = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            int sizePercurso = in.readInt();
            List<String> percurso = new ArrayList<>(sizePercurso);
            for (int j = 0; j < sizePercurso; j++) {
                percurso.add(in.readUTF());
            }
            percursos.add(percurso);
        }

        return new ListaPercursosReply(id, status, percursos);
    }

    @Override
    public ServerPacketType getType() {
        return ServerPacketType.ListaPercursos;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(percursos.size());
        for (List<String> p : percursos) {
            out.writeInt(p.size());
            for (String c : p) {
                out.writeUTF(c);
            }
        }
    }

    public List<List<String>> getPercursos() {
        return this.percursos;
    }
}
