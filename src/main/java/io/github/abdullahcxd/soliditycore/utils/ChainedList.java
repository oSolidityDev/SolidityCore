package io.github.abdullahcxd.soliditycore.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;

public class ChainedList<T> extends ArrayList<T> {

    @Contract(value = " -> new", pure = true)
    public static <T> @NotNull ChainedList<T> startChain() {
        return new ChainedList<>();
    }

    @SafeVarargs
    public static <T> ChainedList<T> startChain(T... elements) {
        return new ChainedList<T>().addUnpacked(elements);
    }

    public ChainedList<T> chainAdd(T element) {
        this.add(element);
        return this;
    }

    public ChainedList<T> chainRemove(T element) {
        this.remove(element);
        return this;
    }

    public ChainedList<T> chainRemove(int index) {
        this.remove(index);
        return this;
    }

    public ChainedList<T> addUnpacked(T... unpacked) {
        this.addAll(Arrays.asList(unpacked));

        return this;
    }
}
