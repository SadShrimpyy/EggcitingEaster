package com.sadshrimpy.eggcitingeaster.events;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.sadshrimpy.eggcitingeaster.utils.sadlibrary.SadMessages;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Skull;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.sadshrimpy.eggcitingeaster.EggcitingEaster.sadLibrary;

public class EggInteract extends EventManager{

    private final Action action;
    private final SadMessages msg;
    private final FileConfiguration configConf;
    private final FileConfiguration msgConf;
    private final PlayerInteractEvent event;

    public EggInteract(PlayerInteractEvent event) {
        this.msgConf = super.msgConf;
        this.configConf = super.configConf;
        this.msg = super.msg;
        this.event = event;
        this.action = event.getAction();
    }

    public void actions() {
        // Controls because this event is strange and fire when he wants to
        if (action.equals(Action.LEFT_CLICK_AIR)) return;
        if (action.equals(Action.RIGHT_CLICK_AIR)) return;
        if (action.equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock() == null) return;

        if (event.hasItem()) {
            // if item in main hand
            hasItem(event);
        } else {
            // if no item
            noItem(event);
        }
    }

    private void hasItem(PlayerInteractEvent event) {
        if (action == Action.LEFT_CLICK_BLOCK) {
            checkTarget(event);
        } else if (action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack itemHand = event.getItem(); // Item in the hand

            // If is NOT a skull
            if (event.getItem().getType() != Material.PLAYER_HEAD) return;

            // Egg placed
            if (getValue(itemHand).equals("piazz"))
                event.getPlayer().sendMessage(msg.viaChat(true, msgConf.getString("player.placed.egg")));
        }
    }

    private void checkTarget(PlayerInteractEvent event) {
        // Check the target block
        if (!event.getClickedBlock().getState().getType().equals(Material.PLAYER_HEAD)) return;

        // Check the ID of the SKULL
        Skull skull = (Skull) event.getClickedBlock().getState();
        UUID skullUuid = Objects.requireNonNull(skull.getOwningPlayer()).getUniqueId();
        Player player = Bukkit.getPlayer(event.getPlayer().getName());

        if (event.getHand() != EquipmentSlot.HAND) return;
        if (sadLibrary.map == null) return;

        String perm = getPermission(skullUuid);

        if (!perm.equals("no-needed"))
            if (!player.hasPermission(perm)) {
                player.sendMessage(sadLibrary.messages().viaChat(true, sadLibrary.configurations().getMessagesConfiguration().getString("player.generic.no-permission")
                        .replace(sadLibrary.placeholders().getPlayerName(), player.getName())
                        .replace(sadLibrary.placeholders().getPermission(), perm)));
                return;
            }

        if (sadLibrary.map.containsKey(skullUuid)) {
            // Is a valid one
            validEgg(skull, skullUuid, player);
        } else {
            // Is not a valid one
            player.sendMessage(msg.viaChat(true, msgConf.getString("player.found.expired-egg")
                    .replace(sadLibrary.placeholders().getPlayerName(), player.getName())));
        }
    }

    private void validEgg(Skull skull, UUID skullUuid, Player player) {
        player.sendMessage(msg.viaChat(true, msgConf.getString("player.found.valid-egg")
                .replace(sadLibrary.placeholders().getPlayerName(), player.getName())));
        Location location = skull.getLocation();

        // Destroy
        if (executeActions(player.getName(), skullUuid))
            Objects.requireNonNull(Bukkit.getWorld(skull.getWorld().getUID())).setType(location, Material.AIR);

        // Particles
        configConf.getStringList(pathBuilder(skullUuid) + ".click-event.particles").forEach(str -> {
            String[] temp = str.split(":");
            player.spawnParticle(Particle.valueOf(temp[0]), location, Integer.parseInt(temp[1]));
        });
    }

    private String getPermission(UUID skullUuid) {
        for (String str : getKeys())
            if (configConf.get("egg-item." + str + ".head-value").equals(sadLibrary.map.get(skullUuid)))
                return configConf.getString("egg-item." + str + ".permission-to-retrive");
        return "no-needed";
    }

    private String pathBuilder(UUID skullUuid) {
        for (String str : getKeys())
            if (configConf.get("egg-item." + str + ".head-value").equals(sadLibrary.map.get(skullUuid)))
                return "egg-item." + str;
        return null;
    }

    private void noItem(PlayerInteractEvent event) {
        // Check the target block
        checkTarget(event);
    }

    private boolean executeActions(String clickerName, UUID skullUuid) {
        ConsoleCommandSender console = Bukkit.getServer().getConsoleSender();
        String path = pathBuilder(skullUuid);
        configConf.getStringList(path + ".commands").forEach(str -> Bukkit.dispatchCommand(console, str.replace(sadLibrary.placeholders().getClicker(), clickerName)));
        return configConf.getBoolean(path + ".click-event.destroy-egg");
    }

    private List<String> getKeys() {
        List<String> objs = new ArrayList<>();
        configConf.getConfigurationSection("egg-item").getKeys(false).forEach(str -> objs.add(str));
        return objs;
    }

    private String getValue(ItemStack item) {
        SkullMeta meta = (SkullMeta) item.getItemMeta();

        AtomicReference<String> value = null;
        try {
            Field profile = meta.getClass().getDeclaredField("profile");
            profile.setAccessible(true);
            GameProfile gmProf = (GameProfile) profile.get(meta);
            Collection<Property> proprieties = gmProf.getProperties().get("textures");
            if (value == null)
                return "piazz";
            proprieties.forEach(peepe -> { value.set(peepe.getValue()); } );
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return value.toString();
    }
}
