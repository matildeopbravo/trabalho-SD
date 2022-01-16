package sd.server;

import sd.Operation;
import sd.client.ui.ClientUI;
import sd.exceptions.NotAdminException;
import sd.packets.client.ClientPacket;

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
                try {
                    ClientPacket clientPacket = ClientPacket.deserialize(in);
                    lockOutput();
                    Operation op = clientPacket.getType();
                    System.out.println("Got operation packet: " + op);

                    System.out.println("O user pretende realizer operação: " + op);
                    if(op.equals(Operation.Login)) {
                        user = Operation.autenticaUser(clientPacket, out);
                        if(user == null ) System.out.println("credenciais invalidas");
                        else {
                            stopNotificationPusher();
                            startNotificationPusher();
                            System.out.println("User autenticado é " + user);
                        }
                    }
                    else {
                        if (!op.equals(Operation.Registar) && !isAuthenticated()) {
                            System.out.println("Pedido de user não autenticado");
                            //in.skip(in.available());
                            handleFailure();
                            continue;
                        }
                        else if (op.equals(Operation.LogOut)) {
                            stopNotificationPusher();
                        }
                        callMethodIfPossible(clientPacket);
                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    Reply.InvalidFormat.serialize(out);
                }
                catch(IOException e){
                    return;
                }
                catch(NotAdminException e) {
                    System.out.println("Não tem permissão para realizar essa operação");
                    handleFailure();
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

    private void handleFailure() {
        Reply.Failure.serialize(out);
        patience--;
    }

    private void callMethodIfPossible(ClientPacket clientPacket) throws NotAdminException {
        Operation op = clientPacket.getType();
        op.callHandleMethod(user,clientPacket,out);
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
