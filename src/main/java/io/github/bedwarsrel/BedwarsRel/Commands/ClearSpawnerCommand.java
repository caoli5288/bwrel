package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class ClearSpawnerCommand extends BaseCommand implements ICommand {

    public ClearSpawnerCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "clearspawner";
    }

    @Override
    public String getName() {
        return Main.local("commands.clearspawner.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.clearspawner.desc");
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

        if (game.getState() == GameState.RUNNING) {
            sender.sendMessage(
                    ChatHelper.with(ChatColor.RED + Main.local("errors.notwhilegamerunning")));
            return false;
        }

        game.getRessourceSpawner().clear();
        sender
                .sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.spawnercleared")));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
