package io.github.bedwarsrel.BedwarsRel.Statistics;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameManager;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Main;
import me.clip.placeholderapi.external.EZPlaceholderHook;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by SkySslience on 2016.10.17.
 */
public class Placeholders extends EZPlaceholderHook {

    public Placeholders(Plugin plugin, String placeholderName) {
        super(plugin, placeholderName);
    }

    @Override
    public String onPlaceholderRequest(Player player, String arg) {
        PlayerStatistic killerPlayer = Main.getInstance().getPlayerStatisticManager()
                .getStatistic(player);
        if (killerPlayer == null) return "0";
        if (arg.equalsIgnoreCase("player_currentscore")) {
            return String.valueOf(killerPlayer.getCurrentScore());
        }
        if (arg.equalsIgnoreCase("player_kd")) {
            return String.valueOf(killerPlayer.getKD());
        }
        if (arg.equalsIgnoreCase("player_score")) {
            return String.valueOf(killerPlayer.getScore());
        }
        if (arg.equalsIgnoreCase("player_kills")) {
            return String.valueOf(killerPlayer.getKills());
        }
        if (arg.equalsIgnoreCase("player_loses")) {
            return String.valueOf(killerPlayer.getLoses());
        }
        if (arg.equalsIgnoreCase("player_wins")) {
            return String.valueOf(killerPlayer.getWins());
        }
        if (arg.equalsIgnoreCase("player_games")) {
            return String.valueOf(killerPlayer.getGames());
        }
        if (arg.equalsIgnoreCase("player_deaths")) {
            return String.valueOf(killerPlayer.getDeaths());
        }
        if (arg.equalsIgnoreCase("player_destroyedbeds")) {
            return String.valueOf(killerPlayer.getDestroyedBeds());
        }
        if (arg.equalsIgnoreCase("player_team")) {
            Game game = GameManager.getGameBy(player);
            if (game == null) return null;
            Team team = game.getPlayerTeam(player);
            if (team == null) return null;
            return team.getName();
        }
        if (arg.equalsIgnoreCase("player_team_color")) {
            Game game = GameManager.getGameBy(player);
            if (game == null) return null;
            Team team = game.getPlayerTeam(player);
            if (team == null) return null;
            return team.getChatColor().toString();
        }
        return null;
    }
}
