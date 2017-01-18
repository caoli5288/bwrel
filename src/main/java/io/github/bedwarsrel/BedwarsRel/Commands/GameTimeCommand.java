package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class GameTimeCommand extends BaseCommand implements ICommand {

    public GameTimeCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "gametime";
    }

    @Override
    public String getName() {
        return Main.local("commands.gametime.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.gametime.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "time"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        Player player = (Player) sender;

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        String gametime = args.get(1).toString();

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

        if (!Utils.isNumber(gametime) && !"day".equals(gametime) && !"night".equals(gametime)) {
            player.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.timeincorrect")));
            return true;
        }

        int time = 1000;
        if ("day".equals(gametime)) {
            time = 6000;
        } else if ("night".equals(gametime)) {
            time = 18000;
        } else {
            time = Integer.valueOf(gametime);
        }

        game.setTime(time);
        player.sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.gametimeset")));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
