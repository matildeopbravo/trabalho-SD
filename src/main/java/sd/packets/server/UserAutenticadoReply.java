package sd.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class UserAutenticadoReply extends ServerReply {
    private final boolean isAdmin;

    public UserAutenticadoReply(int id, Status status, boolean isAdmin) {
        super(id, status);
        this.isAdmin = isAdmin;
    }

    public static UserAutenticadoReply from(int id, Status status, DataInputStream in) throws IOException {
        boolean isAdmin = in.readBoolean();
        return new UserAutenticadoReply(id, status, isAdmin);
    }

    @Override
    ServerPacketType getType() {
        return ServerPacketType.UserAutenticado;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeBoolean(this.isAdmin);
    }
}
