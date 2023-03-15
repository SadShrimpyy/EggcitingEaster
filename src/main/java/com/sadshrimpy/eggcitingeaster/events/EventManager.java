package com.sadshrimpy.eggcitingeaster.events;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.sadshrimpy.eggcitingeaster.utils.sadlibrary.SadMessages;
import org.bukkit.*;
import org.bukkit.block.Skull;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.sadshrimpy.eggcitingeaster.EggcitingEaster.sadLibrary;

public class EventManager implements Listener {

    SadMessages msg;
    FileConfiguration configConf;
    FileConfiguration msgConf;

    public EventManager() {
        msgConf = sadLibrary.configurations().getMessagesConfiguration();
        configConf = sadLibrary.configurations().getConfigConfiguration();
        msg = sadLibrary.messages();
    }

    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.LEFT_CLICK_AIR)) return;
        if (event.hasItem()) {
            // if item in main hand
            hasItem(event);
        } else {
            // if no item
            noItem(event);
        }
    }

    private void hasItem(PlayerInteractEvent event) {
        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            checkTarget(event);
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
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

        if (event.getHand() == EquipmentSlot.HAND) {
            if (sadLibrary.map != null) {
                if (sadLibrary.map.containsKey(skullUuid)) {
                    // Is a valid one
                    player.sendMessage(msg.viaChat(true, msgConf.getString("player.found.valid-egg")
                            .replace(sadLibrary.placeholders().getPlayerName(), player.getName())));
                    Location location = skull.getLocation();

                    // Destroy
                    if (executeActions(player.getName(), skullUuid)) {
                        // Destroy block
                        Objects.requireNonNull(Bukkit.getWorld(skull.getWorld().getUID())).setType(location, Material.AIR);
                    }

                    // Particles
                    configConf.getStringList(pathBuilder(skullUuid) + ".click-event.particles").forEach(str -> {
                        String[] temp = str.split(":");
                        player.spawnParticle(Particle.valueOf(temp[0]), location, Integer.parseInt(temp[1]));
                    });
                } else {
                    // Is not a valid one
                    player.sendMessage(msg.viaChat(true, msgConf.getString("player.found.expired-egg")
                            .replace(sadLibrary.placeholders().getPlayerName(), player.getName())));
                }
            }
        }
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
