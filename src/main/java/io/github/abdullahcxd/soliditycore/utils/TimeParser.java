package io.github.abdullahcxd.soliditycore.utils;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Time parsing utility similar to Node.js 'ms' package.
 * Converts between human-readable time strings and milliseconds.
 * 
 * <p>Examples:
 * <pre>
 * TimeParser.parse("2d")      -> 172800000
 * TimeParser.parse("1h 30m")  -> 5400000
 * TimeParser.parse("5s")      -> 5000
 * TimeParser.format(60000)    -> "1m"
 * TimeParser.format(60000, true) -> "1 minute"
 * </pre>
 */
@UtilityClass
public class TimeParser {

    private static final Pattern PATTERN = Pattern.compile(
        "(\\d+(?:\\.\\d+)?)\\s*([a-zA-Z]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Map<String, Long> UNIT_MAP = new LinkedHashMap<>();
    private static final Map<Long, String> REVERSE_MAP = new LinkedHashMap<>();
    private static final Map<Long, String> LONG_FORMAT_MAP = new LinkedHashMap<>();

    static {
        // Initialize unit mappings (order matters for formatting)
        UNIT_MAP.put("ms", 1L);
        UNIT_MAP.put("millisecond", 1L);
        UNIT_MAP.put("milliseconds", 1L);
        
        UNIT_MAP.put("s", TimeUnit.SECONDS.toMillis(1));
        UNIT_MAP.put("sec", TimeUnit.SECONDS.toMillis(1));
        UNIT_MAP.put("secs", TimeUnit.SECONDS.toMillis(1));
        UNIT_MAP.put("second", TimeUnit.SECONDS.toMillis(1));
        UNIT_MAP.put("seconds", TimeUnit.SECONDS.toMillis(1));
        
        UNIT_MAP.put("m", TimeUnit.MINUTES.toMillis(1));
        UNIT_MAP.put("min", TimeUnit.MINUTES.toMillis(1));
        UNIT_MAP.put("mins", TimeUnit.MINUTES.toMillis(1));
        UNIT_MAP.put("minute", TimeUnit.MINUTES.toMillis(1));
        UNIT_MAP.put("minutes", TimeUnit.MINUTES.toMillis(1));
        
        UNIT_MAP.put("h", TimeUnit.HOURS.toMillis(1));
        UNIT_MAP.put("hr", TimeUnit.HOURS.toMillis(1));
        UNIT_MAP.put("hrs", TimeUnit.HOURS.toMillis(1));
        UNIT_MAP.put("hour", TimeUnit.HOURS.toMillis(1));
        UNIT_MAP.put("hours", TimeUnit.HOURS.toMillis(1));
        
        UNIT_MAP.put("d", TimeUnit.DAYS.toMillis(1));
        UNIT_MAP.put("day", TimeUnit.DAYS.toMillis(1));
        UNIT_MAP.put("days", TimeUnit.DAYS.toMillis(1));
        
        UNIT_MAP.put("w", TimeUnit.DAYS.toMillis(7));
        UNIT_MAP.put("wk", TimeUnit.DAYS.toMillis(7));
        UNIT_MAP.put("week", TimeUnit.DAYS.toMillis(7));
        UNIT_MAP.put("weeks", TimeUnit.DAYS.toMillis(7));
        
        UNIT_MAP.put("mo", TimeUnit.DAYS.toMillis(30));
        UNIT_MAP.put("month", TimeUnit.DAYS.toMillis(30));
        UNIT_MAP.put("months", TimeUnit.DAYS.toMillis(30));
        
        UNIT_MAP.put("y", TimeUnit.DAYS.toMillis(365));
        UNIT_MAP.put("yr", TimeUnit.DAYS.toMillis(365));
        UNIT_MAP.put("year", TimeUnit.DAYS.toMillis(365));
        UNIT_MAP.put("years", TimeUnit.DAYS.toMillis(365));

        // Reverse mappings for formatting (short format)
        REVERSE_MAP.put(TimeUnit.DAYS.toMillis(365), "y");
        REVERSE_MAP.put(TimeUnit.DAYS.toMillis(30), "mo");
        REVERSE_MAP.put(TimeUnit.DAYS.toMillis(7), "w");
        REVERSE_MAP.put(TimeUnit.DAYS.toMillis(1), "d");
        REVERSE_MAP.put(TimeUnit.HOURS.toMillis(1), "h");
        REVERSE_MAP.put(TimeUnit.MINUTES.toMillis(1), "m");
        REVERSE_MAP.put(TimeUnit.SECONDS.toMillis(1), "s");
        REVERSE_MAP.put(1L, "ms");

        // Long format mappings
        LONG_FORMAT_MAP.put(TimeUnit.DAYS.toMillis(365), "year");
        LONG_FORMAT_MAP.put(TimeUnit.DAYS.toMillis(30), "month");
        LONG_FORMAT_MAP.put(TimeUnit.DAYS.toMillis(7), "week");
        LONG_FORMAT_MAP.put(TimeUnit.DAYS.toMillis(1), "day");
        LONG_FORMAT_MAP.put(TimeUnit.HOURS.toMillis(1), "hour");
        LONG_FORMAT_MAP.put(TimeUnit.MINUTES.toMillis(1), "minute");
        LONG_FORMAT_MAP.put(TimeUnit.SECONDS.toMillis(1), "second");
        LONG_FORMAT_MAP.put(1L, "millisecond");
    }

    /**
     * Parses a time string to milliseconds.
     * 
     * @param input Time string (e.g., "2d", "1h 30m", "5s")
     * @return Milliseconds, or null if parsing fails
     */
    public static @Nullable Long parse(@NotNull String input) {
        if (input.trim().isEmpty()) {
            return null;
        }

        // Try parsing as a number first
        try {
            return Long.parseLong(input.trim());
        } catch (NumberFormatException ignored) {
            // Continue with pattern matching
        }

        input = input.toLowerCase().trim();
        Matcher matcher = PATTERN.matcher(input);
        
        long total = 0;
        boolean foundMatch = false;

        while (matcher.find()) {
            foundMatch = true;
            double value = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            Long unitValue = UNIT_MAP.get(unit);
            if (unitValue == null) {
                return null; // Unknown unit
            }

            total += (long) (value * unitValue);
        }

        return foundMatch ? total : null;
    }

    /**
     * Parses a time string to milliseconds, throwing an exception on failure.
     * 
     * @param input Time string
     * @return Milliseconds
     * @throws IllegalArgumentException if parsing fails
     */
    public static long parseOrThrow(@NotNull String input) {
        Long result = parse(input);
        if (result == null) {
            throw new IllegalArgumentException("Invalid time format: " + input);
        }
        return result;
    }

    /**
     * Parses a time string with a fallback value.
     * 
     * @param input Time string
     * @param fallback Default value if parsing fails
     * @return Milliseconds or fallback
     */
    public static long parseOrDefault(@NotNull String input, long fallback) {
        Long result = parse(input);
        return result != null ? result : fallback;
    }

    /**
     * Formats milliseconds to a human-readable string (short format).
     * 
     * @param milliseconds Time in milliseconds
     * @return Formatted string (e.g., "2d", "1h 30m")
     */
    public static @NotNull String format(long milliseconds) {
        return format(milliseconds, false, false);
    }

    /**
     * Formats milliseconds to a human-readable string.
     * 
     * @param milliseconds Time in milliseconds
     * @param longFormat Use long format (e.g., "2 days" instead of "2d")
     * @return Formatted string
     */
    public static @NotNull String format(long milliseconds, boolean longFormat) {
        return format(milliseconds, longFormat, false);
    }

    /**
     * Formats milliseconds to a human-readable string.
     * 
     * @param milliseconds Time in milliseconds
     * @param longFormat Use long format (e.g., "2 days" instead of "2d")
     * @param verbose Show all units (e.g., "2d 3h 5m" instead of "2d")
     * @return Formatted string
     */
    public static @NotNull String format(long milliseconds, boolean longFormat, boolean verbose) {
        if (milliseconds < 0) {
            return "-" + format(-milliseconds, longFormat, verbose);
        }

        if (milliseconds == 0) {
            return longFormat ? "0 milliseconds" : "0ms";
        }

        StringBuilder result = new StringBuilder();
        long remaining = milliseconds;

        Map<Long, String> formatMap = longFormat ? LONG_FORMAT_MAP : REVERSE_MAP;

        for (Map.Entry<Long, String> entry : formatMap.entrySet()) {
            long unitValue = entry.getKey();
            String unitName = entry.getValue();

            if (remaining >= unitValue) {
                long count = remaining / unitValue;
                remaining %= unitValue;

                if (!result.isEmpty()) {
                    result.append(" ");
                }

                result.append(count);
                
                if (longFormat) {
                    result.append(" ");
                    result.append(unitName);
                    if (count > 1) {
                        result.append("s");
                    }
                } else {
                    result.append(unitName);
                }

                if (!verbose) {
                    break; // Only show the largest unit
                }
            }
        }

        return result.toString();
    }

    /**
     * Formats milliseconds showing all significant units.
     * 
     * @param milliseconds Time in milliseconds
     * @return Formatted string (e.g., "2d 3h 5m 30s")
     */
    public static @NotNull String formatVerbose(long milliseconds) {
        return format(milliseconds, false, true);
    }

    /**
     * Formats milliseconds in long format showing all units.
     * 
     * @param milliseconds Time in milliseconds
     * @return Formatted string (e.g., "2 days 3 hours 5 minutes")
     */
    public static @NotNull String formatLongVerbose(long milliseconds) {
        return format(milliseconds, true, true);
    }

    /**
     * Checks if a string is a valid time format.
     * 
     * @param input Time string to validate
     * @return true if valid
     */
    public static boolean isValid(@NotNull String input) {
        return parse(input) != null;
    }

    /**
     * Converts milliseconds to seconds.
     */
    public static long toSeconds(long milliseconds) {
        return TimeUnit.MILLISECONDS.toSeconds(milliseconds);
    }

    /**
     * Converts milliseconds to minutes.
     */
    public static long toMinutes(long milliseconds) {
        return TimeUnit.MILLISECONDS.toMinutes(milliseconds);
    }

    /**
     * Converts milliseconds to hours.
     */
    public static long toHours(long milliseconds) {
        return TimeUnit.MILLISECONDS.toHours(milliseconds);
    }

    /**
     * Converts milliseconds to days.
     */
    public static long toDays(long milliseconds) {
        return TimeUnit.MILLISECONDS.toDays(milliseconds);
    }

    /**
     * Builder for creating time durations programmatically.
     */
    public static class Builder {
        private long total = 0;

        public Builder years(long years) {
            total += years * TimeUnit.DAYS.toMillis(365);
            return this;
        }

        public Builder months(long months) {
            total += months * TimeUnit.DAYS.toMillis(30);
            return this;
        }

        public Builder weeks(long weeks) {
            total += weeks * TimeUnit.DAYS.toMillis(7);
            return this;
        }

        public Builder days(long days) {
            total += TimeUnit.DAYS.toMillis(days);
            return this;
        }

        public Builder hours(long hours) {
            total += TimeUnit.HOURS.toMillis(hours);
            return this;
        }

        public Builder minutes(long minutes) {
            total += TimeUnit.MINUTES.toMillis(minutes);
            return this;
        }

        public Builder seconds(long seconds) {
            total += TimeUnit.SECONDS.toMillis(seconds);
            return this;
        }

        public Builder milliseconds(long milliseconds) {
            total += milliseconds;
            return this;
        }

        public long build() {
            return total;
        }

        public String buildAndFormat() {
            return format(total);
        }

        public String buildAndFormatVerbose() {
            return formatVerbose(total);
        }
    }

    /**
     * Creates a new duration builder.
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull Builder builder() {
        return new Builder();
    }

    @Contract(pure = true)
    public static @NotNull String formatMillis(long millis) {
        long seconds = millis / 1000 % 60;
        long minutes = millis / (1000 * 60) % 60;
        long hours = millis / (1000 * 60 * 60);

        return String.format("%02dh %02dm %02ds", hours, minutes, seconds);
    }
}