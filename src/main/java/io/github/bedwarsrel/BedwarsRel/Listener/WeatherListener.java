package io.github.bedwarsrel.BedwarsRel.Listener;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.List;

public class WeatherListener extends BaseListener {

    @EventHandler
    public void onWeatherEvent(WeatherChangeEvent we) {
        if (we.isCancelled()) {
            return;
        }

        List<Game> games = Main.getInstance().getGameManager().getGamesByWorld(we.getWorld());

        if (games.size() == 0) {
            return;
        }

        for (Game game : games) {
            if (game.getState() != GameState.STOPPED) {
                we.setCancelled(true);
                break;
            }
        }
    }

}
