package io.github.bedwarsrel.BedwarsRel.Game;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Events.BWGameEndEvent;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Statistics.PlayerStatistic;
import io.github.bedwarsrel.BedwarsRel.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SingleGameCycle extends GameCycle {

    public SingleGameCycle(Game game) {
        super(game);
    }

    @Override
    public void onGameStart() {
        // Reset on game end
    }

    @Override
    public void onGameEnd() {
        // Reset scoreboard first
        this.getGame().resetScoreboard();

        // First team players, they get a reserved slot in lobby
        for (Player p : this.getGame().getTeamPlayers()) {
            this.kickPlayer(p, false);
        }

        // and now the spectators
        List<Player> freePlayers = new ArrayList<Player>(this.getGame().getFree());
        for (Player p : freePlayers) {
            this.kickPlayer(p, true);
        }

        // reset countdown prevention breaks
        this.setEndGameRunning(false);

        // Reset team chests
        for (Team team : this.getGame().getTeams().values()) {
            team.setInventory(null);
            team.getChests().clear();
        }

        // clear protections
        this.getGame().clearProtections();

        // reset region
        this.getGame().resetRegion();

        // Restart lobby directly?
        if (this.getGame().isStartable() && this.getGame().getLobbyCountdown() == null) {
            GameLobbyCountdown lobbyCountdown = new GameLobbyCountdown(this.getGame());
            lobbyCountdown.runTaskTimer(Main.getInstance(), 20L, 20L);
            this.getGame().setLobbyCountdown(lobbyCountdown);
        }

        // set state and with that, the sign
        this.getGame().setState(GameState.WAITING);
        this.getGame().updateScoreboard();
    }

    private void kickPlayer(Player player, boolean wasSpectator) {
        for (Player freePlayer : this.getGame().getFree()) {
            player.showPlayer(freePlayer);
        }

        if (wasSpectator && this.getGame().isFull()) {
            this.getGame().playerLeave(player, false);
            return;
        }

        if (Main.getInstance().toMainLobby()) {
            if (Main.getInstance().allPlayersBackToMainLobby()) {
                this.getGame().playerLeave(player, false);
                return;
            } else {
                player.teleport(this.getGame().getLobby());
            }
        } else {
            player.teleport(this.getGame().getLobby());
        }

        if (Main.getInstance().isHologramsEnabled()
                && Main.getInstance().getHolographicInteractor() != null
                && this.getGame().getLobby() == player.getWorld()) {
            Main.getInstance().getHolographicInteractor().updateHolograms(player);
        }

        if (Main.getInstance().statisticsEnabled()) {
            PlayerStatistic statistic =
                    Main.getInstance().getPlayerStatisticManager().getStatistic(player);
            statistic.setScore(statistic.getScore() + statistic.getCurrentScore());
            statistic.setCurrentScore(0);
            statistic.store();

            if (Main.getInstance().getBooleanConfig("statistics.show-on-game-end", true)) {
                Main.getInstance().getServer().dispatchCommand(player, "bw stats");
            }
        }

        this.getGame().setPlayerDamager(player, null);

        PlayerStorage storage = this.getGame().getPlayerStorage(player);
        storage.clean();
        storage.loadLobbyInventory(this.getGame());
    }

    @Override
    public void onPlayerLeave(Player player) {
        // teleport to join location
        PlayerStorage storage = this.getGame().getPlayerStorage(player);

        if (Main.getInstance().toMainLobby()) {
            if (Main.getInstance().isHologramsEnabled()
                    && Main.getInstance().getHolographicInteractor() != null
                    && this.getGame().getMainLobby().getWorld() == player.getWorld()) {
                Main.getInstance().getHolographicInteractor().updateHolograms(player);
            }

            player.teleport(this.getGame().getMainLobby());
        } else {
            if (Main.getInstance().isHologramsEnabled()
                    && Main.getInstance().getHolographicInteractor() != null
                    && storage.getLeft() == player.getWorld()) {
                Main.getInstance().getHolographicInteractor().updateHolograms(player);
            }

            player.teleport(storage.getLeft());
        }

        if (this.getGame().getState() == GameState.RUNNING && !this.getGame().isStopping()
                && !this.getGame().spectator(player)) {
            this.checkGameOver();
        }
    }

    @Override
    public void onGameLoaded() {
        // Reset on game end
    }

    @Override
    public boolean onPlayerJoins(Player player) {
        if (this.getGame().isFull() && !player.hasPermission("bw.vip.joinfull")) {
            if (this.getGame().getState() != GameState.RUNNING
                    || !Main.getInstance().spectationEnabled()) {
                player.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("lobby.gamefull")));
                return false;
            }
        } else if (this.getGame().isFull() && player.hasPermission("bw.vip.joinfull")) {
            if (this.getGame().getState() == GameState.WAITING) {
                List<Player> players = this.getGame().getNonVipPlayers();

                if (players.size() == 0) {
                    player.sendMessage(
                            ChatHelper.with(ChatColor.RED + Main.local("lobby.gamefullpremium")));
                    return false;
                }

                Player kickPlayer = null;
                if (players.size() == 1) {
                    kickPlayer = players.get(0);
                } else {
                    kickPlayer = players.get(Utils.randInt(0, players.size() - 1));
                }

                kickPlayer
                        .sendMessage(ChatHelper.with(ChatColor.RED + Main.local("lobby.kickedbyvip")));
                this.getGame().playerLeave(kickPlayer, false);
            } else {
                if (this.getGame().getState() == GameState.RUNNING
                        && !Main.getInstance().spectationEnabled()) {
                    player.sendMessage(
                            ChatHelper.with(ChatColor.RED + Main.local("errors.cantjoingame")));
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onGameOver(GameOverTask task) {
        if (task.getCounter() == task.getStartCount() && task.getWinner() != null) {
            this.getGame().broadcast(ChatColor.GOLD + Main.local("ingame.teamwon",
                    ImmutableMap.of("team", task.getWinner().getDisplayName() + ChatColor.GOLD)));
            this.getGame().stopWorkers();
        } else if (task.getCounter() == task.getStartCount() && task.getWinner() == null) {
            this.getGame().broadcast(ChatColor.GOLD + Main.local("ingame.draw"));
        }

        if (task.getCounter() == 0) {
            BWGameEndEvent endEvent = new BWGameEndEvent(this.getGame());
            Main.getInstance().getServer().getPluginManager().callEvent(endEvent);

            this.onGameEnd();
            task.cancel();
        } else {
            this.getGame().broadcast(ChatColor.AQUA + Main.local("ingame.backtolobby", ImmutableMap.of("sec",
                    ChatColor.YELLOW.toString() + task.getCounter() + ChatColor.AQUA)));
        }

        task.countdown();
    }

}
