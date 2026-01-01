package io.github.abdullahcxd.soliditycore.listener;

import io.github.abdullahcxd.soliditycore.SolidityPlugin;
import io.github.abdullahcxd.soliditycore.editor.SolidityEditor;
import lombok.Getter;
import org.bukkit.event.Listener;

@Getter
public class SolidityListener implements Listener {

    private SolidityEditor editor;
    private SolidityPlugin plugin;

    public SolidityListener initialize(SolidityPlugin plugin) {
        this.editor = SolidityEditor.getInstance();
        this.plugin = plugin;
        return this;
    }
}
