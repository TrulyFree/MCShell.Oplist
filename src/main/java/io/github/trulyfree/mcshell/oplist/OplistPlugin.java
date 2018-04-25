package io.github.trulyfree.mcshell.oplist;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import io.github.trulyfree.mcshell.config.ConfigurationPlugin;
import io.github.trulyfree.mcshell.config.ConfiguredShellPlugin;
import io.github.trulyfree.mcshell.plugin.ShellPlugin;
import io.github.trulyfree.mcshell.shell.CommandManager;
import io.github.trulyfree.plugins.annotation.Plugin;
import io.github.trulyfree.plugins.plugin.PluginBuilder;
import io.github.trulyfree.plugins.plugin.PluginManager;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Plugin(name = "oplist",
        builder = OplistPlugin.Builder.class,
        manager = CommandManager.class,
        dependsOn = {ConfigurationPlugin.class})
public final class OplistPlugin implements ConfiguredShellPlugin<OplistConfig> {
    private final @NonNull @NotNull AtomicReference<OplistConfig> oplistConfig = new AtomicReference<>();
    private final @NonNull @NotNull List<Oplist.OplistEntry> oplist;

    public OplistPlugin(final PluginManager<? super ShellPlugin> pluginManager) {
        AtomicReference<OplistConfig> oplistConfigAtomicReference = new AtomicReference<>();
        pluginManager.findPlugin(
                ConfigurationPlugin.class,
                configurationPlugin -> {
                    try {
                        oplistConfig.set(configurationPlugin.getConfig(OplistPlugin.this));
                    } catch (FileNotFoundException e) {
                        oplistConfig.set(new OplistConfig("/path/to/ops.json"));
                        try {
                            configurationPlugin.saveConfig(this);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }
        );
        File oplistFile = new File(oplistConfig.get().getOplistFile());
        if (!oplistFile.exists() || !oplistFile.canRead()) {
            throw new IllegalArgumentException("Oplist config must define a valid path to an oplist file.");
        }
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(oplistFile)) {
            oplist = Collections.unmodifiableList(gson.fromJson(
                    reader,
                    Oplist.class
            ));
        } catch (IOException | JsonParseException e) {
            throw new IllegalArgumentException("Oplist config must define a valid oplist file.");
        }
    }

    @NonNull
    @NotNull
    public List<Oplist.OplistEntry> getOplist() {
        return oplist;
    }

    @NonNull
    @NotNull
    @Override
    public OplistConfig getConfig() {
        return oplistConfig.get();
    }

    @NonNull
    @NotNull
    @Override
    public Class<? extends OplistConfig> getConfigClass() {
        return OplistConfig.class;
    }

    public static class Builder implements PluginBuilder<ShellPlugin> {
        @NonNull
        @NotNull
        @Override
        public OplistPlugin build(@NonNull @NotNull PluginManager<? super ShellPlugin> pluginManager) {
            return new OplistPlugin(pluginManager);
        }
    }
}
