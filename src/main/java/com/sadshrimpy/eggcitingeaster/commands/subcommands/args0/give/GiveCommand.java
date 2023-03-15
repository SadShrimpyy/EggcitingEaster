package com.sadshrimpy.eggcitingeaster.commands.subcommands.args0.give;

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

public class GiveCommand implements CommandSyntax {

    FileConfiguration config = sadLibrary.configurations().getConfigConfiguration();
    FileConfiguration msgConfig = sadLibrary.configurations().getMessagesConfiguration();
    SadMessages msg = sadLibrary.messages();
    SadPlaceholders place = sadLibrary.placeholders();

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public String getPermission(String[] args) {
        return sadLibrary.permissions().getGive();
    }

    @Override
    public boolean hasSubcommands() { return true; }

    @Override
    public void perform(CommandSender sender, String[] args) {
        // /eggcitingeaster give <player/*> <eggID>
        if (args.length <= 2) {
            for (String str : msgConfig.getStringList("help.generic"))
                sender.sendMessage(msg.viaChat(false, str));
        } else {
            // Build item
            String eggId = "egg-item." + args[2];

            // Contains the requested?
            if (!config.contains(eggId)) {
                sender.sendMessage(msg.viaChat(true, msgConfig.getString("player.generic.egg-not-found"))
                        .replace(place.getEggId(), args[2])
                        .replace(place.getPlayerName(), getSenderName(sender)));

                return;
            }

            String eggName = msg.translateColors(config.getString(eggId + ".name"));
            ItemStack easterEgg = new EasterEgg(
                    eggName,
                    config.getStringList(eggId + ".lore"),
                    config.getString(eggId + ".head-value")
            ).buildItem();

            // To all
            if (args[1].equals("*")) {
                // /ea give * egg2
                if (sender.hasPermission(sadLibrary.permissions().getGiveAll()))
                    Bukkit.getOnlinePlayers().forEach(player -> sendEgg(sender, easterEgg, eggName, player));
            }
            else {
                // /ea give SadShrimpy egg2
                // Ooooooonly yooouuuuuuuuuuuuuuuuuuuu
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(msg.viaChat(true, msgConfig.getString("player.generic.not-found")
                            .replace(place.getPlayerName(), getSenderName(sender))
                            .replace(place.getPlayerTarget(), args[1])));
                    return;
                }

                if (player.isOnline())
                    sendEgg(sender, easterEgg, eggName, player);
            }
        }
    }

    private void sendEgg(CommandSender sender, ItemStack egg, String eggName, Player player) {
        if (!isInventoryFull(player)) {
            // Msg the TARGET
            player.sendMessage(msg.viaChat(true, msgConfig.getString("player.give.received")
                    .replace(place.getPlayerName(), player.getName())
                    .replace(place.getEggName(), eggName)
                    .replace(place.getPlayerExecutor(), getSenderName(sender))));
            player.getInventory().addItem(egg);
        } else {
            sender.sendMessage(msg.viaChat(true, msgConfig.getString("player.generic.dropped")));
            player.getWorld().dropItem(player.getLocation().add(0, 1, 0), egg);
        }

        // Msg the SENDER
        sender.sendMessage(msg.viaChat(true, msgConfig.getString("player.give.executor")
                .replace(place.getPlayerName(), getSenderName(sender))
                .replace(place.getEggName(), eggName)
                .replace(place.getPlayerTarget(), player.getName())));
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
