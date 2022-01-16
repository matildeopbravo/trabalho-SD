package sd.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TipoUserAutenticadoReply extends ServerReply {
    private final boolean isAdmin;

    public TipoUserAutenticadoReply(int id, Status status, boolean isAdmin) {
        super(id, status);
        this.isAdmin = isAdmin;
    }

    public static TipoUserAutenticadoReply from(int id, Status status, DataInputStream in) throws IOException {
        boolean isAdmin = in.readBoolean();
        return new TipoUserAutenticadoReply(id, status, isAdmin);
    }

    @Override
    public ServerPacketType getType() {
        return ServerPacketType.TipoDeUserAutenticado;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeBoolean(this.isAdmin);
    }

    public boolean getIsAdmin() {
        return isAdmin;
    }
}
