package sd;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class IDGenerator {
    private int lastID = -1;
    private final Lock lock = new ReentrantLock();

    public int nextID() {
        try {
            lock.lock();
            return ++lastID;
        }
        finally {
            {
                lock.unlock();
            }
        }
    }
}
