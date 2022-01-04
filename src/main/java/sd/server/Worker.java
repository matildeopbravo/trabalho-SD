package sd.server;

import sd.Operation;
import sd.exceptions.NotAdminException;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
        try {
            while(!s.isClosed() && patience > 0) {
                try {
                    Operation op = Operation.values()[in.readInt()];
                    if(op.equals(Operation.Autenticar)) {
                        user = Operation.autenticaUser(in, out);
                    }
                    else {
                        if (!op.equals(Operation.Registar) && !isAuthenticated()) {
                            handleFailure();
                            continue;
                        }
                        callMethodIfPossible(op);
                    }
                }
                catch (ArrayIndexOutOfBoundsException | IOException e ) {
                    Reply.InvalidFormat.serialize(out);
                }
                catch(NotAdminException e) {
                    handleFailure();
                }
            }
        }
        finally {
        endConnection();
    }
    }

    private void handleFailure() {
        Reply.Failure.serialize(out);
        patience--;

    }

    private void callMethodIfPossible(Operation op) throws NotAdminException {
        if(!user.isAdmin() && op.isAdminOption()) {
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
