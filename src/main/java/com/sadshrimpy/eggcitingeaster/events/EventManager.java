package com.sadshrimpy.eggcitingeaster.events;

import com.sadshrimpy.eggcitingeaster.utils.sadlibrary.SadMessages;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.sadshrimpy.eggcitingeaster.EggcitingEaster.sadLibrary;

public class EventManager implements Listener {

    protected final SadMessages msg;
    protected final FileConfiguration configConf;
    protected final FileConfiguration msgConf;

    public EventManager() {
        msgConf = sadLibrary.configurations().getMessagesConfiguration();
        configConf = sadLibrary.configurations().getConfigConfiguration();
        msg = sadLibrary.messages();
    }

    @EventHandler
    public void onClickBlock(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null)
            new EggInteract(event).actions();
        else if (event.getClickedBlock().getState().getType().equals(Material.PLAYER_HEAD))
            new EggInteract(event).actions();
    }
}
