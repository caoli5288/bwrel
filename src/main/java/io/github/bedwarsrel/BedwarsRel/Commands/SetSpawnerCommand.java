package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.RessourceSpawner;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetSpawnerCommand extends BaseCommand {

    public SetSpawnerCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "setspawner";
    }

    @Override
    public String getName() {
        return Main.local("commands.setspawner.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.setspawner.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "ressource"};
    }

    private String[] getRessources() {
        ConfigurationSection section =
                Main.getInstance().getConfig().getConfigurationSection("ressource");
        if (section == null) {
            return new String[]{};
        }

        List<String> ressources = new ArrayList<String>();
        for (String key : section.getKeys(false)) {
            ressources.add(key.toLowerCase());
        }

        return ressources.toArray(new String[ressources.size()]);
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;
        ArrayList<String> arguments = new ArrayList<String>(Arrays.asList(this.getRessources()));
        String material = args.get(1).toString().toLowerCase();
        Game game = this.getPlugin().getGameManager().getGame(args.get(0));

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

        if (!arguments.contains(material)) {
            player
                    .sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.spawnerargument")));
            return false;
        }

        Object section = Main.getInstance().getConfig().get("ressource." + material);
        ItemStack stack = RessourceSpawner.createSpawnerStackByConfig(section);

        Location location = player.getLocation();
        RessourceSpawner spawner = new RessourceSpawner(game, material, location);
        game.addRessourceSpawner(spawner);
        player.sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.spawnerset",
                ImmutableMap.of("name", stack.getItemMeta().getDisplayName() + ChatColor.GREEN))));
        return true;
    }

    @Override
    public String getPermission() {
        return "setup";
    }

}
