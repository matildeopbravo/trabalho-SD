package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

public abstract class ClientPacket {
    private int id;

    abstract Operation getType();

    protected ClientPacket() {
        // Protected, serve só para chamar com super() nas subclasses
        this.id = new Random().nextInt();
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(getType().getValue());
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
            case ListaVoos -> PedeListaVoosPacket.from(in);
        };

        p.id = id;

        return p;
    }
}
