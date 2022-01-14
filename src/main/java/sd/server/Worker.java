package sd.server;

import sd.Operation;
import sd.client.ui.ClientUI;
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
        int i = 0;
        try {
            while(!s.isClosed() && patience > 0) {
                try {
                    int n = in.readInt();
                    System.out.println("read :" + n);
                    Operation op = Operation.getFromFakeOrdinal(n);
                    System.out.println(op);

                    System.out.println("O user pretende realizer operação: " + op);
                    if(op.equals(Operation.Login)) {
                        user = Operation.autenticaUser(in, out);
                        if(user == null ) System.out.println("credenciais invalidas");
                        else {
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
                        callMethodIfPossible(op);
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

    private void callMethodIfPossible(Operation op) throws NotAdminException {
        op.callHandleMethod(user,in,out);
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
}
