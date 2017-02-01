package io.github.bedwarsrel.BedwarsRel;

import org.bukkit.ChatColor;

public class ChatHelper {

    public static String with(String i) {
        return Main.getUTF8("chat-prefix", ChatColor.GRAY + "[" + ChatColor.AQUA + "BedWars" + ChatColor.GRAY + "]")
                + " " + ChatColor.WHITE + i;
    }

}
