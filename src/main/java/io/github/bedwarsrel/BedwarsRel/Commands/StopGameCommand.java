package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class StopGameCommand extends BaseCommand implements ICommand {

    public StopGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "stop";
    }

    @Override
    public String getName() {
        return Main.local("commands.stop.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.stop.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        Game game = null;

        if (args.size() == 0) {
            game = this.getPlugin().getGameManager().getGameOfPlayer((Player) sender);

            if (game == null) {
                sender.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.notingame")));
                return false;
            }
        }

        if (args.size() != 0) {
            game = this.getPlugin().getGameManager().getGame(args.get(0));

            if (game == null) {
                sender.sendMessage(ChatHelper.with(ChatColor.RED
                        + Main.local("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
                return false;
            }
        }

        if (!game.stop()) {
            sender
                    .sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.gamenotrunning")));
            return false;
        }

        sender.sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.stopped")));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
