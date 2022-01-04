package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Reserva {
    private static long lastCodigo = -1;

    private long codigoReserva;
    private ServerUser usr;
    private Set<Voo> voos;

    public Reserva(ServerUser usr, Set<Voo> voos) {
        this.usr = usr;
        this.codigoReserva = ++lastCodigo;
        this.voos = voos;
    }

    //public void serialize(DataOutputStream out) throws IOException {
    //    user.serialize(out);
    //    out.writeInt(voos.size());
    //    for (Voo v : voos) {
    //        v.serialize(out);
    //    }
    //}

    //public static Reserva deserialize(DataInputStream in) throws IOException {
    //    ServerUser user = ServerUser.deserialize();
    //    int size = in.readInt();
    //    List<Voo> voos = new ArrayList<>(size);
    //    for(int i = 0; i < size ; i++) {
    //        voos.add(Voo.deserialize(in));
    //    }
    //    return new Reserva(user,voos);
    //}
}
