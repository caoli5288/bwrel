package io.github.bedwarsrel.BedwarsRel.Commands;

import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class BaseCommand implements ICommand {

    private Main plugin = null;

    public BaseCommand(Main plugin) {
        this.plugin = plugin;
    }

    protected Main getPlugin() {
        return this.plugin;
    }

    @Override
    public abstract String getCommand();

    @Override
    public abstract String getName();

    @Override
    public abstract String getDescription();

    @Override
    public abstract String[] getArguments();

    @Override
    public abstract boolean execute(CommandSender sender, ArrayList<String> args);

    @Override
    public boolean hasPermission(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatHelper.with("Only players should execute this command!"));
            return false;
        }

        if (!sender.hasPermission("bw." + this.getPermission())) {
            sender.sendMessage(ChatHelper
                    .with(ChatColor.RED + "You don't have permission to execute this command!"));
            return false;
        }

        return true;
    }

}
