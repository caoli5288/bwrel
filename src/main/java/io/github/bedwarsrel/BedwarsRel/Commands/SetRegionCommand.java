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

public class SetRegionCommand extends BaseCommand implements ICommand {

    public SetRegionCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setregion";
    }

    @Override
    public String getName() {
        return Main.local("commands.setregion.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.setregion.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "loc1;loc2"};
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

        if (game.getState() == GameState.RUNNING) {
            sender.sendMessage(
                    ChatHelper.with(ChatColor.RED + Main.local("errors.notwhilegamerunning")));
            return false;
        }

        String loc = args.get(1);
        if (!loc.equalsIgnoreCase("loc1") && !loc.equalsIgnoreCase("loc2")) {
            player
                    .sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.regionargument")));
            return false;
        }

        game.setLoc(player.getLocation(), loc);
        player.sendMessage(ChatHelper.with(ChatColor.GREEN
                + Main.local("success.regionset", ImmutableMap.of("location", loc, "game", game.getName()))));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
