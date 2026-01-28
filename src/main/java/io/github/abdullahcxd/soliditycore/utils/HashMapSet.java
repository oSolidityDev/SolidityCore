package io.github.abdullahcxd.soliditycore.utils;

import lombok.SneakyThrows;

import java.util.HashMap;

public class HashMapSet<K, V> extends HashMap<K, V> {

    @SneakyThrows
    @Override
    public V put(K key, V value) {
        if (this.containsKey(key)) return null;
        return super.put(key, value);
    }
}
