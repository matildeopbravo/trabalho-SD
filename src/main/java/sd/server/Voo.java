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
    private Voo(int id, long capacidade , LocalDate data, VooTabelado vooTabelado) {
        this.id = id;
        this.capacidade = capacidade;
        this.data = data;
        this.vooTabelado = vooTabelado;
    }

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

    public void diminuiCapacidade() {
        capacidade--;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeLong(capacidade);
        out.writeUTF(data.toString());
        vooTabelado.serialize(out);
    }
    public static Voo deserialize(DataInputStream in) throws IOException {
        int id = in.readInt();
        long capacidade = in.readLong();
        LocalDate data = LocalDate.parse(in.readUTF());
        var voo = VooTabelado.deserialize(in);
        return new Voo(id,capacidade,data,voo);
    }
}
