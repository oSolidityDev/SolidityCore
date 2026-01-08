package io.github.abdullahcxd.soliditycore.commands;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Builder(builderMethodName = "create")
@Getter
public class CommandInfo {

    @Builder
    @Getter
    public static class CommandArgument {
        private String name;
        private boolean required;
        private int position;
        @Builder.Default
        private ArgumentType type = ArgumentType.STRING;
        private String description;
    }

    public enum ArgumentType {
        STRING,
        INTEGER,
        DOUBLE,
        BOOLEAN,
        PLAYER,
        OFFLINE_PLAYER,
        MATERIAL,
        WORLD
    }

    private String name;
    private String description;
    private String permission;
    private String usage;
    @Builder.Default
    private List<String> aliases = new ArrayList<>();
    private boolean player;
    @Builder.Default
    private List<CommandArgument> arguments = new ArrayList<>();
    @Builder.Default
    private List<BaseCommand> subcommands = new ArrayList<>();
    private boolean subcommand;

}