package sd.server;

import sd.client.ClientUser;

import java.net.Socket;

public class ServerUser {
    private final ClientUser user;
    private boolean isAuthenticated = false;
    private Socket s;
    private final boolean isAdmin ;

    // se for criado pelo cliente nao pode ser admin
    public ServerUser(ClientUser user, Socket s ) {
        this.s = s;
        this.user = user;
        this.isAdmin = false;
    }

    public ServerUser(String username, String password,  boolean isAdmin) {
        this.user = new ClientUser(username, password);
        this.isAdmin = isAdmin;
    }

    public String getUserName() {
        return user.getUserName();
    }
}
