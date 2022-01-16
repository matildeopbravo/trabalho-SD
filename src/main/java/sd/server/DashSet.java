package sd.server;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DashSet<E>  {
    private final ReadWriteLock lock;
    private final Set<E> set;

    public DashSet(){
        this.lock = new ReentrantReadWriteLock();
        this.set = new HashSet<>();
    }

    public int size() {
        try {
            lock.readLock().lock();
            return set.size();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        try {
            lock.readLock().lock();
            return set.isEmpty();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(E object) {
        try {
            lock.readLock().lock();
            return set.contains(object);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public boolean add(E object) {
        try {
            lock.writeLock().lock();
            return set.add(object);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public boolean remove(E object) {
        try {
            lock.writeLock().lock();
            return set.remove(object);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public boolean putAll(Set<? extends E> m) {
        try {
            lock.writeLock().lock();
            return set.addAll(m);
        }
        finally {
            lock.writeLock().unlock();
        }
    }
}
