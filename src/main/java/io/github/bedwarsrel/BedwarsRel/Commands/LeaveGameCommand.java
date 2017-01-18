package io.github.bedwarsrel.BedwarsRel.Commands;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class LeaveGameCommand extends BaseCommand {

    public LeaveGameCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "leave";
    }

    @Override
    public String getName() {
        return Main.local("commands.leave.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.leave.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (game == null) {
            return true;
        }

        game.playerLeave(player, false);
        return true;
    }

    @Override
    public String getPermission() {
        return "base";
    }

}
