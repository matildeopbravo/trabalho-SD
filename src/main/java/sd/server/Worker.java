package sd.server;

import sd.Operation;
import sd.exceptions.NotAdminException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class Worker implements Runnable {
    private int patience ;
    private ServerUser user;
    private Socket s ;
    private DataInputStream in ;
    private DataOutputStream out ;

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
    }

    public void run() {
        int i = 0;
        try {
            while(!s.isClosed() && patience > 0) {
                try {
                    Operation op = Operation.values()[-(in.readInt()+1)];
                    System.out.println("O user pretende realizer operação: " + op);
                    if(op.equals(Operation.Autenticar)) {
                        user = Operation.autenticaUser(in, out);
                        System.out.println("User autenticado é " + user);
                    }
                    else {
                        if (!op.equals(Operation.Registar) && !isAuthenticated()) {
                            System.out.println("Pedido de user não autenticado");
                            handleFailure();
                            continue;
                        }
                        callMethodIfPossible(op);
                    }
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("out of bounds");
                    //Reply.InvalidFormat.serialize(out);
                }
                catch(IOException e){
                    return;
                }
                catch(NotAdminException e) {
                    System.out.println("Não tem permissão para realizar essa operação");
                    handleFailure();
                }
            }
        }
        finally {
            System.err.println("Client " + s.getInetAddress() + " disconnected");
            endConnection();
        }
    }

    private void handleFailure() {
        Reply.Failure.serialize(out);
        patience--;
    }

    private void callMethodIfPossible(Operation op) throws NotAdminException {
        if(user != null && !user.isAdmin() && op.isAdminOption()) {
            throw new NotAdminException();
        }
        op.callHandleMethod(in,out);
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
}
