package sd.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class ReservaEfetuadaReply extends ServerReply {
    private final int reserva;
    private final LocalDate data;

    public ReservaEfetuadaReply(int id, Status status, int reserva, LocalDate data) {
        super(id, status);
        this.reserva = reserva;
        this.data = data;
    }

    public static ReservaEfetuadaReply from(int id, Status status, DataInputStream in) throws IOException {
        int reserva = in.readInt();
        LocalDate data = LocalDate.parse(in.readUTF());

        return new ReservaEfetuadaReply(id, status, reserva, data);
    }

    @Override
    ServerPacketType getType() {
        return ServerPacketType.ReservaEfetuada;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(reserva);
        out.writeUTF(data.toString());
    }
}
