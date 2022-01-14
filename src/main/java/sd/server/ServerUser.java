package sd.server;

import sd.Connection;
import sd.client.ClientUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ServerUser {
    private final ClientUser user;
    private boolean isAuthenticated;
    private final boolean isAdmin ;

    // se for criado pelo cliente nao pode ser admin
    public ServerUser(ClientUser user) {
        this.user = user;
        this.isAdmin = false;
        this.isAuthenticated  = false;
    }

    public ServerUser(String username, String password,  boolean isAdmin) {
        this.user = new ClientUser(username, password);
        this.isAdmin = isAdmin;
    }

    //public void serialize(DataOutputStream out) throws IOException {
    //    user.serialize(out);
    //    out.writeBoolean(isAuthenticated);
    //    out.writeBoolean(isAdmin);
    //}
    //public static ServerUser deserialize(DataInputStream in) throws IOException {
    //    ClientUser usr = ClientUser.deserialize(in);
    //    return new ServerUser(usr);
    //}

    public String getUserName() {
        return user.getUserName();
    }

    public String getPassword() {
        return user.getPassword();
    }

    public void  setIsAuthenticated(boolean b) {
        this.isAuthenticated = b;
    }

    public boolean isAdmin(){
        return isAdmin;
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }
}
