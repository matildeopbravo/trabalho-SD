package sd.packets.server;

import sd.server.Reserva;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ListaReservasReply extends ServerReply {
    private final Set<Reserva> reservas;

    public ListaReservasReply(int id, Status status, Set<Reserva> reservas) {
        super(id, status);
        this.reservas = reservas;
    }

    public static ListaReservasReply from(int id, Status status, DataInputStream in) throws IOException {
        int size = in.readInt();
        HashSet<Reserva> reservas = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            reservas.add(Reserva.deserialize(in));
        }

        return new ListaReservasReply(id, status, reservas);
    }

    @Override
    public ServerPacketType getType() {
        return ServerPacketType.ListaReservas;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(reservas.size());
        for (Reserva r : reservas) {
            r.serialize(out);
        }
    }

    public Set<Reserva> getReservas() {
        return reservas;
    }
}
