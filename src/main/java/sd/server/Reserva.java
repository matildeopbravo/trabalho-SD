package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reserva {
    private ServerUser user;
    private List<Voo> voos;

    public Reserva(ServerUser user, List<Voo> voos) {
        this.user = user;
        this.voos = voos;
    }
    public void serialize(DataOutputStream out) throws IOException {
        user.serialize(out);
        out.writeInt(voos.size());
        for (Voo v : voos) {
            v.serialize(out);
        }
    }

    public static Reserva deserialize(DataInputStream in) throws IOException {
        ServerUser user = ServerUser.deserialize();
        int size = in.readInt();
        List<Voo> voos = new ArrayList<>(size);
        for(int i = 0; i < size ; i++) {
            voos.add(Voo.deserialize(in));
        }
        return new Reserva(user,voos);
    }
}
