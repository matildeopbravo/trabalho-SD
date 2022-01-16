package sd.server;

import sd.Operation;
import sd.client.ui.ClientUI;
import sd.exceptions.NotAdminException;
import sd.packets.client.ClientPacket;
import sd.packets.server.ServerReply;
import sd.packets.server.StatusReply;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;

public class Worker implements Runnable {
    private int patience ;
    private ServerUser user;
    private final Socket s ;
    private DataInputStream in ;
    private DataOutputStream out ;
    private final ReentrantLock outputLock;
    private Thread notificationPusher;

    public Worker(Socket s) {
        this.s = s;
        user = null;
        this.patience = 5;
        try {
            this.in = new DataInputStream(s.getInputStream());
            this.out = new DataOutputStream(s.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.outputLock = new ReentrantLock();
        notificationPusher = null;
    }

    public void run() {
        try {
            while(!s.isClosed() && patience > 0) {
                ClientPacket clientPacket;
                try {
                    clientPacket = ClientPacket.deserialize(in);
                }
                catch (IOException e) {
                    return;
                }
                lockOutput();
                try {
                    Operation op = clientPacket.getType();
                    System.out.println("Got operation packet: " + op);

                    System.out.println("O user pretende realizer operação: " + op);
                    if (op.equals(Operation.Login)) {
                        user = Operation.autenticaUser(clientPacket, out);
                        if (user == null) System.out.println("Não é Possível Fazer Login");
                        else {
                            stopNotificationPusher();
                            startNotificationPusher();
                            System.out.println("User autenticado é " + user);
                        }
                    } else {
                        if (!op.equals(Operation.Registar) && !isAuthenticated()) {
                            System.out.println("Pedido de user não autenticado");
                            //in.skip(in.available());
                            handleFailure(clientPacket);
                            continue;
                        } else if (op.equals(Operation.LogOut)) {
                            stopNotificationPusher();
                        }
                        callMethodIfPossible(clientPacket);
                    }
                }
                catch(NotAdminException e) {
                    System.out.println("Não tem permissão para realizar essa operação");
                    handleFailure(clientPacket);
                }
                finally {
                    unlockOutput();
                }
            }
        }
        finally {
            System.out.println("Client "   + ClientUI.ANSI_RED + s.getInetAddress().getHostAddress()
                    + ClientUI.ANSI_RESET + " disconnected");
            endConnection();
        }
    }

    private void handleFailure(ClientPacket clientPacket) {
        StatusReply reply = new StatusReply(clientPacket.getId(), ServerReply.Status.Failure);
        try {
            reply.serialize(out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        patience--;
    }

    private void callMethodIfPossible(ClientPacket clientPacket) throws NotAdminException {
        Operation op = clientPacket.getType();
        op.callHandleMethod(user,clientPacket,out);
        System.out.println("Called Method");
        if(user != null && !user.isAdmin() && op.isAdminOption()) {
            throw new NotAdminException();
        }
    }

    private boolean isAuthenticated() {
        return user != null;
    }
    private void endConnection() {
        try {
            s.shutdownOutput();
            s.shutdownInput();
            s.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public DataOutputStream getOutput() {
        return out;
    }

    public void lockOutput() {
        outputLock.lock();
    }

    public void unlockOutput() {
        outputLock.unlock();
    }

    public void startNotificationPusher() {
        if (notificationPusher == null) {
            NotificationPusher pusher = new NotificationPusher(this.user, this);
            this.notificationPusher = new Thread(pusher);
            this.notificationPusher.start();
        }
    }

    public void stopNotificationPusher() {
        if (this.notificationPusher != null) {
            this.notificationPusher.interrupt();
            this.notificationPusher = null;
        }
    }
}
