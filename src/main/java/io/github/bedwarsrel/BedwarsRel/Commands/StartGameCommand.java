package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class StartGameCommand extends BaseCommand implements ICommand {

    public StartGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "start";
    }

    @Override
    public String getName() {
        return Main.local("commands.start.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.start.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        if (game == null) {
            sender.sendMessage(ChatHelper.with(ChatColor.RED
                    + Main.local("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
            return false;
        }

        game.run(sender);
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
