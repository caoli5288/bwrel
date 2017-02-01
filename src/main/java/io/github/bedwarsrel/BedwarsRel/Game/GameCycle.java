package io.github.bedwarsrel.BedwarsRel.Game;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Events.BedwarsGameOverEvent;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Shop.Specials.RescuePlatform;
import io.github.bedwarsrel.BedwarsRel.Shop.Specials.SpecialItem;
import io.github.bedwarsrel.BedwarsRel.Statistics.PlayerStatistic;
import io.github.bedwarsrel.BedwarsRel.Timing;
import io.github.bedwarsrel.BedwarsRel.Utils;
import lombok.Setter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public abstract class GameCycle {

    private Game game;

    @Setter
    private boolean endGameRunning;

    GameCycle(Game game) {
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public abstract void onGameStart();

    public abstract void onGameEnds();

    public abstract void onPlayerLeave(Player player);

    public abstract void onGameLoaded();

    public abstract boolean onPlayerJoins(Player player);

    public abstract void onGameOver(GameOverTask task);

    private boolean storeRecords(boolean storeHolders, Team winner) {
        int playTime = this.getGame().getLength() - this.getGame().getTimeLeft();
        boolean throughBed = false;

        if (playTime <= this.getGame().getRecord()) {

            // check for winning through bed destroy
            for (Team team : this.getGame().getPlayingTeams()) {
                if (team.isDead(this.getGame())) {
                    throughBed = true;
                    break;
                }
            }

            if (!throughBed) {
                this.getGame().broadcast(Main.local("ingame.record-nobeddestroy"));
                return false;
            }

            if (storeHolders) {
                if (playTime < this.getGame().getRecord()) {
                    this.getGame().getRecordHolders().clear();
                }

                for (Player player : winner.getPlayers()) {
                    this.getGame().addRecordHolder(player.getName());
                }
            }

            this.getGame().setRecord(playTime);
            this.getGame().saveRecord();

            this.getGame()
                    .broadcast(Main.local("ingame.newrecord",
                            ImmutableMap.of("record", this.getGame().getFormattedRecord(), "team",
                                    winner.getChatColor() + winner.getDisplayName())));
            return true;
        }

        return false;
    }

    private String winTitleReplace(String str, Team winner) {
        int playTime = this.getGame().getLength() - this.getGame().getTimeLeft();
        String finalStr = str;
        String formattedTime = Utils.getFormattedTime(playTime);

        finalStr = finalStr.replace("$time$", formattedTime);

        if (winner == null) {
            return finalStr;
        }

        finalStr = finalStr.replace("$team$", winner.getChatColor() + winner.getDisplayName());
        return finalStr;
    }

    @SuppressWarnings("unchecked")
    private void runGameOver(Team winner) {
        BedwarsGameOverEvent overEvent = new BedwarsGameOverEvent(this.getGame(), winner);
        Main.getInstance().getServer().getPluginManager().callEvent(overEvent);

        if (overEvent.isCancelled()) {
            return;
        }

        this.getGame().stopWorkers();
        this.setEndGameRunning(true);

        // new record?
        boolean storeRecords = Main.getInstance().getBooleanConfig("store-game-records", true);
        boolean storeHolders = Main.getInstance().getBooleanConfig("store-game-records-holder", true);
        boolean madeRecord = false;
        if (storeRecords && winner != null) {
            madeRecord = this.storeRecords(storeHolders, winner);
        }

        int delay = Main.getInstance().getConfig().getInt("gameoverdelay"); // configurable
        // delay
        String title = this.winTitleReplace(Main.local("ingame.title.win-title"), winner);
        String subtitle = this.winTitleReplace(Main.local("ingame.title.win-subtitle"), winner);

        if (Main.getInstance().statisticsEnabled()
                || Main.getInstance().getBooleanConfig("rewards.enabled", false)
                || (Main.getInstance().getBooleanConfig("titles.win.enabled", true)
                && (!"".equals(title) || !"".equals(subtitle)))) {
            if (winner != null) {
                for (Player player : winner.getEnsure()) {
                    if (Main.getInstance().getBooleanConfig("titles.win.enabled", true)
                            && (!"".equals(title) || !"".equals(subtitle))) {
                        try {
                            Class<?> clazz = Class.forName("io.github.bedwarsrel.BedwarsRel.Com."
                                    + Main.getInstance().getCurrentVersion() + ".Title");

                            if (!"".equals(title)) {
                                double titleFadeIn =
                                        Main.getInstance().getConfig().getDouble("titles.win.title-fade-in", 1.5);
                                double titleStay =
                                        Main.getInstance().getConfig().getDouble("titles.win.title-stay", 5.0);
                                double titleFadeOut =
                                        Main.getInstance().getConfig().getDouble("titles.win.title-fade-out", 2.0);
                                Method showTitle = clazz.getDeclaredMethod("showTitle", Player.class, String.class,
                                        double.class, double.class, double.class);

                                showTitle.invoke(null, player, title, titleFadeIn, titleStay, titleFadeOut);
                            }

                            if (!"".equals(subtitle)) {
                                double subTitleFadeIn =
                                        Main.getInstance().getConfig().getDouble("titles.win.subtitle-fade-in", 1.5);
                                double subTitleStay =
                                        Main.getInstance().getConfig().getDouble("titles.win.subtitle-stay", 5.0);
                                double subTitleFadeOut =
                                        Main.getInstance().getConfig().getDouble("titles.win.subtitle-fade-out", 2.0);
                                Method showSubTitle = clazz.getDeclaredMethod("showSubTitle", Player.class,
                                        String.class, double.class, double.class, double.class);

                                showSubTitle.invoke(null, player, subtitle, subTitleFadeIn, subTitleStay,
                                        subTitleFadeOut);
                            }
                        } catch (Exception ex) {
                            Main.getInstance().getBugsnag().notify(ex);
                            ex.printStackTrace();
                        }
                    }

                    if (Main.getInstance().getBooleanConfig("rewards.enabled", false)) {
                        List<String> list;
                        list = Main.getInstance().getConfig().getStringList("rewards.player-win");
                        Main.getInstance().dispatchRewardCommands(list, this.getRewardPlaceholders(player));
                    }

                    if (Main.getInstance().statisticsEnabled()) {
                        PlayerStatistic statistic =
                                Main.getInstance().getPlayerStatisticManager().getStatistic(player);
                        statistic.setWins(statistic.getWins() + 1);
                        statistic.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.win", 50));

                        if (madeRecord) {
                            statistic.addCurrentScore(
                                    Main.getInstance().getIntConfig("statistics.scores.record", 100));
                        }
                    }
                }
            }

            if (Main.getInstance().getBooleanConfig("rewards.enabled", false)) {
                List<String> list;
                list = Main.getInstance().getConfig().getStringList("rewards.player-end-game");
                for (Team t : game.getTeams().values()) {
                    for (Player player : t.getEnsure()) {
                        Main.getInstance().dispatchRewardCommands(list, this.getRewardPlaceholders(player));
                    }
                }
            }

        }

        getGame().getPlayingTeams().clear();

        GameOverTask gameOver = new GameOverTask(this, delay, winner);
        gameOver.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    private Map<String, String> getRewardPlaceholders(Player player) {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("{player}", player.getName());
        if (Main.getInstance().statisticsEnabled()) {
            PlayerStatistic statistic =
                    Main.getInstance().getPlayerStatisticManager().getStatistic(player);
            placeholders.put("{score}", String.valueOf(statistic.getCurrentScore()));
        }

        return placeholders;
    }

    public void checkGameOver() {
        if (!Main.getInstance().isEnabled()) {
            return;
        }

        Timing timing = Timing.timing("check-game-over", () -> {
            if (!isEndGameRunning()) {
                Team winner = this.getGame().isOver();
                if (winner != null) {
                    this.runGameOver(winner);
                } else if ((getGame().getTeamPlayers().isEmpty() || this.getGame().isOverSet())) {
                    this.runGameOver(null);
                }
            }
        });
        Main.log(timing);
    }

    public void handle(PlayerRespawnEvent event, Player player) {
        Team team = getGame().getPlayerTeam(player);

        getGame().setPlayerDamager(player, null);

        if (getGame().spectator(player)) {
            Collection<Team> r = getGame().getTeams().values();
            event.setRespawnLocation(
                    ((Team) r.toArray()[Utils.randInt(0, r.size() - 1)]).getSpawnLocation());
            return;
        }

        if (team.isDead(getGame())) {
            PlayerStorage storage = getGame().getPlayerStorage(player);

            if (Main.getInstance().statisticsEnabled()) {
                PlayerStatistic statistic =
                        Main.getInstance().getPlayerStatisticManager().getStatistic(player);
                statistic.setLoses(statistic.getLoses() + 1);
            }

            if (Main.getInstance().spectationEnabled()) {
                if (storage != null && storage.getLeft() != null) {
                    event.setRespawnLocation(team.getSpawnLocation());
                }

                getGame().toSpectator(player);
            } else {
                if (game.getCycle() instanceof BungeeGameCycle) {
                    getGame().playerLeave(player, false);
                    return;
                }

                if (!Main.getInstance().toMainLobby()) {
                    if (storage != null) {
                        if (storage.getLeft() != null) {
                            event.setRespawnLocation(storage.getLeft());
                        }
                    }
                } else {
                    if (getGame().getMainLobby() != null) {
                        event.setRespawnLocation(this.getGame().getMainLobby());
                    } else {
                        if (storage != null) {
                            if (storage.getLeft() != null) {
                                event.setRespawnLocation(storage.getLeft());
                            }
                        }
                    }
                }

                getGame().playerLeave(player, false);
            }

        } else {
            if (Main.getInstance().getRespawnProtectionTime() > 0) {
                RespawnProtectionRunnable r = getGame().addProtection(player);
                r.runProtection();
            }
            event.setRespawnLocation(team.getSpawnLocation());
        }

        Main.run(this::checkGameOver, 20);
    }

    public void onPlayerDies(Player player, Player killer) {
        if (this.isEndGameRunning()) {
            return;
        }

        Iterator<SpecialItem> itr = this.game.getSpecialItems().iterator();
        while (itr.hasNext()) {
            SpecialItem item = itr.next();
            if (item instanceof RescuePlatform) {
                RescuePlatform r = (RescuePlatform) item;
                if (r.getOwner().equals(player)) {
                    itr.remove();
                }
            }
        }

        Team deathTeam = this.getGame().getPlayerTeam(player);
        if (Main.getInstance().statisticsEnabled()) {
            Main.log(Timing.timing("statistic", () -> {
                PlayerStatistic die;
                PlayerStatistic kil;

                die = Main.getInstance().getPlayerStatisticManager().getStatistic(player);

                boolean onlyOnBedDestroy =
                        Main.getInstance().getBooleanConfig("statistics.bed-destroyed-kills", false);
                boolean teamIsDead = deathTeam.isDead(this.getGame());

                if (!onlyOnBedDestroy || teamIsDead) {
                    die.setDeaths(die.getDeaths() + 1);
                    die.addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.die", 0));
                }

                if (killer != null) {
                    if (!onlyOnBedDestroy || teamIsDead) {
                        kil = Main.getInstance().getPlayerStatisticManager().getStatistic(killer);
                        if (kil != null) {
                            kil.setKills(kil.getKills() + 1);
                            kil
                                    .addCurrentScore(Main.getInstance().getIntConfig("statistics.scores.kill", 10));
                        }
                    }
                }

                // dispatch reward commands directly
                if (Main.getInstance().getBooleanConfig("rewards.enabled", false) && killer != null
                        && (!onlyOnBedDestroy || teamIsDead)) {
                    List<String> commands = Main.getInstance().getConfig().getStringList("rewards.player-kill");
                    Main.getInstance().dispatchRewardCommands(commands,
                            ImmutableMap.of("{player}", killer.getName(), "{score}",
                                    String.valueOf(Main.getInstance().getIntConfig("statistics.scores.kill", 10))));
                }
            }));
        }

        if (killer == null) {
            this.getGame().broadcast(ChatColor.GOLD + Main.local("ingame.player.died", ImmutableMap
                    .of("player", Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));

            this.sendTeamDeadMessage(deathTeam);
            this.checkGameOver();
            return;
        }

        Team killerTeam = this.getGame().getPlayerTeam(killer);
        if (killerTeam == null) {
            this.getGame().broadcast(ChatColor.GOLD + Main.local("ingame.player.died", ImmutableMap
                    .of("player", Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));
            this.sendTeamDeadMessage(deathTeam);
            this.checkGameOver();
            return;
        }

        StringBuilder heal = new StringBuilder();
        double health = killer.getHealth() / killer.getMaxHealth() * killer.getHealthScale() / 2;
        DecimalFormat format = new DecimalFormat("#.#");


        if (Main.getInstance().getBooleanConfig("hearts-on-death", true)) {
            heal.append("[").append(ChatColor.RED.toString()).append("\u2764");
            heal.append(format.format(health)).append(ChatColor.GOLD).append("]");
        }

        getGame().broadcast(ChatColor.GOLD + Main.local("ingame.player.killed",
                ImmutableMap.of("killer", Game.getPlayerWithTeamString(killer, killerTeam, ChatColor.GOLD, heal.toString()),
                        "player", Game.getPlayerWithTeamString(player, deathTeam, ChatColor.GOLD))));
        sendTeamDeadMessage(deathTeam);

        checkGameOver();
    }

    private void sendTeamDeadMessage(Team deathTeam) {
        if (deathTeam.getPlayers().size() == 1 && deathTeam.isDead(this.getGame())) {
            getGame().broadcast(ChatColor.RED + Main.local("ingame.team-dead", ImmutableMap.of("team",
                    deathTeam.getChatColor() + deathTeam.getDisplayName() + ChatColor.RED)));
        }
    }

    public boolean isEndGameRunning() {
        return this.endGameRunning;
    }

}
