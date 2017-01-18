package io.github.bedwarsrel.BedwarsRel.Commands;

import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public interface ICommand {

    public String getCommand();

    public String getPermission();

    public String getName();

    public String getDescription();

    public String[] getArguments();

    public boolean hasPermission(CommandSender sender);

    public boolean execute(CommandSender sender, ArrayList<String> args);

}
