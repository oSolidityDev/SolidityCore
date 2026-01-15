package io.github.abdullahcxd.soliditycore;

import io.github.abdullahcxd.soliditycore.commands.CommandManager;
import io.github.abdullahcxd.soliditycore.commands.base.SolidityCoreCommand;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class SolidityCore extends JavaPlugin {

    @Override
    public void onLoad() {
        saveDefaultConfig();
        SolidityEditor.getInstance().setCore(this);
        SolidityEditor.getInstance().initialize();
        SenderUtils.initialize();

        ConsoleCommandSender console = getConsoleCommandSender();

        SenderUtils.sendPrefixed(console, SenderUtils.separator(32));
        SenderUtils.newline(console);
        SenderUtils.sendPrefixed(console, "<green>Loading SolidityCore version <gold>"
                + getPluginMeta().getVersion() + "</gold></green>");
        SenderUtils.newline(console);
        SenderUtils.sendPrefixed(console, SenderUtils.separator(32));

        loadLibraries();
    }

    @Override
    public void onEnable() {
        ConsoleCommandSender console = getConsoleCommandSender();

        CommandManager.registerCommand(this, new SolidityCoreCommand());

        // Check dependencies for all registered plugins
        for (SolidityPlugin plugin : getRegisteredPlugins()) {
            if (plugin.hasDependencies() && !plugin.checkDependenciesLoaded()) {
                SenderUtils.error(console, "Plugin " + plugin.getSolidityMetadata().getPluginName()
                        + " has missing dependencies! It will be disabled.");
                getServer().getPluginManager().disablePlugin(plugin);
            }
        }

        // Optionally log all loaded plugins
        SenderUtils.sendPrefixed(console, "<green>All plugins loaded successfully!</green>");
        SolidityEditor.getInstance().broadcastPluginInfo();
    }

    @Override
    public void onDisable() {
        ConsoleCommandSender console = getConsoleCommandSender();
        SenderUtils.sendPrefixed(console, "<red>Shutting down SolidityCore...</red>");
    }

    public @NotNull ConsoleCommandSender getConsoleCommandSender() {
        return getServer().getConsoleSender();
    }

    /**
     * Fetches all registered SolidityPlugins
     * This requires that all plugins extending SolidityPlugin register themselves in SolidityEditor
     */
    private SolidityPlugin @NotNull [] getRegisteredPlugins() {
        return SolidityEditor.getInstance()
                .getMetadata()
                .values()
                .stream()
                .map(meta -> (SolidityPlugin) getServer().getPluginManager().getPlugin(meta.getPluginName()))
                .filter(Objects::nonNull)
                .toArray(SolidityPlugin[]::new);
    }

    private void loadLibraries() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this);

        libraryManager.addMavenCentral();

        Library hikari = Library.builder()
                .groupId("com.zaxxer")
                .artifactId("HikariCP")
                .version("7.0.2")
                .id("hikaricp")
                .isolatedLoad(true)
                .build();
        libraryManager.loadLibrary(hikari);

        Library mariadb = Library.builder()
                .groupId("org.mariadb.jdbc")
                .artifactId("mariadb-java-client")
                .version("3.5.7")
                .id("mariadb")
                .isolatedLoad(true)
                .build();
        libraryManager.loadLibrary(mariadb);

        Library mysql = Library.builder()
                .groupId("com.mysql")
                .artifactId("mysql-connector-j")
                .version("9.5.0")
                .id("mysql")
                .isolatedLoad(true)
                .build();
        libraryManager.loadLibrary(mysql);

        Library postgresql = Library.builder()
                .groupId("org.postgresql")
                .artifactId("postgresql")
                .version("42.7.8")
                .id("postgresql")
                .isolatedLoad(true)
                .build();
        libraryManager.loadLibrary(postgresql);

        Library h2 = Library.builder()
                .groupId("com.h2database")
                .artifactId("h2")
                .version("2.4.240")
                .id("h2")
                .isolatedLoad(true)
                .build();
        libraryManager.loadLibrary(h2);

        Library sqlite = Library.builder()
                .groupId("org.xerial")
                .artifactId("sqlite-jdbc")
                .version("3.51.1.0")
                .id("sqlite")
                .isolatedLoad(true)
                .build();
        libraryManager.loadLibrary(sqlite);

        libraryManager.addMavenCentral();

    }
}
