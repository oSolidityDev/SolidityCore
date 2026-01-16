package io.github.abdullahcxd.soliditycore.commands;

import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import io.github.abdullahcxd.soliditycore.builders.MessageBuilder;
import io.github.abdullahcxd.soliditycore.utils.SenderUtils;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    private SolidityPlugin solidityPlugin;
    private BaseCommand parent;

    public BaseCommand() {
        initialize();
        if (getCommandInfo().getSubcommands() != null && !getCommandInfo().getSubcommands().isEmpty()) {
            for (BaseCommand command : getCommandInfo().getSubcommands()) {
                command.setParent(this);
            }
        }
    }

    public abstract void initialize();
    public abstract CommandInfo getCommandInfo();
    public abstract void execute(CommandContext context);

    /**
     * Override this method to provide custom tab completion
     * @param sender The command sender
     * @param args The current arguments
     * @return List of tab completion options
     */
    public List<String> tabComplete(CommandSender sender, List<String> args) {
        return new ArrayList<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        // Check if command is player-only
        if (getCommandInfo().isPlayer() && !(sender instanceof Player)) {
            SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<red>You cannot use this command from console!</red>");
            return true;
        }

        // Check permissions
        if (getCommandInfo().getPermission() != null && !sender.hasPermission(getCommandInfo().getPermission())) {
            SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<red>You do not have permission to use this command.</red>");
            return true;
        }

        // Handle subcommands
        if (hasSubcommands()) {
            if (args.length == 0) {
                renderHelpMessage(sender);
                return true;
            }

            String subcommandName = args[0].toLowerCase();
            BaseCommand subcommand = findSubcommand(subcommandName);

            if (subcommand == null) {
                SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<red>Unknown subcommand:</red> <gold>" + subcommandName + "</gold>");
                renderHelpMessage(sender);
                return true;
            }

            String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
            return subcommand.onCommand(sender, command, label + " " + subcommandName, subArgs);
        }

        // Validate argument count
        List<CommandInfo.CommandArgument> commandArgs = getCommandInfo().getArguments();
        long requiredCount = commandArgs.stream()
                .filter(CommandInfo.CommandArgument::isRequired)
                .count();

        if (args.length < requiredCount) {
            SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<red>Not enough arguments!</red>");
            String fullCommand = buildFullCommandPath();
            SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<gold>Usage:</gold> /" + fullCommand + " " + buildArguments());
            SenderUtils.newline(sender);
            SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<red><> <gray>- Required, <green>[] <gray>- Optional</gray></red>");

            // Show argument descriptions if available
            if (commandArgs.stream().anyMatch(a -> a.getDescription() != null)) {
                SenderUtils.newline(sender);
                SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<yellow>Arguments:</yellow>");
                for (CommandInfo.CommandArgument arg : commandArgs) {
                    if (arg.getDescription() != null) {
                        String argDisplay = arg.isRequired() ? "<" + arg.getName() + ">" : "[" + arg.getName() + "]";
                        SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "  <gold>" + argDisplay + "</gold> <gray>- " + arg.getDescription() + "</gray>");
                    }
                }
            }
            return true;
        }

        // Execute command with parsed arguments
        try {
            CommandContext context = new CommandContext(sender, new ArrayList<>(Arrays.asList(args)));

            // Resolve and parse arguments
            ArgumentResolver.resolveArguments(context, getCommandInfo(), context.getRawArguments());

            execute(context);
        } catch (Exception e) {
            SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<red>An error occurred while executing this command.</red>");
            e.printStackTrace();
        }

        return true;
    }

    private boolean hasSubcommands() {
        return getCommandInfo().getSubcommands() != null && !getCommandInfo().getSubcommands().isEmpty();
    }

    private @Nullable BaseCommand findSubcommand(String name) {
        if (!hasSubcommands()) return null;

        return getCommandInfo().getSubcommands().stream()
                .filter(sub -> sub.getCommandInfo().getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public void renderHelpMessage(CommandSender sender) {
        String fullCommand = buildFullCommandPath();
        String displayName = fullCommand.substring(0, 1).toUpperCase() + fullCommand.substring(1);

        SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<yellow><bold>" + displayName + " Commands:</bold></yellow>");
        SenderUtils.newline(sender);
        SenderUtils.sendWithPrefix(sender, solidityPlugin.getSolidityPluginName(), "<red><> <gray>- Required, <green>[] <gray>- Optional</gray></red>");
        SenderUtils.newline(sender);

        if (hasSubcommands()) {
            for (BaseCommand sub : getCommandInfo().getSubcommands()) {
                CommandInfo info = sub.getCommandInfo();

                // Check permission for subcommand visibility
                if (info.getPermission() != null && !sender.hasPermission(info.getPermission())) {
                    continue;
                }

                MessageBuilder builder = MessageBuilder.create()
                        .append("/" + fullCommand + " " + info.getName() + " ", NamedTextColor.GOLD);

                // Sort arguments by position
                List<CommandInfo.CommandArgument> sortedArgs = new ArrayList<>(info.getArguments());
                compare(builder, sortedArgs);

                if (info.getDescription() != null && !info.getDescription().isEmpty()) {
                    builder.append("- " + info.getDescription(), NamedTextColor.GRAY);
                }

                SenderUtils.send(sender, builder.build());
            }
        } else {
            MessageBuilder builder = MessageBuilder.create()
                    .append("/" + fullCommand + " ", NamedTextColor.GOLD);

            // Sort arguments by position
            List<CommandInfo.CommandArgument> sortedArgs = new ArrayList<>(getCommandInfo().getArguments());
            compare(builder, sortedArgs);

            if (getCommandInfo().getDescription() != null) {
                builder.append("- " + getCommandInfo().getDescription(), NamedTextColor.GRAY);
            }

            SenderUtils.send(sender, builder.build());
        }

        SenderUtils.newline(sender);
    }

    private void compare(MessageBuilder builder, @NotNull List<CommandInfo.CommandArgument> sortedArgs) {
        sortedArgs.sort(Comparator.comparingInt(CommandInfo.CommandArgument::getPosition));

        for (CommandInfo.CommandArgument arg : sortedArgs) {
            if (arg.isRequired()) {
                builder.append("<" + arg.getName() + ">", NamedTextColor.RED);
            } else {
                builder.append("[" + arg.getName() + "]", NamedTextColor.GREEN);
            }
            builder.append(" ");
        }
    }

    public String buildArguments() {
        List<CommandInfo.CommandArgument> sortedArgs = new ArrayList<>(getCommandInfo().getArguments());
        sortedArgs.sort(Comparator.comparingInt(CommandInfo.CommandArgument::getPosition));

        return sortedArgs.stream()
                .map(arg -> arg.isRequired() ? "<" + arg.getName() + ">" : "[" + arg.getName() + "]")
                .collect(Collectors.joining(" "));
    }

    public String buildFullCommandPath() {
        if (parent == null) return getCommandInfo().getName();

        List<String> path = new ArrayList<>();
        BaseCommand current = this;
        while (current != null) {
            path.addFirst(current.getCommandInfo().getName());
            current = current.getParent();
        }

        return String.join(" ", path);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        // Check permissions
        if (getCommandInfo().getPermission() != null && !sender.hasPermission(getCommandInfo().getPermission())) {
            return new ArrayList<>();
        }

        // Handle subcommand tab completion
        if (hasSubcommands()) {
            if (args.length == 1) {
                String input = args[0].toLowerCase();
                return getCommandInfo().getSubcommands().stream()
                        .filter(sub -> sub.getCommandInfo().getPermission() == null ||
                                sender.hasPermission(sub.getCommandInfo().getPermission()))
                        .map(sub -> sub.getCommandInfo().getName())
                        .filter(name -> name.toLowerCase().startsWith(input))
                        .collect(Collectors.toList());
            } else if (args.length > 1) {
                String subName = args[0].toLowerCase();
                BaseCommand sub = findSubcommand(subName);
                if (sub != null) {
                    String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
                    return sub.onTabComplete(sender, command, label, subArgs);
                }
            }
            return new ArrayList<>();
        }

        // Call custom tab completion
        List<String> suggestions = tabComplete(sender, new ArrayList<>(Arrays.asList(args)));

        // Filter suggestions based on current input
        if (!args[args.length - 1].isEmpty()) {
            String currentArg = args[args.length - 1].toLowerCase();
            suggestions = suggestions.stream()
                    .filter(s -> s.toLowerCase().startsWith(currentArg))
                    .collect(Collectors.toList());
        }

        return suggestions;
    }
}