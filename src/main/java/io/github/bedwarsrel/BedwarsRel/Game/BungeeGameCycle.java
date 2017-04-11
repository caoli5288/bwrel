package io.github.bedwarsrel.BedwarsRel.Game;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Events.BWGameEndEvent;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;

public class BungeeGameCycle extends GameCycle {

    @Getter
    private static int count;

    public BungeeGameCycle(Game game) {
        super(game);
    }

    @Override
    public void onGameStart() {
        count++;
    }

    private void kickAll() {
        for (Player player : this.getGame().getTeamPlayers()) {
            for (Player freePlayer : this.getGame().getFree()) {
                player.showPlayer(freePlayer);
            }
            this.getGame().playerLeave(player, false);
        }

        for (Player freePlayer : this.getGame().getFreePlayersClone()) {
            this.getGame().playerLeave(freePlayer, false);
        }
    }

    @Override
    public void onGameEnd() {
        if (count < Main.getInstance().getIntConfig("bungeecord.full-restart", 1)) {
            if (Main.getBool("reset-by-reload", false)) {
                GameManager.reload();
            } else {
                getGame().resetScoreboard();
                kickAll();
                setEndGameRunning(false);
                for (Team team : getGame().getTeams().values()) {
                    team.reset();
                }

                getGame().clearProtections();

                getGame().setState(GameState.WAITING);
                getGame().updateScoreboard();

                getGame().resetRegion();
            }
        } else {
            kickAll();
            if (!Main.getBool("reset-by-reload", false)) {
                getGame().resetRegion();
            }
            Main.run(65, Bukkit::shutdown);
        }
    }

    @Override
    public void onPlayerLeave(Player player) {
        if (player.isOnline() || player.isDead()) {
            this.bungeeSendToServer(Main.getInstance().getBungeeHub(), player, true);
        }

        if (this.getGame().getState() == GameState.RUNNING && !this.getGame().isStopping()) {
            this.checkGameOver();
        }
    }

    @Override
    public void onGameLoaded() {
        // Reset on game end
    }

    @Override
    public boolean onPlayerJoins(Player player) {
        final Player p = player;

        if (this.getGame().isFull() && !player.hasPermission("bw.vip.joinfull")) {
            if (this.getGame().getState() != GameState.RUNNING
                    || !Main.getInstance().spectationEnabled()) {
                this.bungeeSendToServer(Main.getInstance().getBungeeHub(), p, false);
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        BungeeGameCycle.this.sendBungeeMessage(p,
                                ChatHelper.with(ChatColor.RED + Main.local("lobby.gamefull")));
                    }
                }.runTaskLater(Main.getInstance(), 60L);

                return false;
            }
        } else if (this.getGame().isFull() && player.hasPermission("bw.vip.joinfull")) {
            if (this.getGame().getState() == GameState.WAITING) {
                List<Player> players = this.getGame().getNonVipPlayers();

                if (players.size() == 0) {
                    this.bungeeSendToServer(Main.getInstance().getBungeeHub(), p, false);
                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            BungeeGameCycle.this.sendBungeeMessage(p,
                                    ChatHelper.with(ChatColor.RED + Main.local("lobby.gamefullpremium")));
                        }
                    }.runTaskLater(Main.getInstance(), 60L);
                    return false;
                }

                Player kickPlayer = null;
                if (players.size() == 1) {
                    kickPlayer = players.get(0);
                } else {
                    kickPlayer = players.get(Utils.randInt(0, players.size() - 1));
                }

                final Player kickedPlayer = kickPlayer;

                this.getGame().playerLeave(kickedPlayer, false);
                new BukkitRunnable() {

                    @Override
                    public void run() {
                        BungeeGameCycle.this.sendBungeeMessage(kickedPlayer,
                                ChatHelper.with(ChatColor.RED + Main.local("lobby.kickedbyvip")));
                    }
                }.runTaskLater(Main.getInstance(), 60L);
            } else {
                if (this.getGame().getState() == GameState.RUNNING
                        && !Main.getInstance().spectationEnabled()) {

                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            BungeeGameCycle.this.bungeeSendToServer(Main.getInstance().getBungeeHub(), p, false);
                        }

                    }.runTaskLater(Main.getInstance(), 5L);

                    new BukkitRunnable() {

                        @Override
                        public void run() {
                            BungeeGameCycle.this.sendBungeeMessage(p,
                                    ChatHelper.with(ChatColor.RED + Main.local("lobby.gamefull")));
                        }
                    }.runTaskLater(Main.getInstance(), 60L);
                    return false;
                }
            }
        }

        return true;
    }

    public void sendBungeeMessage(Player player, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Message");
        out.writeUTF(player.getName());
        out.writeUTF(message);

        player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
    }

    public void bungeeSendToServer(final String server, final Player player, boolean preventDelay) {
        if (server == null) {
            player.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.bungeenoserver")));
            return;
        }

        new BukkitRunnable() {

            @Override
            public void run() {
                ByteArrayOutputStream b = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(b);

                try {
                    out.writeUTF("Connect");
                    out.writeUTF(server);
                } catch (Exception e) {
                    Main.getInstance().getBugsnag().notify(e);
                    e.printStackTrace();
                    return;
                }

                if (b != null) {
                    player.sendPluginMessage(Main.getInstance(), "BungeeCord", b.toByteArray());
                }
            }
        }.runTaskLater(Main.getInstance(), (preventDelay) ? 0L : 20L);
    }

    @Override
    public void onGameOver(GameOverTask task) {
        int count = task.getCounter();
        if (count == task.getStartCount() && task.getWinner() != null) {
            this.getGame().broadcast(ChatColor.GOLD + Main.local("ingame.teamwon",
                    ImmutableMap.of("team", task.getWinner().getDisplayName() + ChatColor.GOLD)));
        } else if (count == task.getStartCount() && task.getWinner() == null) {
            this.getGame().broadcast(ChatColor.GOLD + Main.local("ingame.draw"));
        }

        if (count == 2) {
            String label = Main.getUTF8("player-command-on-end", "");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.performCommand(label.replace("%player%", p.getName()));
            }
        }

        // game over
        if (count == 0) {
            BWGameEndEvent endEvent = new BWGameEndEvent(getGame());
            Main.getInstance().getServer().getPluginManager().callEvent(endEvent);

            onGameEnd();
            task.cancel();
        } else if ((count == task.getStartCount()) || (count % 10 == 0)
                || (count <= 5 && (count > 0))) {
            this.getGame().broadcast(ChatColor.AQUA + Main.local("ingame.serverrestart", ImmutableMap
                    .of("sec", ChatColor.YELLOW.toString() + count + ChatColor.AQUA)));
        }

        task.countdown();
    }

}
