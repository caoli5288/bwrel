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

public class RegionNameCommand extends BaseCommand implements ICommand {

    public RegionNameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "regionname";
    }

    @Override
    public String getName() {
        return Main.local("commands.regionname.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.regionname.desc");
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

        Player player = (Player) sender;

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        String name = args.get(1).toString();

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

        if (name.length() > 15) {
            player.sendMessage(
                    ChatHelper.with(ChatColor.RED + Main.local("errors.toolongregionname")));
            return true;
        }

        game.setRegionName(name);
        player
                .sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.regionnameset")));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
