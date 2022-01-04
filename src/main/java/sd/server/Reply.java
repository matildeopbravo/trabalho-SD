package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum Reply {
    Success,
    Failure,
    InvalidFormat,
    Codigo
    ;

    public void serialize(DataOutputStream outputStream) {
        try {
            outputStream.writeInt(this.ordinal());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Reply deserialize(DataInputStream inputStream) throws IOException {
        return values()[inputStream.readInt()];
    }
}
