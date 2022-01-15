package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class TodosPercursosPacket extends ClientPacket {
    private String origem;
    private String destino;

    public TodosPercursosPacket(String origem, String destino) {
        super();
        this.origem = origem;
        this.destino = destino;
    }

    public static TodosPercursosPacket from(DataInputStream in) throws IOException {
        return new TodosPercursosPacket(in.readUTF(), in.readUTF());
    }

    @Override
    Operation getType() {
        return Operation.PercursosPossiveis;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeUTF(origem);
        out.writeUTF(destino);
    }

    public String getOrigem() {
        return origem;
    }

    public String getDestino() {
        return destino;
    }
}
