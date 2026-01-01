package io.github.abdullahcxd.soliditycore.commands;

import io.github.abdullahcxd.soliditycore.SolidityCore;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class CommandContext {

    private final SolidityCore core;
    private final CommandSender sender;
    private final List<String> rawArguments;
    private final Map<String, Object> arguments;
    private final Map<Integer, Object> positionalArguments;

    public CommandContext(CommandSender sender, List<String> rawArguments) {
        this.core = SolidityEditor.getInstance().getCore();
        this.sender = sender;
        this.rawArguments = new ArrayList<>(rawArguments);
        this.arguments = new HashMap<>();
        this.positionalArguments = new HashMap<>();
    }

    /**
     * Get a typed argument by name
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String name, Class<T> type) {
        Object value = arguments.get(name);
        if (value == null) return null;
        if (!type.isInstance(value)) {
            throw new ClassCastException("Argument '" + name + "' is not of type " + type.getSimpleName());
        }
        return (T) value;
    }

    /**
     * Get argument by name (returns Object)
     */
    public Object get(String name) {
        return arguments.get(name);
    }

    /**
     * Get a typed argument by position
     */
    @SuppressWarnings("unchecked")
    public <T> T get(int position, Class<T> type) {
        Object value = positionalArguments.get(position);
        if (value == null) return null;
        if (!type.isInstance(value)) {
            throw new ClassCastException("Argument at position " + position + " is not of type " + type.getSimpleName());
        }
        return (T) value;
    }

    /**
     * Get argument by position (returns Object)
     */
    public Object get(int position) {
        return positionalArguments.get(position);
    }

    /**
     * Get String argument by name
     */
    public String getString(String name) {
        return get(name, String.class);
    }

    /**
     * Get String argument by position
     */
    public String getString(int position) {
        return get(position, String.class);
    }

    /**
     * Get Integer argument by name
     */
    public Integer getInt(String name) {
        return get(name, Integer.class);
    }

    /**
     * Get Integer argument by position
     */
    public Integer getInt(int position) {
        return get(position, Integer.class);
    }

    /**
     * Get Double argument by name
     */
    public Double getDouble(String name) {
        return get(name, Double.class);
    }

    /**
     * Get Double argument by position
     */
    public Double getDouble(int position) {
        return get(position, Double.class);
    }

    /**
     * Get Boolean argument by name
     */
    public Boolean getBoolean(String name) {
        return get(name, Boolean.class);
    }

    /**
     * Get Boolean argument by position
     */
    public Boolean getBoolean(int position) {
        return get(position, Boolean.class);
    }

    /**
     * Get Player argument by name
     */
    public Player getPlayer(String name) {
        return get(name, Player.class);
    }

    /**
     * Get Player argument by position
     */
    public Player getPlayer(int position) {
        return get(position, Player.class);
    }

    /**
     * Get OfflinePlayer argument by name
     */
    public OfflinePlayer getOfflinePlayer(String name) {
        return get(name, OfflinePlayer.class);
    }

    /**
     * Get OfflinePlayer argument by position
     */
    public OfflinePlayer getOfflinePlayer(int position) {
        return get(position, OfflinePlayer.class);
    }

    /**
     * Get Material argument by name
     */
    public Material getMaterial(String name) {
        return get(name, Material.class);
    }

    /**
     * Get Material argument by position
     */
    public Material getMaterial(int position) {
        return get(position, Material.class);
    }

    /**
     * Get World argument by name
     */
    public World getWorld(String name) {
        return get(name, World.class);
    }

    /**
     * Get World argument by position
     */
    public World getWorld(int position) {
        return get(position, World.class);
    }

    /**
     * Check if argument exists by name
     */
    public boolean has(String name) {
        return arguments.containsKey(name);
    }

    /**
     * Check if argument exists at position
     */
    public boolean has(int position) {
        return positionalArguments.containsKey(position);
    }

    /**
     * Get sender as Player (throws exception if not a player)
     */
    public Player getPlayerSender() {
        if (!(sender instanceof Player)) {
            throw new IllegalStateException("Sender is not a player");
        }
        return (Player) sender;
    }

    /**
     * Check if sender is a player
     */
    public boolean isPlayer() {
        return sender instanceof Player;
    }

    void put(String name, int position, Object value) {
        arguments.put(name, value);
        positionalArguments.put(position, value);
    }
}