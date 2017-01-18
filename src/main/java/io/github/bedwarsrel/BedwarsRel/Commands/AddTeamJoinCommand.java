package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Game.TeamJoinMetaDataValue;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class AddTeamJoinCommand extends BaseCommand {

    public AddTeamJoinCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getPermission() {
        return "setup";
    }

    @Override
    public String getCommand() {
        return "addteamjoin";
    }

    @Override
    public String getName() {
        return Main.local("commands.addteamjoin.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.addteamjoin.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{"game", "team"};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!super.hasPermission(sender)) {
            return false;
        }

        Player player = (Player) sender;
        String team = args.get(1);

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

        Team gameTeam = game.getTeam(team);

        if (gameTeam == null) {
            player.sendMessage(ChatHelper.with(ChatColor.RED + Main.local("errors.teamnotfound")));
            return false;
        }

        // only in lobby
        if (game.getLobby() == null || !player.getWorld().equals(game.getLobby().getWorld())) {
            player.sendMessage(
                    ChatHelper.with(ChatColor.RED + Main.local("errors.mustbeinlobbyworld")));
            return false;
        }

        if (player.hasMetadata("bw-addteamjoin")) {
            player.removeMetadata("bw-addteamjoin", Main.getInstance());
        }

        player.setMetadata("bw-addteamjoin", new TeamJoinMetaDataValue(gameTeam));
        final Player runnablePlayer = player;

        new BukkitRunnable() {

            @Override
            public void run() {
                try {
                    if (!runnablePlayer.hasMetadata("bw-addteamjoin")) {
                        return;
                    }

                    runnablePlayer.removeMetadata("bw-addteamjoin", Main.getInstance());
                } catch (Exception ex) {
                    Main.getInstance().getBugsnag().notify(ex);
                    // just ignore
                }
            }
        }.runTaskLater(Main.getInstance(), 20L * 10L);

        player.sendMessage(
                ChatHelper.with(ChatColor.GREEN + Main.local("success.selectteamjoinentity")));
        return true;
    }

}
