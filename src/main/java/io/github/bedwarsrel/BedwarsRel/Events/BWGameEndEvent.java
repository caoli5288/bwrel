package io.github.bedwarsrel.BedwarsRel.Events;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class BWGameEndEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private Game game = null;

    public BWGameEndEvent(Game game) {
        this.game = game;
    }

    @Override
    public HandlerList getHandlers() {
        return BWGameEndEvent.handlers;
    }

    public static HandlerList getHandlerList() {
        return BWGameEndEvent.handlers;
    }

    public Game getGame() {
        return this.game;
    }

}
