package sd.server;

import java.io.DataOutputStream;
import java.io.IOException;

public enum Reply {
    Success,
    Failure,
    InvalidFormat,
    ;

    public void serialize(DataOutputStream outputStream) {
        try {
            outputStream.writeInt(this.ordinal());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
