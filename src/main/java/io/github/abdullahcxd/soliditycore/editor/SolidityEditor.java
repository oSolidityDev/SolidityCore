package io.github.abdullahcxd.soliditycore.editor;

import io.github.abdullahcxd.soliditycore.SolidityCore;
import io.github.abdullahcxd.soliditycore.SolidityMetadata;
import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class SolidityEditor {

    @Getter
    private static final SolidityEditor instance = new SolidityEditor();

    private final List<SolidityMetadata> metadata;
    @Setter
    private SolidityCore core;

    private SolidityEditor() {
        this.metadata = new ArrayList<>();
    }

    public void registerPluginMeta(@NotNull SolidityPlugin plugin) {
        this.metadata.add(plugin.getSolidityMetadata());
    }

    public Map<String, String> getPluginsVersion() {
        return metadata.stream()
                .collect(Collectors.toMap(SolidityMetadata::getPluginName, SolidityMetadata::getPluginVersion));
    }

    public Optional<SolidityMetadata> getPlugin(@NotNull String name) {
        return metadata.stream()
                .filter(m -> m.getPluginName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<String> getAuthors(@NotNull String name) {
        return getPlugin(name)
                .map(SolidityMetadata::getAuthors)
                .orElse(Collections.emptyList());
    }

    public void broadcastPluginInfo() {
        metadata.forEach(meta -> {
            System.out.println(meta.getPluginName() + " v" + meta.getPluginVersion()
                    + " by " + String.join(", ", meta.getAuthors()));
        });
    }

    public int reloadAll() {
        int reloaded = 0;

        for (SolidityMetadata meta : metadata) {
            Optional<SolidityPlugin> pluginOptional = core.getServer().getPluginManager()
                    .getPlugin(meta.getPluginName()) instanceof SolidityPlugin sp ? Optional.of(sp) : Optional.empty();

            if (pluginOptional.isPresent()) {
                try {
                    pluginOptional.get().reloadConfigurations(); // reload the plugin configuration
                    reloaded++;
                } catch (Exception e) {
                    System.err.println("Failed to reload plugin " + meta.getPluginName());
                    e.printStackTrace();
                }
            } else {
                System.err.println("Plugin " + meta.getPluginName() + " not found or not a SolidityPlugin.");
            }
        }

        return reloaded;
    }


    public boolean isPluginLoaded(@NotNull String name) {
        return getPlugin(name).isPresent();
    }
}
