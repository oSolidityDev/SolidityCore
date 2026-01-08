package io.github.abdullahcxd.soliditycore.utils;

import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import org.jetbrains.annotations.Nullable;

public class Validator {

    public static <T> void notNull(@Nullable T element, String errorMessage) {
        if (element == null)
            throw new SolidityException(errorMessage);
    }

}
