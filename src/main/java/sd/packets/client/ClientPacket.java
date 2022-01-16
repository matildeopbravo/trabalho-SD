package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class ClientPacket {
    private int id;
    private static int lastId = 1;

    public abstract Operation getType();

    protected ClientPacket() {
        // Protected, serve só para chamar com super() nas subclasses
        this.id = lastId++;
    }

    public void serialize(DataOutputStream out) throws IOException {
        getType().serialize(out);
        out.writeInt(id);
        this.writeTo(out);
    }

    protected abstract void writeTo(DataOutputStream out) throws IOException;

    public static ClientPacket deserialize(DataInputStream in) throws IOException {
        Operation type = Operation.getFromFakeOrdinal(in.readInt());
        int id = in.readInt();

        ClientPacket p = switch (type) {
            case Registar -> RegistarPacket.from(in);
            case Login -> LoginPacket.from(in);
            case LogOut -> LogOutPacket.from(in);
            case Reserva -> ReservaPacket.from(in);
            case CancelaReserva -> CancelaReservaPacket.from(in);
            case AdicionaVoo -> AdicionaVooPacket.from(in);
            case Encerramento -> EncerramentoPacket.from(in);
            case ListaVoos -> ListaVoosPacket.from(in);
            case ListaReservas -> ListaReservasPacket.from(in);
            case PercursosPossiveis -> TodosPercursosPacket.from(in);
            case ListaUsers -> ListaUsersPacket.from(in);
        };
        p.id = id;

        return p;
    }

    public int getId() {
        return this.id;
    }
}
