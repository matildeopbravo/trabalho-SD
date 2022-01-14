package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public enum Reply {
    Success,
    Failure,
    InvalidFormat,
    Codigo;

    public void serialize(DataOutputStream outputStream) {
        try {
            System.out.println("Serealized " + this);
            outputStream.writeInt(this.getValue());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Reply deserialize(DataInputStream inputStream) throws ArrayIndexOutOfBoundsException {
        try {
            Reply r = values()[getOrdinalFromFake(inputStream.readInt())];
            System.out.println("Deserealized " + r);
            return r;
        }
        catch(IOException e) {
            return Failure;

        }
    }
    public int getValue() {
        return -this.ordinal() - 1;
    }

    public static int getOrdinalFromFake(int fakeValue) {
        return -(fakeValue+1);
    }

}
