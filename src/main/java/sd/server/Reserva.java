package sd.server;

import sd.IDGenerator;
import sd.client.ClientUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Reserva {
    public static IDGenerator idGen = new IDGenerator();
    private final int codigoReserva;
    private final ClientUser usr;
    private final Set<Voo> voos;

    public Reserva(ClientUser usr, Set<Voo> voos) {
        this.usr = usr;
        this.codigoReserva = idGen.nextID();
        this.voos = voos;
    }

    private Reserva(int id, ClientUser usr, Set<Voo> voos) {
        this.usr = usr;
        this.codigoReserva = id;
        this.voos = voos;
    }

    public int getId() {
        return codigoReserva;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(codigoReserva);
        usr.serializeWithoutPassword(out);
        out.writeInt(voos.size());
        for (Voo v : voos) {
            v.serialize(out);
        }
    }

    public static Reserva deserialize(DataInputStream in) throws IOException {
        int id = in.readInt();
        var userName = ClientUser.deserealizeWithoutPasswd(in);

        int size = in.readInt();
        var voos = new HashSet<Voo>(size);
        for (int i = 0; i < size; i++) {
            voos.add(Voo.deserialize(in));
        }
        return new Reserva(id, userName, voos);
    }

    public ClientUser getClientUser() {
        return usr;
    }

    public Set<Voo> getVoos() {
        return voos;
    }

    public Reserva clone() {
        return new Reserva(this.codigoReserva, this.usr, this.voos);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reserva reserva = (Reserva) o;
        return codigoReserva == reserva.codigoReserva && Objects.equals(usr, reserva.usr) && Objects.equals(voos, reserva.voos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codigoReserva, usr, voos);
    }
}
