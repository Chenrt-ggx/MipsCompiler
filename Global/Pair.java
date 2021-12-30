package Global;

import java.util.Objects;
import java.io.Serializable;

public class Pair<K, V> implements Serializable {
    private final K key;
    private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return key.hashCode() * 13 + (value == null ? 0 : value.hashCode());
    }

    @Override
    public String toString() {
        return key + "=" + value;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Pair) {
            @SuppressWarnings("unchecked")
            Pair<K, V> pair = (Pair<K, V>)obj;
            return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
        }
        return false;
    }
}