package sd;

import sd.server.Voo;
import sd.server.VooTabelado;

import java.util.Objects;

public class OrigemDestino {
    private final String origem;
    private final String destino;

    public OrigemDestino(String origem, String destino) {
        this.origem = origem;
        this.destino = destino;
    }

    public OrigemDestino(VooTabelado v) {
        this.origem = v.getOrigem();
        this.destino = v.getDestino();
    }

    public String getOrigem() {
        return origem;
    }

    public String getDestino() {
        return destino;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrigemDestino that = (OrigemDestino) o;
        return origem.equals(that.origem) && destino.equals(that.destino);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origem, destino);
    }
}
