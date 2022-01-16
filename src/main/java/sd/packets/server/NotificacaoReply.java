package sd.packets.server;

import com.sun.net.httpserver.Authenticator;
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

    public NotificacaoReply(String mensagem) {
        super(0, Status.Success);
        this.mensagem = mensagem;
    }

    public static NotificacaoReply from(int id, ServerReply.Status status, DataInputStream in) throws IOException {
        String mensagem = in.readUTF();
        return new NotificacaoReply(mensagem);
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