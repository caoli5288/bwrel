package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SetLobbyCommand extends BaseCommand implements ICommand {

    public SetLobbyCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setlobby";
    }

    @Override
    public String getName() {
        return Main.local("commands.setlobby.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.setlobby.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        if (game == null) {
            player.sendMessage(ChatHelper.with(ChatColor.RED
                    + Main.local("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
            return false;
        }

        if (game.getState() != GameState.STOPPED) {
            sender.sendMessage(
                    ChatHelper.with(ChatColor.RED + Main.local("errors.notwhilegamerunning")));
            return false;
        }

        game.setLobby(player);
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
