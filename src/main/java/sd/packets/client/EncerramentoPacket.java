package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;

public class EncerramentoPacket extends ClientPacket {
    private final LocalDate date;

    public EncerramentoPacket(LocalDate date) {
        super();
        this.date = date;
    }

    public static EncerramentoPacket from(DataInputStream in) throws IOException {
        LocalDate date = LocalDate.parse(in.readUTF());
        return new EncerramentoPacket(date);
    }

    @Override
    public Operation getType() {
        return Operation.Encerramento;
    }

    @Override
    protected void writeTo(DataOutputStream out) throws IOException {
        out.writeUTF(date.toString());
    }
}
