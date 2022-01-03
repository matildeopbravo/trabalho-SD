package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.time.LocalDate;

public class Voo {
   private String id;
   private String origem;
   private String destino;
   private long capacidade;
   private LocalDate data;

    public static Voo deserialize(DataInputStream in) {
        //  TODO
        return new Voo();
    }

    public void serialize(DataOutputStream out) {
        // TODO
    }
}
