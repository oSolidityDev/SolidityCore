package io.github.abdullahcxd.soliditycore.builders;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Collection;

/**
 * A builder for creating and sending titles/subtitles to players
 */
public class TitleBuilder {

    private Component title;
    private Component subtitle;
    private Duration fadeIn = Duration.ofMillis(500);
    private Duration stay = Duration.ofSeconds(3);
    private Duration fadeOut = Duration.ofMillis(500);

    private TitleBuilder() {
        this.title = Component.empty();
        this.subtitle = Component.empty();
    }

    /**
     * Creates a new TitleBuilder
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull TitleBuilder create() {
        return new TitleBuilder();
    }

    /**
     * Sets the title text using MiniMessage format
     */
    public TitleBuilder title(@NotNull String miniMessage) {
        this.title = MessageBuilder.fromMiniMessage(miniMessage).build();
        return this;
    }

    /**
     * Sets the title component directly
     */
    public TitleBuilder title(@NotNull Component component) {
        this.title = component;
        return this;
    }

    /**
     * Sets the title using a MessageBuilder
     */
    public TitleBuilder title(@NotNull MessageBuilder builder) {
        this.title = builder.build();
        return this;
    }

    /**
     * Sets the subtitle text using MiniMessage format
     */
    public TitleBuilder subtitle(@NotNull String miniMessage) {
        this.subtitle = MessageBuilder.fromMiniMessage(miniMessage).build();
        return this;
    }

    /**
     * Sets the subtitle component directly
     */
    public TitleBuilder subtitle(@NotNull Component component) {
        this.subtitle = component;
        return this;
    }

    /**
     * Sets the subtitle using a MessageBuilder
     */
    public TitleBuilder subtitle(@NotNull MessageBuilder builder) {
        this.subtitle = builder.build();
        return this;
    }

    /**
     * Sets the fade-in duration in milliseconds
     */
    public TitleBuilder fadeIn(long millis) {
        this.fadeIn = Duration.ofMillis(millis);
        return this;
    }

    /**
     * Sets the fade-in duration
     */
    public TitleBuilder fadeIn(@NotNull Duration duration) {
        this.fadeIn = duration;
        return this;
    }

    /**
     * Sets the stay duration in milliseconds
     */
    public TitleBuilder stay(long millis) {
        this.stay = Duration.ofMillis(millis);
        return this;
    }

    /**
     * Sets the stay duration
     */
    public TitleBuilder stay(@NotNull Duration duration) {
        this.stay = duration;
        return this;
    }

    /**
     * Sets the fade-out duration in milliseconds
     */
    public TitleBuilder fadeOut(long millis) {
        this.fadeOut = Duration.ofMillis(millis);
        return this;
    }

    /**
     * Sets the fade-out duration
     */
    public TitleBuilder fadeOut(@NotNull Duration duration) {
        this.fadeOut = duration;
        return this;
    }

    /**
     * Sets all timing values at once (in milliseconds)
     */
    public TitleBuilder times(long fadeIn, long stay, long fadeOut) {
        this.fadeIn = Duration.ofMillis(fadeIn);
        this.stay = Duration.ofMillis(stay);
        this.fadeOut = Duration.ofMillis(fadeOut);
        return this;
    }

    /**
     * Sets all timing values at once
     */
    public TitleBuilder times(@NotNull Duration fadeIn, @NotNull Duration stay, @NotNull Duration fadeOut) {
        this.fadeIn = fadeIn;
        this.stay = stay;
        this.fadeOut = fadeOut;
        return this;
    }

    /**
     * Builds the Title object
     */
    public Title build() {
        return Title.title(
            title,
            subtitle,
            Title.Times.times(fadeIn, stay, fadeOut)
        );
    }

    /**
     * Sends the title to a player
     */
    public void send(@NotNull Player player) {
        player.showTitle(build());
    }

    /**
     * Sends the title to multiple players
     */
    public void send(@NotNull Collection<? extends Player> players) {
        Title title = build();
        players.forEach(player -> player.showTitle(title));
    }

    /**
     * Sends only the title part (no subtitle) to a player
     */
    public void sendTitleOnly(@NotNull Player player) {
        player.sendTitlePart(TitlePart.TITLE, title);
    }

    /**
     * Sends only the subtitle part (no title) to a player
     */
    public void sendSubtitleOnly(@NotNull Player player) {
        player.sendTitlePart(TitlePart.SUBTITLE, subtitle);
    }

    /**
     * Clears the current title for a player
     */
    public static void clear(@NotNull Player player) {
        player.clearTitle();
    }

    /**
     * Resets the title for a player (clears and resets times)
     */
    public static void reset(@NotNull Player player) {
        player.resetTitle();
    }

    // === Quick Static Methods ===

    /**
     * Quickly sends a simple title
     */
    public static void quick(@NotNull Player player, @NotNull String title) {
        create().title(title).send(player);
    }

    /**
     * Quickly sends a title with subtitle
     */
    public static void quick(@NotNull Player player, @NotNull String title, @NotNull String subtitle) {
        create().title(title).subtitle(subtitle).send(player);
    }

    /**
     * Sends a success title (green checkmark)
     */
    public static void success(@NotNull Player player, @NotNull String message) {
        create()
            .title("<green>✔</green>")
            .subtitle("<gray>" + message + "</gray>")
            .send(player);
    }

    /**
     * Sends an error title (red X)
     */
    public static void error(@NotNull Player player, @NotNull String message) {
        create()
            .title("<red>✖</red>")
            .subtitle("<gray>" + message + "</gray>")
            .send(player);
    }

    /**
     * Sends a warning title (yellow warning sign)
     */
    public static void warning(@NotNull Player player, @NotNull String message) {
        create()
            .title("<yellow>⚠</yellow>")
            .subtitle("<gray>" + message + "</gray>")
            .send(player);
    }

    /**
     * Sends an info title (blue info icon)
     */
    public static void info(@NotNull Player player, @NotNull String message) {
        create()
            .title("<aqua>ℹ</aqua>")
            .subtitle("<gray>" + message + "</gray>")
            .send(player);
    }

    /**
     * Sends a countdown title
     */
    public static void countdown(@NotNull Player player, int number) {
        String color = number <= 3 ? "<red>" : "<yellow>";
        create()
            .title(color + number)
            .fadeIn(0)
            .stay(800)
            .fadeOut(200)
            .send(player);
    }

    /**
     * Sends an animated "GO!" title
     */
    public static void go(@NotNull Player player) {
        create()
            .title("<green><bold>GO!</bold></green>")
            .fadeIn(0)
            .stay(1000)
            .fadeOut(500)
            .send(player);
    }
}