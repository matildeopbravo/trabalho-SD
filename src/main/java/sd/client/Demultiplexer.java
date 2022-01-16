package sd.client;

import sd.client.ui.ClientUI;
import sd.packets.server.ServerReply;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Demultiplexer implements Runnable {
    private final Map<Integer, ServerReply> waitingReplies;
    private final Lock queueLock;
    private final Condition cond;
    private final DataInputStream inputStream;

    public Demultiplexer(DataInputStream inputStream) {
        this.inputStream = inputStream;
        this.waitingReplies = new HashMap<>();
        this.queueLock = new ReentrantLock();
        this.cond = queueLock.newCondition();
    }

    public ServerReply awaitReplyTo(int id) {
        queueLock.lock();
        try {
            while (!waitingReplies.containsKey(id)) {
                try {
                    cond.await();
                } catch (InterruptedException ignored) {
                }
            }
            ServerReply reply = waitingReplies.get(id);
            waitingReplies.remove(id);
            return reply;
        } finally {
            queueLock.unlock();
        }
    }

    public void start() {
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                ServerReply r = ServerReply.deserialize(inputStream);
                queueLock.lock();
                try {
                    waitingReplies.put(r.getId(), r);
                    // TODO: Arranjar forma de não dar signal a toda a gente
                    // Se bem que não é grande problema, porque isto não _deve_ passar de 1
                    cond.signalAll();
                } finally {
                    queueLock.unlock();
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
