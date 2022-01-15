package sd.packets.client;

import sd.Operation;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class LogOutPacket extends ClientPacket {
    public static LogOutPacket from(DataInputStream in) {
        return new LogOutPacket();
    }

    public LogOutPacket() {
        super();
    }

    @Override
    Operation getType() {
        return Operation.LogOut;
    }

    @Override
    protected void writeTo(DataOutputStream out) {
        // A packet é vazia
    }
}
