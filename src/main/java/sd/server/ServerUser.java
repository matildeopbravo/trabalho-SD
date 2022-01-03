package sd.server;

import sd.Connection;
import sd.client.ClientUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ServerUser {
    private final ClientUser user;
    private Connection connection;
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

    public static ServerUser deserialize() {
        //TODO
        return null;
    }

    public void createConnection(DataInputStream in, DataOutputStream out) {
        this.connection = new Connection(in,out);
    }

    public String getUserName() {
        return user.getUserName();
    }

    public String getPassword() {
        return user.getPassword();
    }

    public void serialize(DataOutputStream out) {
        // TODO
    }

    public void setIsAuthenticated(boolean b) {
        // TODO
    }

    public void setStreams(DataInputStream in, DataOutputStream out) {
        // TODO
    }

    public boolean isAdmin(){
        return isAdmin;
    }
}
