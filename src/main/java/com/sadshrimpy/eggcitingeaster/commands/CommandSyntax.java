package com.sadshrimpy.eggcitingeaster.commands;

import org.bukkit.command.CommandSender;

public interface CommandSyntax {
    abstract String getName();
    abstract String getPermission(String[] args);
    abstract boolean hasSubcommands();
    abstract void perform(CommandSender sender, String[] args);
}
