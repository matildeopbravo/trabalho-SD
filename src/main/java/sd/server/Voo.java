package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class Voo {
   public static int lastId= -1;
   private int id;
   private long capacidade;
   private LocalDate data;
   private VooTabelado vooTabelado;

    public Voo(LocalDate data, VooTabelado vooTabelado) {
        this.id = ++lastId;
        this.capacidade = vooTabelado.getCapacidade();
        this.data = data;
        this.vooTabelado = vooTabelado;
    }

    //public static Voo deserialize(DataInputStream in) throws IOException {
    //    return new Voo(in.readInt(), in.readUTF(),in.readUTF(),in.readLong(),LocalDate.parse(in.readUTF()));
    //}

    //public static Voo deserializeWithoutID(DataInputStream in) throws IOException {
    //    return new Voo(in.readUTF(),in.readUTF(),in.readLong(),LocalDate.parse(in.readUTF()));
    //}

    public int getID() {
        return id;
    }

    public LocalDate getData() {
        return this.data;
    }

    public int getId() {
        return id;
    }

    public long getCapacidade() {
        return capacidade;
    }

    public String getOrigem() {
        return vooTabelado.getOrigem();
    }
    public String getDestino() {
        return vooTabelado.getDestino();
    }

    public VooTabelado getVooTabelado() {
        return vooTabelado;
    }
}
