package sd.server;

import sd.packets.server.NotificacaoReply;
import sd.packets.server.ServerReply;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class NotificationPusher implements Runnable{
    private final ServerUser serverUser;
    private final Worker worker;

    public NotificationPusher(ServerUser serverUser, Worker worker) {
        this.serverUser = serverUser;
        this.worker = worker;
    }

    public void run() {
        while (true) {
            String notification = serverUser.takeNotification();
            if (notification == null)
                return;
            worker.lockOutput();
            try {
                DataOutputStream out = worker.getOutput();
                NotificacaoReply reply = new NotificacaoReply(notification);
                reply.serialize(out);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                worker.unlockOutput();
            }
        }
    }
}
