package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.TeamColor;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class AddTeamCommand extends BaseCommand {

    public AddTeamCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "addteam";
    }

    @Override
    public String getName() {
        return Main.local("commands.addteam.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.addteam.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "name", "color", "maxplayers"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        String name = args.get(1);
        String color = args.get(2);
        String maxPlayers = args.get(3);

        TeamColor tColor = TeamColor.valueOf(color.toUpperCase());

        if (game == null) {
            sender.sendMessage(ChatHelper.with(ChatColor.RED
                    + Main.local("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
            return false;
        }

        if (game.getState() != GameState.STOPPED) {
            sender.sendMessage(
                    ChatHelper.with(ChatColor.RED + Main.local("errors.notwhilegamerunning")));
            return false;
        }

        int playerMax = Integer.parseInt(maxPlayers);

        if (playerMax < 1 || playerMax > 24) {
            sender.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.playeramount")));
            return false;
        }

        if (tColor == null) {
            sender.sendMessage(
                    ChatHelper.with(ChatColor.RED + Main.local("errors.teamcolornotallowed")));
            return false;
        }

        if (name.length() < 3 || name.length() > 20) {
            sender
                    .sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.teamnamelength")));
            return false;
        }

        if (game.getTeam(name) != null) {
            sender.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.teamnameinuse")));
            return false;
        }

        game.addTeam(name, tColor, playerMax);
        sender.sendMessage(ChatHelper.with(
                ChatColor.GREEN + Main.local("success.teamadded", ImmutableMap.of("team", name))));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
