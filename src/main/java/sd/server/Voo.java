package sd.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Voo {
   public static int lastId= -1;
   private final int id;
   private long capacidade;
   private final LocalDate data;
   private final VooTabelado vooTabelado;
   private ReentrantLock capacityLock;

    public Voo(LocalDate data, VooTabelado vooTabelado) {
        this.id = ++lastId;
        this.capacidade = vooTabelado.getCapacidade();
        this.data = data;
        this.vooTabelado = vooTabelado;
        this.capacityLock = new ReentrantLock();
    }
    private Voo(int id, long capacidade , LocalDate data, VooTabelado vooTabelado) {
        this.id = id;
        this.capacidade = capacidade;
        this.data = data;
        this.vooTabelado = vooTabelado;
        this.capacityLock = new ReentrantLock();
    }

    public int getID() {
        return id;
    }

    public LocalDate getData() {
        return this.data;
    }

    public int getId() {
        return id;
    }

    public long getCapacidade() {
        try {
            capacityLock.lock();
            return capacidade;
        }
        finally {
            capacityLock.unlock();
        }
    }

    public String getOrigem() {
        return vooTabelado.getOrigem();
    }
    public String getDestino() {
        return vooTabelado.getDestino();
    }

    public VooTabelado getVooTabelado() {
        return vooTabelado;
    }

    public void diminuiCapacidade() {
        try {
            capacityLock.lock();
            capacidade--;
        }
        finally {
            capacityLock.unlock();
        }
    }

    public void serialize(DataOutputStream out) throws IOException {
        out.writeInt(id);
        out.writeLong(capacidade);
        out.writeUTF(data.toString());
        vooTabelado.serialize(out);
    }
    public static Voo deserialize(DataInputStream in) throws IOException {
        int id = in.readInt();
        long capacidade = in.readLong();
        LocalDate data = LocalDate.parse(in.readUTF());
        var voo = VooTabelado.deserialize(in);
        return new Voo(id,capacidade,data,voo);
    }

    public Voo clone(){
        return new Voo(this.id,this.capacidade,this.data,this.vooTabelado);
    }

    public void unlock(){
        capacityLock.unlock();
    }
    public void lock() {
        capacityLock.lock();
    }


}
