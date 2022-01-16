package sd.packets.server;

import sd.Pair;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class NotificacaoReply extends ServerReply {
    private final String mensagem;

    public String getMensagem() {
        return this.mensagem;
    }

    public NotificacaoReply(int id, Status status, String mensagem) {
        super(id, status);
        this.mensagem = mensagem;
    }

    public static NotificacaoReply from(int id, Status status, DataInputStream in) throws IOException {
        String mensagem = in.readUTF();
        return new NotificacaoReply(id, status, mensagem);
    }

    @Override
    ServerPacketType getType() {
        return ServerPacketType.Notificacao;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeUTF(mensagem);
    }
}