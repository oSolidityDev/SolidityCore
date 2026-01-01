package io.github.abdullahcxd.soliditycore.utils;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class TimeUtil {

    @Contract(pure = true)
    public static @NotNull String formatMillis(long millis) {
        long seconds = millis / 1000 % 60;
        long minutes = millis / (1000 * 60) % 60;
        long hours = millis / (1000 * 60 * 60);

        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}
