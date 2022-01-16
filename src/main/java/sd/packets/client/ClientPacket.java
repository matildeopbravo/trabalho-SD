package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

public abstract class ClientPacket {
    private int id;

    public abstract Operation getType();

    protected ClientPacket() {
        // Protected, serve só para chamar com super() nas subclasses
        // Fazer +1 garante que não temos de nos preocupar com o id ser 0 e colidir com notificações
        this.id = new Random().nextInt() + 1;
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
        };
        p.id = id;

        return p;
    }

    public int getId() {
        return this.id;
    }
}
