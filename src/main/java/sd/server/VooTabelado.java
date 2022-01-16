package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Objects;

public class VooTabelado implements Comparable<VooTabelado>{
    private final String origem;
    private final String destino;
    private final long capacidade;


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

    public VooTabelado clone(){
        return new VooTabelado(this.origem,this.destino,this.capacidade);
    }

    @Override
    public int compareTo(VooTabelado other) {
        int origem = this.origem.compareTo(other.origem);
        if (origem != 0) return origem;
        int destino = this.destino.compareTo(other.destino);
        if (destino != 0) return destino;
        return Long.compare(this.capacidade, other.capacidade);
    }
}
