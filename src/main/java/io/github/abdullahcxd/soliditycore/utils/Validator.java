package io.github.abdullahcxd.soliditycore.utils;

import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class Validator {

    public static <T> void notNull(@Nullable T element, String errorMessage) {
        if (element == null)
            throw new SolidityException(errorMessage);
    }

    public static <T> void isNull(@Nullable T element, String errorMessage) {
        if (element != null)
            throw new SolidityException(errorMessage);
    }

    public static void isTrue(boolean condition, String errorMessage) {
        if (!condition)
            throw new SolidityException(errorMessage);
    }

    public static void isFalse(boolean condition, String errorMessage) {
        if (condition)
            throw new SolidityException(errorMessage);
    }

    public static void notEmpty(@Nullable String value, String errorMessage) {
        if (value == null || value.isEmpty())
            throw new SolidityException(errorMessage);
    }

    public static void notBlank(@Nullable String value, String errorMessage) {
        if (value == null || value.trim().isEmpty())
            throw new SolidityException(errorMessage);
    }

    public static void minLength(@Nullable String value, int min, String errorMessage) {
        notNull(value, errorMessage);
        if (value.length() < min)
            throw new SolidityException(errorMessage);
    }

    public static void maxLength(@Nullable String value, int max, String errorMessage) {
        notNull(value, errorMessage);
        if (value.length() > max)
            throw new SolidityException(errorMessage);
    }

    public static void notEmpty(@Nullable Collection<?> collection, String errorMessage) {
        if (collection == null || collection.isEmpty())
            throw new SolidityException(errorMessage);
    }

    public static void notEmpty(@Nullable Map<?, ?> map, String errorMessage) {
        if (map == null || map.isEmpty())
            throw new SolidityException(errorMessage);
    }

    public static void positive(int value, String errorMessage) {
        if (value <= 0)
            throw new SolidityException(errorMessage);
    }

    public static void positive(long value, String errorMessage) {
        if (value <= 0)
            throw new SolidityException(errorMessage);
    }

    public static void notNegative(int value, String errorMessage) {
        if (value < 0)
            throw new SolidityException(errorMessage);
    }

    public static void notNegative(long value, String errorMessage) {
        if (value < 0)
            throw new SolidityException(errorMessage);
    }

    public static void inRange(int value, int min, int max, String errorMessage) {
        if (value < min || value > max)
            throw new SolidityException(errorMessage);
    }

    public static void instanceOf(Object obj, @NotNull Class<?> type, String errorMessage) {
        notNull(obj, errorMessage);
        if (!type.isInstance(obj))
            throw new SolidityException(errorMessage);
    }

    public static void notSame(Object a, Object b, String errorMessage) {
        if (a == b)
            throw new SolidityException(errorMessage);
    }

    public static void fail(String errorMessage) {
        throw new SolidityException(errorMessage);
    }

}
