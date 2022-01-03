package sd.server;

import sd.client.ClientUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ServerUser {
    private final ClientUser user;
    private boolean isAuthenticated = false;
    private DataOutputStream out;
    private DataInputStream in;
    private final boolean isAdmin ;

    // se for criado pelo cliente nao pode ser admin
    public ServerUser(ClientUser user) {
        this.user = user;
        this.isAdmin = false;
    }

    public ServerUser(String username, String password,  boolean isAdmin) {
        this.user = new ClientUser(username, password);
        this.isAdmin = isAdmin;
    }

    public void setStreams(DataInputStream in, DataOutputStream out) {
        this.in = in;
        this.out = out;
    }


    public String getUserName() {
        return user.getUserName();
    }

    public void setIsAuthenticated(boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
    }

    public String getPassword() {
        return user.getPassword();
    }
}
