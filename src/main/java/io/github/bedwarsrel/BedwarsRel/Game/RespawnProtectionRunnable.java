package io.github.bedwarsrel.BedwarsRel.Game;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnProtectionRunnable extends BukkitRunnable {

    private Game game = null;
    private Player player = null;
    private int length = 0;

    public RespawnProtectionRunnable(Game game, Player player, int seconds) {
        this.game = game;
        this.player = player;
        this.length = seconds;
    }

    @Override
    public void run() {
        if (this.length > 0) {
            this.player.sendMessage(ChatHelper.with(Main.local("ingame.protectionleft",
                    ImmutableMap.of("length", String.valueOf(this.length)))));
        }

        if (this.length <= 0) {
            this.player.sendMessage(ChatHelper.with(Main.local("ingame.protectionend")));
            this.game.removeProtection(this.player);
        }

        this.length--;
    }

    public void runProtection() {
        this.runTaskTimerAsynchronously(Main.getInstance(), 5L, 20L);
    }

}
