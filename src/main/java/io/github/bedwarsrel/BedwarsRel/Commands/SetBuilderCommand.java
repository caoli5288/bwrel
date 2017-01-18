package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class SetBuilderCommand extends BaseCommand implements ICommand {

    public SetBuilderCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setbuilder";
    }

    @Override
    public String getName() {
        return Main.local("commands.setbuilder.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.setbuilder.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "builder"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        String builder = args.get(1).toString();

        if (game == null) {
            sender.sendMessage(ChatHelper.with(ChatColor.RED
                    + Main.local("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
            return false;
        }

        game.setBuilder(builder);
        sender.sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.builderset")));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
