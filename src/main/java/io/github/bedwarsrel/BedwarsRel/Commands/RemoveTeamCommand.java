package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class RemoveTeamCommand extends BaseCommand {

    public RemoveTeamCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "removeteam";
    }

    @Override
    public String getName() {
        return Main.local("commands.removeteam.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.removeteam.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "name"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        String name = args.get(1);

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

        Team theTeam = game.getTeam(name);
        if (theTeam == null) {
            sender.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.teamnotfound")));
            return false;
        }

        game.removeTeam(theTeam);
        sender.sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.teamremoved")));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
