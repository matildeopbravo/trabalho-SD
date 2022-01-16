package sd.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class ServerReply {
    public enum Status {
        Success,
        Failure,
        InvalidFormat
    }

    public enum ServerPacketType {
        Status,
        ListaVoos,
        ListaReservas,
        ListaPercursos,
        ReservaEfetuada,
        Notificacao,
        TipoDeUserAutenticado, // isAdmin
        ListaUsers
    }

    private final int id;
    private final Status status;

    public ServerReply(int id, Status status) {
        this.id = id;
        this.status = status;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(getType().ordinal());
        out.writeInt(id);
        out.writeInt(status.ordinal());

        if (status == Status.Success) {
            this.writeTo(out);
        }
    }

    public static ServerReply deserialize(DataInputStream in) throws IOException {
        ServerPacketType type = ServerPacketType.values()[in.readInt()];
        int id = in.readInt();
        Status status = Status.values()[in.readInt()];

        if (status == Status.Success) {
            return switch (type) {
                case Status -> StatusReply.from(id, status, in);
                case ListaVoos -> ListaVoosReply.from(id, status, in);
                case ListaReservas -> ListaReservasReply.from(id, status, in);
                case ListaPercursos -> ListaPercursosReply.from(id, status, in);
                case ReservaEfetuada -> ReservaEfetuadaReply.from(id, status, in);
                case Notificacao -> NotificacaoReply.from(id, status, in);
                case TipoDeUserAutenticado -> TipoUserAutenticadoReply.from(id, status, in);
                case ListaUsers -> ListaUsersReply.from(id,status,in);
            };
        } else {
            // Classe fake que devolve o tipo certo, mas que não tem dados
            return new ServerReply(id, status) {
                @Override
                public ServerPacketType getType() {
                    return type;
                }

                @Override
                protected void writeTo(DataOutputStream out) {}
            };
        }
    }

    public int getId() {
        return this.id;
    }

    public Status getStatus() {
        return this.status;
    }

    public abstract ServerPacketType getType();
    protected abstract void writeTo(DataOutputStream out) throws IOException;

}
