package com.sadshrimpy.eggcitingeaster.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
//import org.bukkit.command.TabCompleter;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.StringUtil;

import java.util.*;

import static com.sadshrimpy.eggcitingeaster.EggcitingEaster.sadLibrary;

public class TabCompleterManager implements TabCompleter {

    private CommandSender sender;
    private String[] args;

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {

        this.sender = sender;
        this.args = args;

        boolean permission = sender.hasPermission(sadLibrary.permissions().getCompleter());

        switch (args.length) {
            case 1:
                return firstArg(permission);

            case 2:
                return secondArg(permission);

            case 3:
                return thirdArg(permission);
        }

        return new ArrayList<>();
    }


    private List<String> firstArg(boolean permission) {
        if (permission)
            return StringUtil.copyPartialMatches(args[0], Arrays.asList("help", "give", "retrive", "reload"), new ArrayList<>());
        else
            return StringUtil.copyPartialMatches(args[0], Collections.singletonList("<You don't have the permission>"), new ArrayList<>());
    }

    private List<String> secondArg(boolean permission) {
        if (permission)
            switch (args[0].toLowerCase()) {
                case "give":
                    List<String> players = new ArrayList<>(Bukkit.getOnlinePlayers().size());
                    for (Player player : Bukkit.getOnlinePlayers())
                        players.add(player.getName());
                    players.add("*");
                    return StringUtil.copyPartialMatches(args[1], players, new ArrayList<>());

                case "retrive":
                    // /eggcitingeaster retrive <eggID>
                    return StringUtil.copyPartialMatches(args[1], getItems(sadLibrary.configurations().getConfigConfiguration()), new ArrayList<>());
            }
        else
            return StringUtil.copyPartialMatches(args[1], Collections.singletonList("<You don't have the permission>"), new ArrayList<>());
        return null;
    }


    private List<String> thirdArg(boolean permission) {
        if (permission) {
            switch (args[0].toLowerCase()) {
                case "give":
                    // /eggcitingeaster give <player/*> <eggID>
                    return StringUtil.copyPartialMatches(args[2], getItems(sadLibrary.configurations().getConfigConfiguration()), new ArrayList<>());
            }
        }
        return null;
    }

    private static List<String> getItems(FileConfiguration config) {
        List<String> objs = new ArrayList<>();
        Objects.requireNonNull(config.getConfigurationSection("egg-item")).getKeys(false).forEach(str -> objs.add(str));
        return objs;
    }
}