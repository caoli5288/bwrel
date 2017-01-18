package io.github.bedwarsrel.BedwarsRel.Commands;

import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameManager;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class KickCommand extends BaseCommand implements ICommand {

    public KickCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "kick";
    }

    @Override
    public String getName() {
        return Main.local("commands.kick.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.kick.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"player"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!super.hasPermission(sender) && !sender.isOp()) {
            return false;
        }

        Player player = (Player) sender;
        Game game = GameManager.getGameBy(player);

        // find player
        Player kickPlayer = Main.getInstance().getServer().getPlayer(args.get(0).toString());

        if (game == null) {
            player.sendMessage(ChatHelper.with(Main.local("errors.notingameforkick")));
            return true;
        }

        if (kickPlayer == null || !kickPlayer.isOnline()) {
            player.sendMessage(ChatHelper.with(Main.local("errors.playernotfound")));
            return true;
        }

        if (!game.isInGame(kickPlayer)) {
            player.sendMessage(ChatHelper.with(Main.local("errors.playernotingame")));
            return true;
        }

        game.playerLeave(kickPlayer, true);
        return true;
    }

    @Override
    public String getPermission() {
        return "kick";
    }

}
