package sd.server;

import sd.Connection;
import sd.client.ClientUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerUser {
    private final ClientUser user;
    private boolean isAuthenticated;
    private final boolean isAdmin ;
    private final Lock authenticationLock;

    // se for criado pelo cliente nao pode ser admin
    public ServerUser(ClientUser user) {
        this.user = user;
        this.isAdmin = false;
        this.isAuthenticated  = false;
        this.authenticationLock = new ReentrantLock();
    }

    public ServerUser(String username, String password,  boolean isAdmin) {
        this.user = new ClientUser(username, password);
        this.isAdmin = isAdmin;
        this.authenticationLock = new ReentrantLock();
    }

    public ClientUser getClientUser(){return user;}

    public String getUserName() {
        return user.getUserName();
    }

    public String getPassword() {
        return user.getPassword();
    }

    public void  setIsAuthenticated(boolean b) {
        try {
            authenticationLock.lock();
            this.isAuthenticated = b;
        }
        finally {
            authenticationLock.unlock();
        }
    }

    public boolean isAdmin(){
        return isAdmin;
    }

    public boolean isAuthenticated() {
        try {
            authenticationLock.lock();
            return isAuthenticated ;
        }
        finally {
            authenticationLock.unlock();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerUser that = (ServerUser) o;
        return isAuthenticated == that.isAuthenticated && isAdmin == that.isAdmin && Objects.equals(user, that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, isAuthenticated, isAdmin);
    }
}
