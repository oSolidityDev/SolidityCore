package io.github.abdullahcxd.soliditycore.builders;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A builder for playing sounds to players with a fluent API
 */
public class SoundBuilder {

    private Sound sound;
    private SoundCategory category = SoundCategory.MASTER;
    private float volume = 1.0f;
    private float pitch = 1.0f;

    private SoundBuilder(@NotNull Sound sound) {
        this.sound = sound;
    }

    /**
     * Creates a new SoundBuilder
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull SoundBuilder create(@NotNull Sound sound) {
        return new SoundBuilder(sound);
    }

    /**
     * Sets the sound to play
     */
    public SoundBuilder sound(@NotNull Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Sets the sound category
     */
    public SoundBuilder category(@NotNull SoundCategory category) {
        this.category = category;
        return this;
    }

    /**
     * Sets the volume (0.0 to 1.0, can go higher)
     */
    public SoundBuilder volume(float volume) {
        this.volume = Math.max(0.0f, volume);
        return this;
    }

    /**
     * Sets the pitch (0.5 to 2.0 is normal range)
     */
    public SoundBuilder pitch(float pitch) {
        this.pitch = Math.max(0.5f, Math.min(2.0f, pitch));
        return this;
    }

    /**
     * Sets volume to quiet (0.3)
     */
    public SoundBuilder quiet() {
        return volume(0.3f);
    }

    /**
     * Sets volume to normal (1.0)
     */
    public SoundBuilder normal() {
        return volume(1.0f);
    }

    /**
     * Sets volume to loud (1.5)
     */
    public SoundBuilder loud() {
        return volume(1.5f);
    }

    /**
     * Sets pitch to low (0.8)
     */
    public SoundBuilder low() {
        return pitch(0.8f);
    }

    /**
     * Sets pitch to high (1.2)
     */
    public SoundBuilder high() {
        return pitch(1.2f);
    }

    /**
     * Plays the sound to a player
     */
    public void play(@NotNull Player player) {
        player.playSound(player.getLocation(), sound, category, volume, pitch);
    }

    /**
     * Plays the sound to multiple players
     */
    public void play(@NotNull Collection<? extends Player> players) {
        players.forEach(this::play);
    }

    /**
     * Plays the sound at a location for all nearby players
     */
    public void playAt(@NotNull Location location) {
        if (location.getWorld() != null) {
            location.getWorld().playSound(location, sound, category, volume, pitch);
        }
    }

    // === Quick Static Methods ===

    /**
     * Plays a sound quickly with default settings
     */
    public static void quick(@NotNull Player player, @NotNull Sound sound) {
        create(sound).play(player);
    }

    /**
     * Plays a sound at a location
     */
    public static void quickAt(@NotNull Location location, @NotNull Sound sound) {
        create(sound).playAt(location);
    }

    // === Common Sound Presets ===

    /**
     * Plays a success sound (level up)
     */
    public static void success(@NotNull Player player) {
        create(Sound.ENTITY_PLAYER_LEVELUP)
            .volume(0.5f)
            .pitch(1.2f)
            .play(player);
    }

    /**
     * Plays an error sound (villager no)
     */
    public static void error(@NotNull Player player) {
        create(Sound.ENTITY_VILLAGER_NO)
            .volume(0.5f)
            .pitch(0.8f)
            .play(player);
    }

    /**
     * Plays a warning sound (note bass)
     */
    public static void warning(@NotNull Player player) {
        create(Sound.BLOCK_NOTE_BLOCK_BASS)
            .volume(0.6f)
            .pitch(0.5f)
            .play(player);
    }

    /**
     * Plays a click sound (UI button)
     */
    public static void click(@NotNull Player player) {
        create(Sound.UI_BUTTON_CLICK)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a pop sound (item pickup)
     */
    public static void pop(@NotNull Player player) {
        create(Sound.ENTITY_ITEM_PICKUP)
            .volume(0.3f)
            .pitch(1.5f)
            .play(player);
    }

    /**
     * Plays a ding sound (experience orb)
     */
    public static void ding(@NotNull Player player) {
        create(Sound.ENTITY_EXPERIENCE_ORB_PICKUP)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a whoosh sound (bat takeoff)
     */
    public static void whoosh(@NotNull Player player) {
        create(Sound.ENTITY_BAT_TAKEOFF)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a teleport sound (enderman)
     */
    public static void teleport(@NotNull Player player) {
        create(Sound.ENTITY_ENDERMAN_TELEPORT)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays an anvil sound
     */
    public static void anvil(@NotNull Player player) {
        create(Sound.BLOCK_ANVIL_LAND)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a breaking sound (glass)
     */
    public static void breaking(@NotNull Player player) {
        create(Sound.BLOCK_GLASS_BREAK)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a countdown tick sound
     */
    public static void countdownTick(@NotNull Player player) {
        create(Sound.BLOCK_NOTE_BLOCK_HAT)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a countdown finish sound
     */
    public static void countdownFinish(@NotNull Player player) {
        create(Sound.BLOCK_NOTE_BLOCK_PLING)
            .volume(0.7f)
            .pitch(2.0f)
            .play(player);
    }

    /**
     * Plays an explosion sound
     */
    public static void explosion(@NotNull Player player) {
        create(Sound.ENTITY_GENERIC_EXPLODE)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a bell sound
     */
    public static void bell(@NotNull Player player) {
        create(Sound.BLOCK_BELL_USE)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a door open sound
     */
    public static void doorOpen(@NotNull Player player) {
        create(Sound.BLOCK_WOODEN_DOOR_OPEN)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }

    /**
     * Plays a door close sound
     */
    public static void doorClose(@NotNull Player player) {
        create(Sound.BLOCK_WOODEN_DOOR_CLOSE)
            .volume(0.5f)
            .pitch(1.0f)
            .play(player);
    }
}