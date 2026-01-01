package io.github.abdullahcxd.soliditycore;

import io.github.abdullahcxd.soliditycore.utils.ChainedList;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SolidityMetadata {

    private final String pluginName;
    private final String pluginVersion;

    @Builder.Default
    private final List<String> authors = ChainedList.startChain("YourName");

    @Builder.Default
    private final String description = "No description provided";

    @Builder.Default
    private final List<String> dependencies = List.of();
}
