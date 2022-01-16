package sd.server;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DashMap<K,V>  {
    private ReadWriteLock lock;
    private Map<K,V> map;

    public DashMap(){
        this.lock = new ReentrantReadWriteLock();
        this.map = new HashMap<>();
    }

    public int size() {
        try {
            lock.readLock().lock();
            return map.size();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        try {
            lock.readLock().lock();
            return map.isEmpty();
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public boolean containsKey(K key) {
        try {
            lock.readLock().lock();
            return map.containsKey(key);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public boolean containsValue(V value) {
        try {
            lock.readLock().lock();
            return map.containsValue(value);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public V get(K key) {
        try {
            lock.readLock().lock();
            return map.get(key);
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public V put(K key, V value) {
        try {
            lock.writeLock().lock();
            return map.put(key,value);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public V remove(K key) {
        try {
            lock.writeLock().lock();
            return map.remove(key);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void putAll(Map<? extends K, ? extends V> m) {
        try {
            lock.writeLock().lock();
            map.putAll(m);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public V computeIfAbsent(K k, Function<? super K, ? extends V> fun) {
        return map.computeIfAbsent(k,fun);
    }

    //public Set<K> keySet() {
    //    try {
    //        lock.readLock().lock();
    //        return map.keySet();
    //    }
    //    finally {
    //        lock.readLock().unlock();
    //    }
    //}

    public Collection<V> values(Function<? super V, V> cloneFun) {
        try {
            lock.readLock().lock();
            return map.values().stream().map(cloneFun).collect(Collectors.toSet());
        }
        finally {
            lock.readLock().unlock();
        }
    }

    public V putIfAbsent(K k, V v) {
        try {
            lock.writeLock().lock();
            return map.putIfAbsent(k, v);
        }
        finally {
            lock.writeLock().unlock();
        }
    }

    public void lock() {
        lock.writeLock().lock();
    }

    public void unlock() {
        lock.writeLock().unlock();
    }

    //public Set<Map.Entry<K, V>> entrySet() {
    //    try {
    //        lock.readLock().lock();
    //        return map.entrySet();
    //    }
    //    finally {
    //        lock.readLock().unlock();
    //    }
    //}
}
