package io.github.bedwarsrel.BedwarsRel.Commands;

import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class AddHoloCommand extends BaseCommand implements ICommand {

    public AddHoloCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "addholo";
    }

    @Override
    public String getName() {
        return Main.local("commands.addholo.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.addholo.desc");
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

        if (!Main.getInstance().isHologramsEnabled()
                && Main.getInstance().getHolographicInteractor() != null) {
            return true;
        }

        Player player = (Player) sender;
        Main.getInstance().getHolographicInteractor().addHologramLocation(player.getEyeLocation());
        Main.getInstance().getHolographicInteractor().updateHolograms();
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
