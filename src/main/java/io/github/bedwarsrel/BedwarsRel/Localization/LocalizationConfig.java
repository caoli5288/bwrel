package io.github.bedwarsrel.BedwarsRel.Localization;

import io.github.bedwarsrel.BedwarsRel.$;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Map;

public class LocalizationConfig extends YamlConfiguration {

    static Field local;

    @SuppressWarnings("unchecked")
    public String getPlayerLocale(Player player) {
        try {
            Object p = $.getHandle(player);
            if ($.nil(local)) {
                local = p.getClass().getDeclaredField("locale");
                local.setAccessible(true);
            }
            return local.get(p).toString().split("_")[0].toLowerCase();
        } catch (Exception ex) {
            return Main.getInstance().getFallbackLocale();
        }
    }

    public void loadLocale(String locKey, boolean isFallback) {
        File locFile =
                new File(Main.getInstance().getDataFolder().getPath() + "/locale/" + locKey + ".yml");
        BufferedReader reader = null;
        InputStream inputStream = null;
        if (locFile.exists()) {
            try {
                inputStream = new FileInputStream(locFile);
            } catch (FileNotFoundException e) {
                // NO ERROR
            }
            Main.getInstance().getServer().getConsoleSender().sendMessage(ChatHelper
                    .with(ChatColor.GOLD + "Using your custom locale \"" + locKey + "\"."));
        } else {
            if (inputStream == null) {
                inputStream = Main.getInstance().getResource("locale/" + locKey + ".yml");
            }
            if (inputStream == null) {
                Main.getInstance().getServer().getConsoleSender()
                        .sendMessage(ChatHelper.with(ChatColor.GOLD + "The locale \"" + locKey
                                + "\" defined in your config is not available. Using fallback locale: "
                                + Main.getInstance().getFallbackLocale()));
                inputStream = Main.getInstance()
                        .getResource("locale/" + Main.getInstance().getFallbackLocale() + ".yml");
            }
        }
        try {
            reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            this.load(reader);
        } catch (Exception e) {
            Main.getInstance().getServer().getConsoleSender().sendMessage(
                    ChatHelper.with(ChatColor.RED + "Failed to load localization language!"));
            return;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public Object get(String path) {
        return this.getString(path);
    }

    public Object get(String path, Map<String, String> params) {
        return getFormatString(path, params);
    }

    @Override
    public String getString(String path) {
        if (super.get(path) == null) {
            return "LOCALE_NOT_FOUND";
        }

        return ChatColor.translateAlternateColorCodes('&', super.getString(path));
    }

    public String getFormatString(String path, Map<String, String> params) {
        String str = getString(path);
        for (String key : params.keySet()) {
            str = str.replace("$" + key.toLowerCase() + "$", params.get(key));
        }

        return ChatColor.translateAlternateColorCodes('&', str);
    }

}
