package io.github.abdullahcxd.soliditycore.storage;

public interface Storage<T> {
    Storage<T> set(String key, T value);
    T get(String key);
    T get(String key, T defaultValue);
    boolean has(String key);
    T remove(String key);
    void clear();
    int size();
}