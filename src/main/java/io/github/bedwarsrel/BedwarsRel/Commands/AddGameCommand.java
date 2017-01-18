package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class AddGameCommand extends BaseCommand {

    public AddGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "addgame";
    }

    @Override
    public String getName() {
        return Main.local("commands.addgame.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.addgame.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"name", "minplayers"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        Game addGame = this.getPlugin().getGameManager().addGame(args.get(0));
        String minPlayers = args.get(1);

        if (!Utils.isNumber(minPlayers)) {
            sender.sendMessage(
                    ChatHelper.with(ChatColor.RED + Main.local("errors.minplayersmustnumber")));
            return false;
        }

        if (addGame == null) {
            sender.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.gameexists")));
            return false;
        }

        int min = Integer.parseInt(minPlayers);
        if (min <= 0) {
            min = 1;
        }

        addGame.setMinPlayers(min);
        sender.sendMessage(ChatHelper.with(ChatColor.GREEN
                + Main.local("success.gameadded", ImmutableMap.of("game", args.get(0).toString()))));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
