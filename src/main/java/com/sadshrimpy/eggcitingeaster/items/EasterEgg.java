package com.sadshrimpy.eggcitingeaster.items;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.sadshrimpy.eggcitingeaster.utils.sadlibrary.SadMessages;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.units.qual.A;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.sadshrimpy.eggcitingeaster.EggcitingEaster.sadLibrary;

public class EasterEgg {

    // YAYA
    ItemStack easterEgg;
    SkullMeta meta;
    String name;
    List<String> lore = new ArrayList<>();

    public EasterEgg(String displayName, List<String> lore, String headValue) {
        SadMessages msg = sadLibrary.messages();
        this.name = displayName;
        this.easterEgg = new ItemStack(Material.PLAYER_HEAD);
        // META RELATED
            this.meta = (SkullMeta) this.easterEgg.getItemMeta();
            assert this.meta != null;
            this.meta.setDisplayName(msg.translateColors(displayName));
            // Lore
            lore.forEach(str -> { this.lore.add(msg.translateColors(str)); } );
            this.meta.setLore(this.lore);
            setHeadValue(headValue);
    }

    private void setHeadValue(String headValue) {
        // Profiler
        UUID uuid = UUID.randomUUID();
        sadLibrary.map.put(uuid, headValue);
        GameProfile profile = new GameProfile(uuid, null);
        profile.getProperties().put("textures", new Property("textures", headValue));

        try {
            // Field
            Field field = this.meta.getClass().getDeclaredField("profile");
            field.setAccessible(true);
            field.set(this.meta, profile);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        this.easterEgg.setItemMeta(this.meta);
    }

    public ItemStack buildItem() {
        return easterEgg;
    }

}
