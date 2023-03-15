package com.sadshrimpy.eggcitingeaster;

import com.sadshrimpy.eggcitingeaster.commands.CommandManager;
import com.sadshrimpy.eggcitingeaster.commands.TabCompleterManager;
import com.sadshrimpy.eggcitingeaster.events.EventManager;
import com.sadshrimpy.eggcitingeaster.utils.sadlibrary.SadLibrary;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class EggcitingEaster extends JavaPlugin {

    public static SadLibrary sadLibrary = new SadLibrary();

    @Override
    public void onEnable() {
        // Plugin startup logic
        sadLibrary.initialize();
        PluginCommand cmd = getCommand("eggcitingeaster");
        PluginManager pm = getServer().getPluginManager();

        // Command
        assert cmd != null;
        cmd.setExecutor(new CommandManager());
        cmd.setTabCompleter(new TabCompleterManager());

        // Events
        pm.registerEvents(new EventManager(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}