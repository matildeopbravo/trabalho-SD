package sd.server;

import sd.Connection;
import sd.client.ClientUser;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ServerUser {
    private final ClientUser user;
    private boolean isAuthenticated;
    private final boolean isAdmin ;
    private final Lock authenticationLock;
    private final List<String> pendingNotifications;
    private final ReentrantLock notificationsLock;
    private final Condition notificationsCondition;

    // se for criado pelo cliente nao pode ser admin
    public ServerUser(ClientUser user) {
        this.user = user;
        this.isAdmin = false;
        this.isAuthenticated  = false;
        this.authenticationLock = new ReentrantLock();
        this.pendingNotifications = new ArrayList<>();
        this.notificationsLock = new ReentrantLock();
        this.notificationsCondition = notificationsLock.newCondition();
    }

    public ServerUser(String username, String password,  boolean isAdmin) {
        this.user = new ClientUser(username, password);
        this.isAuthenticated = false;
        this.isAdmin = isAdmin;
        this.authenticationLock = new ReentrantLock();
        this.pendingNotifications = new ArrayList<>();
        this.notificationsLock = new ReentrantLock();
        this.notificationsCondition = notificationsLock.newCondition();
    }

    public ServerUser(ClientUser c, boolean isAuthenticated, boolean isAdmin) {
        this.user = c;
        this.isAuthenticated = isAuthenticated;
        this.isAdmin = isAdmin;
        this.authenticationLock = new ReentrantLock();
        this.pendingNotifications = new ArrayList<>();
        this.notificationsLock = new ReentrantLock();
        this.notificationsCondition = notificationsLock.newCondition();
    }

    public static ServerUser deserialize(DataInputStream in) throws IOException {
        return new ServerUser(ClientUser.deserialize(in), in.readBoolean(),  in.readBoolean());
    }

    public void serialize(DataOutputStream out) throws IOException {
        user.serialize(out);
        out.writeBoolean(isAuthenticated);
        out.writeBoolean(isAdmin);
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

    public void addNotification(String notification) {
        notificationsLock.lock();
        try {
            pendingNotifications.add(notification);
            notificationsCondition.signalAll();
        }
        finally {
            notificationsLock.unlock();
        }
    }

    public String takeNotification() {
        String notification;
        notificationsLock.lock();
        try {
            while (pendingNotifications.isEmpty()) {
                try {
                    notificationsCondition.await();
                } catch (InterruptedException ignored) {
                    return null;
                }
            }
            notification = pendingNotifications.remove(0);
        }
        finally {
            notificationsLock.unlock();
        }
        return notification;
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
