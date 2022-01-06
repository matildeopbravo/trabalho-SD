package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class Voo {
   private String id;
   private String origem;
   private String destino;
   private long capacidade;
   private LocalDate data;


    public Voo(String id, String origem, String destino, long capacidade, LocalDate data) {
        this.id = id;
        this.origem = origem;
        this.destino = destino;
        this.capacidade = capacidade;
        this.data = data;
    }

    public static Voo deserialize(DataInputStream in) throws IOException {
        return new Voo(in.readUTF(),in.readUTF(),in.readUTF(),in.readLong(),LocalDate.parse(in.readUTF()));
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(id);
        out.writeUTF(origem);
        out.writeUTF(destino);
        out.writeLong(capacidade);
        out.writeUTF(data.toString());
    }

    public String getID() {
        return id;
    }

    @Override
    public String toString() {
        return id + ": " + origem + " -> " + destino + "; Capacidade: " + capacidade + " Data: " + data;
    }
}
