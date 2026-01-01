package io.github.abdullahcxd.soliditycore.builders;

import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A comprehensive builder for creating Adventure Component messages with modern features.
 * Supports MiniMessage, legacy color codes, formatting, click events, hover events, and more.
 */
@Getter
public class MessageBuilder {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY_SERIALIZER =
            LegacyComponentSerializer.legacyAmpersand();

    private final List<Component> components;
    private TextColor currentColor;
    private final List<TextDecoration> currentDecorations;
    private ClickEvent currentClickEvent;
    private HoverEvent<?> currentHoverEvent;

    /**
     * Creates a new MessageBuilder instance
     * @return new MessageBuilder
     */
    @Contract(value = " -> new", pure = true)
    public static @NotNull MessageBuilder create() {
        return new MessageBuilder();
    }

    /**
     * Creates a MessageBuilder from existing text using MiniMessage format
     * @param miniMessage the MiniMessage formatted string
     * @return new MessageBuilder with parsed content
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MessageBuilder fromMiniMessage(@NotNull String miniMessage) {
        MessageBuilder builder = new MessageBuilder();
        builder.components.add(MINI_MESSAGE.deserialize(miniMessage));
        return builder;
    }

    /**
     * Creates a MessageBuilder from legacy formatted text (with & color codes)
     * @param legacyText the legacy formatted string
     * @return new MessageBuilder with parsed content
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull MessageBuilder fromLegacy(@NotNull String legacyText) {
        MessageBuilder builder = new MessageBuilder();
        builder.components.add(LEGACY_SERIALIZER.deserialize(legacyText));
        return builder;
    }

    private MessageBuilder() {
        this.components = new ArrayList<>();
        this.currentDecorations = new ArrayList<>();
    }

    /**
     * Appends plain text to the message
     * @param text the text to append
     * @return this builder
     */
    public MessageBuilder append(@NotNull String text) {
        Component component = Component.text(text);
        component = applyCurrentFormatting(component);
        components.add(component);
        return this;
    }

    /**
     * Appends text with a specific color
     * @param text the text to append
     * @param color the text color
     * @return this builder
     */
    public MessageBuilder append(@NotNull String text, @NotNull TextColor color) {
        Component component = Component.text(text, color);
        component = applyCurrentEvents(component);
        components.add(component);
        return this;
    }

    /**
     * Appends text with a named color
     * @param text the text to append
     * @param color the named text color
     * @return this builder
     */
    public MessageBuilder append(@NotNull String text, @NotNull NamedTextColor color) {
        return append(text, (TextColor) color);
    }

    /**
     * Appends a Component directly
     * @param component the component to append
     * @return this builder
     */
    public MessageBuilder append(@NotNull Component component) {
        components.add(component);
        return this;
    }

    /**
     * Appends text parsed as MiniMessage
     * @param miniMessage the MiniMessage formatted string
     * @return this builder
     */
    public MessageBuilder appendMiniMessage(@NotNull String miniMessage) {
        components.add(MINI_MESSAGE.deserialize(miniMessage));
        return this;
    }

    /**
     * Appends text parsed as legacy format (& color codes)
     * @param legacyText the legacy formatted string
     * @return this builder
     */
    public MessageBuilder appendLegacy(@NotNull String legacyText) {
        components.add(LEGACY_SERIALIZER.deserialize(legacyText));
        return this;
    }

    /**
     * Appends text with hex color support (&#RRGGBB format)
     * @param text the text with hex colors
     * @return this builder
     */
    public MessageBuilder appendWithHex(@NotNull String text) {
        Matcher matcher = HEX_PATTERN.matcher(text);
        if (!matcher.find()) {
            return append(text);
        }

        int lastEnd = 0;
        matcher.reset();

        while (matcher.find()) {
            if (matcher.start() > lastEnd) {
                append(text.substring(lastEnd, matcher.start()));
            }

            String hexCode = matcher.group(1);
            TextColor color = TextColor.fromHexString("#" + hexCode);

            int nextStart = text.indexOf("&#", matcher.end());
            String coloredText = nextStart == -1 ?
                    text.substring(matcher.end()) :
                    text.substring(matcher.end(), nextStart);

            if (!coloredText.isEmpty()) {
                assert color != null;
                append(coloredText, color);
            }

            lastEnd = matcher.end() + coloredText.length();
        }

        if (lastEnd < text.length()) {
            append(text.substring(lastEnd));
        }

        return this;
    }

    /**
     * Appends a newline
     * @return this builder
     */
    public MessageBuilder newline() {
        components.add(Component.newline());
        return this;
    }

    /**
     * Appends a space
     * @return this builder
     */
    public MessageBuilder space() {
        components.add(Component.space());
        return this;
    }

    /**
     * Sets the current color for subsequent appends
     * @param color the color to set
     * @return this builder
     */
    public MessageBuilder color(@NotNull TextColor color) {
        this.currentColor = color;
        return this;
    }

    /**
     * Sets the current color from a hex string
     * @param hex the hex color (e.g., "#FF5733" or "FF5733")
     * @return this builder
     */
    public MessageBuilder color(@NotNull String hex) {
        if (!hex.startsWith("#")) {
            hex = "#" + hex;
        }
        this.currentColor = TextColor.fromHexString(hex);
        return this;
    }

    /**
     * Resets the current color
     * @return this builder
     */
    public MessageBuilder resetColor() {
        this.currentColor = null;
        return this;
    }

    /**
     * Adds a decoration (bold, italic, etc.)
     * @param decoration the decoration to add
     * @return this builder
     */
    public MessageBuilder decorate(@NotNull TextDecoration decoration) {
        if (!currentDecorations.contains(decoration)) {
            currentDecorations.add(decoration);
        }
        return this;
    }

    /**
     * Makes subsequent text bold
     * @return this builder
     */
    public MessageBuilder bold() {
        return decorate(TextDecoration.BOLD);
    }

    /**
     * Makes subsequent text italic
     * @return this builder
     */
    public MessageBuilder italic() {
        return decorate(TextDecoration.ITALIC);
    }

    /**
     * Makes subsequent text underlined
     * @return this builder
     */
    public MessageBuilder underline() {
        return decorate(TextDecoration.UNDERLINED);
    }

    /**
     * Makes subsequent text strikethrough
     * @return this builder
     */
    public MessageBuilder strikethrough() {
        return decorate(TextDecoration.STRIKETHROUGH);
    }

    /**
     * Makes subsequent text obfuscated
     * @return this builder
     */
    public MessageBuilder obfuscate() {
        return decorate(TextDecoration.OBFUSCATED);
    }

    /**
     * Resets all decorations
     * @return this builder
     */
    public MessageBuilder resetDecorations() {
        currentDecorations.clear();
        return this;
    }

    /**
     * Resets all formatting (color and decorations)
     * @return this builder
     */
    public MessageBuilder reset() {
        resetColor();
        resetDecorations();
        resetEvents();
        return this;
    }

    /**
     * Sets a click event to open a URL
     * @param url the URL to open
     * @return this builder
     */
    public MessageBuilder clickOpenUrl(@NotNull String url) {
        this.currentClickEvent = ClickEvent.openUrl(url);
        return this;
    }

    /**
     * Sets a click event to run a command
     * @param command the command to run (without /)
     * @return this builder
     */
    public MessageBuilder clickRunCommand(@NotNull String command) {
        this.currentClickEvent = ClickEvent.runCommand("/" + command);
        return this;
    }

    /**
     * Sets a click event to suggest a command
     * @param command the command to suggest
     * @return this builder
     */
    public MessageBuilder clickSuggestCommand(@NotNull String command) {
        this.currentClickEvent = ClickEvent.suggestCommand("/" + command);
        return this;
    }

    /**
     * Sets a click event to copy text to clipboard
     * @param text the text to copy
     * @return this builder
     */
    public MessageBuilder clickCopyToClipboard(@NotNull String text) {
        this.currentClickEvent = ClickEvent.copyToClipboard(text);
        return this;
    }

    /**
     * Sets a hover event to show text
     * @param text the text to show
     * @return this builder
     */
    public MessageBuilder hoverShowText(@NotNull String text) {
        this.currentHoverEvent = HoverEvent.showText(Component.text(text));
        return this;
    }

    /**
     * Sets a hover event to show a component
     * @param component the component to show
     * @return this builder
     */
    public MessageBuilder hoverShowText(@NotNull Component component) {
        this.currentHoverEvent = HoverEvent.showText(component);
        return this;
    }

    /**
     * Sets a hover event using a builder pattern
     * @param consumer the consumer to configure hover text
     * @return this builder
     */
    public MessageBuilder hoverShowText(@NotNull Consumer<MessageBuilder> consumer) {
        MessageBuilder hoverBuilder = MessageBuilder.create();
        consumer.accept(hoverBuilder);
        this.currentHoverEvent = HoverEvent.showText(hoverBuilder.build());
        return this;
    }

    /**
     * Resets all events (click and hover)
     * @return this builder
     */
    public MessageBuilder resetEvents() {
        this.currentClickEvent = null;
        this.currentHoverEvent = null;
        return this;
    }

    /**
     * Adds a clickable and hoverable text segment
     * @param text the text to display
     * @param clickCommand the command to run on click
     * @param hoverText the text to show on hover
     * @return this builder
     */
    public MessageBuilder clickableHoverable(@NotNull String text,
                                             @NotNull String clickCommand,
                                             @NotNull String hoverText) {
        Component component = Component.text(text)
                .clickEvent(ClickEvent.runCommand("/" + clickCommand))
                .hoverEvent(HoverEvent.showText(Component.text(hoverText)));

        component = applyCurrentFormatting(component);
        components.add(component);
        return this;
    }

    /**
     * Applies current formatting to a component
     */
    private Component applyCurrentFormatting(Component component) {
        if (currentColor != null) {
            component = component.color(currentColor);
        }

        for (TextDecoration decoration : currentDecorations) {
            component = component.decorate(decoration);
        }

        component = applyCurrentEvents(component);
        return component;
    }

    /**
     * Applies current events to a component
     */
    private Component applyCurrentEvents(Component component) {
        if (currentClickEvent != null) {
            component = component.clickEvent(currentClickEvent);
        }

        if (currentHoverEvent != null) {
            component = component.hoverEvent(currentHoverEvent);
        }

        return component;
    }

    /**
     * Builds the final Component
     * @return the built Component
     */
    public Component build() {
        if (components.isEmpty()) {
            return Component.empty();
        }

        if (components.size() == 1) {
            return components.getFirst();
        }

        TextComponent.Builder builder = Component.text();
        for (Component component : components) {
            builder.append(component);
        }

        return builder.build();
    }

    /**
     * Builds and returns as a TextComponent
     * @return the built TextComponent
     */
    public TextComponent buildText() {
        Component component = build();
        return component instanceof TextComponent ?
                (TextComponent) component :
                Component.text().append(component).build();
    }

    /**
     * Converts the message to MiniMessage format
     * @return the MiniMessage string
     */
    public String toMiniMessage() {
        return MINI_MESSAGE.serialize(build());
    }

    /**
     * Converts the message to legacy format (& color codes)
     * @return the legacy formatted string
     */
    public String toLegacy() {
        return LEGACY_SERIALIZER.serialize(build());
    }

    @Override
    public String toString() {
        return toLegacy();
    }
}