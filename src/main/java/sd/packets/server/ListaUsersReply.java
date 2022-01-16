package sd.packets.server;

import sd.server.ServerUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ListaUsersReply extends ServerReply {
    private final Set<ServerUser> users;

    public ListaUsersReply(int id, Status status, Set<ServerUser> users) {
        super(id, status);
        this.users = users;
    }

    public static ListaUsersReply from(int id, Status status, DataInputStream in) throws IOException {
        int size = in.readInt();
        HashSet<ServerUser> users = new HashSet<>(size);
        for (int i = 0; i < size; i++) {
            users.add(ServerUser.deserialize(in));
        }

        return new ListaUsersReply(id, status, users);
    }

    @Override
    public ServerPacketType getType() {
        return ServerPacketType.ListaUsers;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(users.size());
        for (ServerUser r : users) {
            r.serialize(out);
        }
    }

    public Set<ServerUser> getUsers() {
        return users;
    }
}
