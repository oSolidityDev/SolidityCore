package io.github.abdullahcxd.soliditycore.editor;

import io.github.abdullahcxd.soliditycore.SolidityCore;
import io.github.abdullahcxd.soliditycore.SolidityMetadata;
import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import io.github.abdullahcxd.soliditycore.exception.SolidityException;
import io.github.abdullahcxd.soliditycore.prefix.PrefixManager;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SolidityEditor {

    @Getter
    private static final SolidityEditor instance = new SolidityEditor();

    private final Map<String, SolidityMetadata> metadata;
    private SolidityCore core;
    private PrefixManager prefixManager;

    private List<String> blacklistedPlugins;

    private SolidityEditor() {
        this.metadata = new HashMap<>();
    }

    public void registerPluginMeta(@NotNull SolidityPlugin plugin) {
        this.metadata.put(plugin.getSolidityPluginName(), plugin.getSolidityMetadata());
    }

    public void unregisterPluginMeta(@NotNull SolidityPlugin plugin) {
        this.metadata.remove(plugin.getSolidityPluginName());
    }

    public Map<String, String> getPluginsVersion() {
        return metadata.values().stream()
                .collect(Collectors.toMap(SolidityMetadata::getPluginName, SolidityMetadata::getPluginVersion));
    }

    public Optional<SolidityMetadata> getPlugin(@NotNull String name) {
        return metadata.values().stream()
                .filter(m -> m.getPluginName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<String> getAuthors(@NotNull String name) {
        return getPlugin(name)
                .map(SolidityMetadata::getAuthors)
                .orElse(Collections.emptyList());
    }

    public void broadcastPluginInfo() {
        metadata.values().forEach(meta -> {
            System.out.println(meta.getPluginName() + " v" + meta.getPluginVersion()
                    + " by " + String.join(", ", meta.getAuthors()));
        });
    }

    public int reloadAll() {
        int reloaded = 0;

        reloadNecessaryStuff();

        for (SolidityMetadata meta : metadata.values()) {
            Optional<SolidityPlugin> pluginOptional = core.getServer().getPluginManager()
                    .getPlugin(meta.getPluginName()) instanceof SolidityPlugin sp ? Optional.of(sp) : Optional.empty();

            if (pluginOptional.isPresent()) {
                SolidityPlugin plugin = pluginOptional.get();
                if (isBlacklisted(plugin)) {

                    continue;
                }

                try {
                    plugin.reloadConfigurations(); // reload the plugin configuration
                    reloaded++;
                } catch (Exception e) {
                    throw new SolidityException(e);
                }
            } else {
                System.err.println("Plugin " + meta.getPluginName() + " not found or not a SolidityPlugin.");
            }
        }

        return reloaded;
    }

    public void initialize() {
        prefixManager = new PrefixManager(SenderUtils.SOLIDITY_PREFIX);

        prefixManager.register("solidity", SenderUtils.SOLIDITY_PREFIX);
        prefixManager.register("info", PrefixManager.Presets.info());
        prefixManager.register("success", PrefixManager.Presets.success());
        prefixManager.register("error", PrefixManager.Presets.error());
        prefixManager.register("warning", PrefixManager.Presets.warning());
        prefixManager.register("debug", PrefixManager.Presets.debug());
        prefixManager.register("system", PrefixManager.Presets.system());
    }

    public boolean isBlacklisted(String name) {
        return blacklistedPlugins.contains(name);
    }

    public boolean isBlacklisted(@NotNull SolidityPlugin plugin) {
        return isBlacklisted(plugin.getSolidityPluginName());
    }

    public void registerSenderPrefix(String keyId, String prefix) {
        prefixManager.register(keyId, prefix);
    }

    public void setCore(@NotNull SolidityCore core) {
        core.reloadConfig();
        this.core = core;

        reloadNecessaryStuff();
    }

    public void disablePlugin(SolidityPlugin plugin) {
        getPluginManager().disablePlugin(plugin);
        unregisterPluginMeta(plugin);
    }

    public PluginManager getPluginManager() {
        return Bukkit.getPluginManager();
    }

    private void reloadNecessaryStuff() {
        this.blacklistedPlugins = core.getConfig().getStringList("plugins.blacklisted");
    }

    public boolean isPluginLoaded(@NotNull String name) {
        return getPlugin(name).isPresent();
    }
}
