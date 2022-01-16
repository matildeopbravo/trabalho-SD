package sd.client;

import sd.client.ui.ClientUI;
import sd.packets.server.NotificacaoReply;
import sd.packets.server.ServerReply;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.*;

public class Demultiplexer implements Runnable {
    private final Map<Integer, ServerReply> waitingReplies;
    private final Lock queueLock;
    private final Condition cond;
    private final DataInputStream inputStream;
    private final Deque<NotificacaoReply> notifications;
    private final ReentrantLock notificationsLock;

    public Demultiplexer(DataInputStream inputStream) {
        this.inputStream = inputStream;
        this.waitingReplies = new HashMap<>();
        this.queueLock = new ReentrantLock();
        this.cond = queueLock.newCondition();
        this.notifications = new ArrayDeque<>();
        this.notificationsLock = new ReentrantLock();
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

    public List<NotificacaoReply> getNotificacoes() {
        notificationsLock.lock();
        try {
            List<NotificacaoReply> ret = new ArrayList<>();
            while (!notifications.isEmpty()) {
                ret.add(notifications.pop());
            }
            return ret;
        } finally {
            notificationsLock.unlock();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                ServerReply r = ServerReply.deserialize(inputStream);
                if (r.getType() == ServerReply.ServerPacketType.Notificacao) {
                    this.notificationsLock.lock();
                    try {
                        this.notifications.add((NotificacaoReply) r);
                    } finally {
                        this.notificationsLock.unlock();
                    }
                } else {
                    try {
                        queueLock.lock();
                        waitingReplies.put(r.getId(), r);

                        // TODO: Arranjar forma de não dar signal a toda a gente
                        // Se bem que não é grande problema, porque isto não _deve_ passar de 1
                        cond.signalAll();
                    } finally {
                        queueLock.unlock();
                    }
                }
            }
            catch (EOFException e) {
                System.err.println(ClientUI.ANSI_RED + "\nServidor Desconectado" + ClientUI.ANSI_RESET);
                break;
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
        }
    }
}
