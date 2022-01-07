package sd.server;

import sd.client.ui.ClientUI;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class Voo {
   public static int lastId= -1;
   private int id;
   private String origem;
   private String destino;
   private long capacidade;
   private LocalDate data;


    public Voo(String origem, String destino, long capacidade, LocalDate data) {
        this.id = ++lastId;
        this.origem = origem;
        this.destino = destino;
        this.capacidade = capacidade;
        this.data = data;
    }
    public Voo(int id, String origem, String destino, long capacidade, LocalDate data) {
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.capacidade = capacidade;
        this.data = data;
    }

    public static Voo deserialize(DataInputStream in) throws IOException {
        return new Voo(in.readInt(), in.readUTF(),in.readUTF(),in.readLong(),LocalDate.parse(in.readUTF()));
    }

    public static Voo deserializeWithoutID(DataInputStream in) throws IOException {
        return new Voo(in.readUTF(),in.readUTF(),in.readLong(),LocalDate.parse(in.readUTF()));
    }

    public void serializeWithoutID(DataOutputStream out) throws IOException {
        out.writeUTF(origem);
        out.writeUTF(destino);
        out.writeLong(capacidade);
        out.writeUTF(data.toString());
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(id);
        serializeWithoutID(out);
    }

    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        return id + ": " + origem + " -> " + destino + "; Capacidade: " + capacidade + " Data: " + data;
    }

    public String getOrigem() {
        return this.origem;
    }

    public String getDestino() {
        return this.destino;
    }

    public LocalDate getData() {
        return this.data;
    }
}
