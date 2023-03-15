package com.sadshrimpy.eggcitingeaster.commands.subcommands.args0.retrive;

import com.sadshrimpy.eggcitingeaster.commands.CommandSyntax;
import com.sadshrimpy.eggcitingeaster.items.EasterEgg;
import com.sadshrimpy.eggcitingeaster.utils.sadlibrary.SadMessages;
import com.sadshrimpy.eggcitingeaster.utils.sadlibrary.SadPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import static com.sadshrimpy.eggcitingeaster.EggcitingEaster.sadLibrary;

public class RetriveCommand implements CommandSyntax {

    @Override
    public String getName() {
        return "retrive";
    }

    @Override
    public String getPermission(String[] args) {
        return sadLibrary.permissions().getRetrive();
    }

    @Override
    public boolean hasSubcommands() {
        return false;
    }

    @Override
    public void perform(CommandSender sender, String[] args) {
        // /eggcitingeaster retrive ID
        FileConfiguration config = sadLibrary.configurations().getConfigConfiguration();
        FileConfiguration msgConfig = sadLibrary.configurations().getMessagesConfiguration();
        SadMessages msg = sadLibrary.messages();
        SadPlaceholders place = sadLibrary.placeholders();

        if (args.length <= 1) {
            msgConfig.getStringList("help.generic").forEach(str -> sender.sendMessage(msg.viaChat(false, str)));
        } else {
            // ReCaptcha
            if (sender instanceof ConsoleCommandSender) {
                sender.sendMessage(msg.viaChat(true, msgConfig.getString("player.retrive.console")));
                return;
            }

            // GIMME DA EGG
            Player player = Bukkit.getPlayer(getSenderName(sender));

            // Ya im a playa
            String eggId = "egg-item." + args[1];

            // Contains the requested?
            if (!config.contains(eggId)) {
                sender.sendMessage(msg.viaChat(true, msgConfig.getString("player.generic.egg-not-found"))
                        .replace(place.getEggId(), args[2])
                        .replace(place.getPlayerName(), player.getName()));
                return;
            }

            // The inventory is full?
            ItemStack egg = new EasterEgg(
                    config.getString(eggId + ".name"),
                    config.getStringList(eggId + ".lore"),
                    config.getString(eggId + ".head-value")
            ).buildItem();

            if (!isInventoryFull(player)) {
                sender.sendMessage(msg.viaChat(true, msgConfig.getString("player.retrive.successfully")
                        .replace(sadLibrary.placeholders().getPlayerName(), player.getName())));
                player.getInventory().addItem(egg);
            } else {
                sender.sendMessage(msg.viaChat(true, msgConfig.getString("player.generic.dropped")));
                player.getWorld().dropItem(player.getLocation().add(0, 1, 0), egg);
            }
        }
    }

    private boolean isInventoryFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }

    private String getSenderName(CommandSender sender) {
        if (sender instanceof ConsoleCommandSender)
            return "console";
        else
            return sender.getName();
    }
}
