package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class VooTabelado {
    private String origem;
    private String destino;
    private long capacidade;


    public VooTabelado(String origem, String destino, long capacidade) {
        this.origem = origem;
        this.destino = destino;
        this.capacidade = capacidade;
    }

    public static VooTabelado deserialize(DataInputStream in) throws IOException {
        return new VooTabelado(in.readUTF(),in.readUTF(),in.readLong());
    }

    @Override
    public boolean equals(Object o){
        if(o == null || (o.getClass() != this.getClass())) return false;
        final VooTabelado voo = (VooTabelado) o;
        return voo.origem.equals(origem) && voo.destino.equals(destino) && voo.capacidade == capacidade;

    }

    public String getOrigem() {
        return origem;
    }

    public String getDestino() {
        return destino;
    }

    public long getCapacidade() {
        return capacidade;
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeUTF(origem);
        out.writeUTF(destino);
        out.writeLong(capacidade);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origem, destino, capacidade);
    }
}
