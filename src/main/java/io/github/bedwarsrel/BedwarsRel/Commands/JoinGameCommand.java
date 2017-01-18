package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameManager;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class JoinGameCommand extends BaseCommand {

    public JoinGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "join";
    }

    @Override
    public String getName() {
        return Main.local("commands.join.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.join.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;
        Game game = this.getPlugin().getGameManager().getGame(args.get(0));
        Game gameOfPlayer = GameManager.getGameBy(player);

        if (gameOfPlayer != null) {
            if (gameOfPlayer.getState() == GameState.RUNNING) {
                sender.sendMessage(
                        ChatHelper.with(ChatColor.RED + Main.local("errors.notwhileingame")));
                return false;
            }

            if (gameOfPlayer.getState() == GameState.WAITING) {
                gameOfPlayer.playerLeave(player, false);
            }
        }

        if (game == null) {
            sender.sendMessage(ChatHelper.with(ChatColor.RED
                    + Main.local("errors.gamenotfound", ImmutableMap.of("game", args.get(0).toString()))));
            return false;
        }

        if (game.playerJoins(player)) {
            sender.sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.joined")));
        }
        return true;
    }

    @Override
    public String getPermission() {
        return "base";
    }

}
