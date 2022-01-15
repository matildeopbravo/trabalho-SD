package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservaPacket extends ClientPacket {
    private final List<String> cidades;
    private final LocalDate dataInicial;
    private final LocalDate dataFinal;

    public ReservaPacket(List<String> cidades, LocalDate dataInicial, LocalDate dataFinal) {
        super();
        this.cidades = cidades;
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
    }
    
    public static ReservaPacket from(DataInputStream in) throws IOException {
        int cidadesSize = in.readInt();
        List<String> cidades = new ArrayList<>(cidadesSize);
        for (int i = 0; i < cidadesSize; i++) {
            cidades.add(in.readUTF());
        }
        LocalDate dataInicial = LocalDate.parse(in.readUTF());
        LocalDate dataFinal = LocalDate.parse(in.readUTF());

        return new ReservaPacket(cidades, dataInicial, dataFinal);
    }

    @Override
    Operation getType() {
        return Operation.Reserva;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeInt(cidades.size());
        for (String c : cidades) {
            out.writeUTF(c);
        }
        out.writeUTF(dataInicial.toString());
        out.writeUTF(dataFinal.toString());
    }
}
