package sd.packets.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class StatusReply extends ServerReply {
    public StatusReply(int id, Status status) {
        super(id, status);
    }

    public static StatusReply from(int id, Status status, DataInputStream in) {
        return new StatusReply(id, status);
    }

    @Override
    ServerPacketType getType() {
        return ServerPacketType.Status;
    }

    @Override
    protected void writeTo(DataOutputStream out) {}
}
