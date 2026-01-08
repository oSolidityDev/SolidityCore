package io.github.abdullahcxd.soliditycore.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Functional interface for resolvable parameters
 */
@FunctionalInterface
public interface Resolvable<T> {
    T resolve();

    @Contract(pure = true)
    static <T> @NotNull Resolvable<T> of(T value) {
        return () -> value;
    }
}