package io.github.bedwarsrel.BedwarsRel.Listener;

import io.github.bedwarsrel.BedwarsRel.$;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

public class ServerListener extends BaseListener {

    @EventHandler
    public void handle(ServerListPingEvent event) {
        if (!Main.getInstance().isBungee()) {
            return;
        }
        if (Main.getInstance().getGameManager() == null
                || Main.getInstance().getGameManager().getGames() == null
                || Main.getInstance().getGameManager().getGames().size() == 0) {
            return;
        }

        Game game = Main.getInstance().getGameManager().getFirstGame();

        if (game == null) {
            return;
        }

        GameState state = game.getState();

        if ($.nil(state)) return;

        switch (state) {
            case STOPPED:
                event.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfig().getString("bungeecord.motds.stopped"))));
                break;
            case WAITING:
                if (game.isFull()) {
                    event.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
                            Main.getInstance().getConfig().getString("bungeecord.motds.full"))));
                } else {
                    event.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
                            Main.getInstance().getConfig().getString("bungeecord.motds.lobby"))));
                }

                break;
            case RUNNING:
                event.setMotd(replacePlaceholder(game, ChatColor.translateAlternateColorCodes('&',
                        Main.getInstance().getConfig().getString("bungeecord.motds.running"))));
                break;
        }
    }

    private String replacePlaceholder(Game game, String line) {
        String finalLine = line;

        finalLine = finalLine.replace("$title$", Main.local("sign.firstline"));
        finalLine = finalLine.replace("$gamename$", game.getName());
        finalLine = finalLine.replace("$regionname$", game.getRegion().getName());
        finalLine = finalLine.replace("$maxplayers$", getMaxPlayersString(game));
        finalLine = finalLine.replace("$currentplayers$", getCurrentPlayersString(game));
        finalLine = finalLine.replace("$status$", getState(game));

        return finalLine;
    }

    private String getMaxPlayersString(Game game) {
        int maxPlayers = game.getMaxPlayers();
        return String.valueOf(maxPlayers);
    }

    private String getCurrentPlayersString(Game game) {
        int current;
        if (game.getState() == GameState.RUNNING) {
            current = game.getTeamPlayers().size();
        } else if (game.getState() == GameState.WAITING) {
            current = game.getPlayers().size();
        } else {
            current = 0;
        }
        return String.valueOf(current);
    }

    private String getState(Game game) {
        String result;
        if (game.getState() == GameState.WAITING && game.isFull()) {
            result = ChatColor.RED + Main.local("sign.gamestate.full");
        } else {
            result = Main.local("sign.gamestate." + game.getState().toString().toLowerCase());
        }
        return result;
    }

}
