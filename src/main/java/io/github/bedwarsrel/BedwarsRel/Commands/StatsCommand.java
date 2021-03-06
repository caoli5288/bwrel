package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Statistics.PlayerStatistic;
import io.github.bedwarsrel.BedwarsRel.UUIDFetcher;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

public class StatsCommand extends BaseCommand implements ICommand {

    public StatsCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "stats";
    }

    @Override
    public String getName() {
        return Main.local("commands.stats.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.stats.desc");
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

        if (!player.hasPermission("bw.otherstats") && args.size() > 0) {
            args.clear();
        }

        player.sendMessage(ChatHelper.with(
                ChatColor.GREEN + "----------- " + Main.local("stats.header") + " -----------"));

        if (args.size() == 1) {
            String playerStats = args.get(0).toString();
            OfflinePlayer offPlayer = Main.getInstance().getServer().getPlayerExact(playerStats);

            if (offPlayer != null) {
                player.sendMessage(ChatHelper.with(ChatColor.GRAY + Main.local("stats.name") + ": "
                        + ChatColor.YELLOW + offPlayer.getName()));
                PlayerStatistic statistic =
                        Main.getInstance().getPlayerStatisticManager().getStatistic(offPlayer);
                if (statistic == null) {
                    player.sendMessage(ChatHelper.with(ChatColor.RED
                            + Main.local("stats.statsnotfound", ImmutableMap.of("player", playerStats))));
                    return true;
                }

                this.sendStats(player, statistic);
                return true;
            }

            UUID offUUID = null;
            try {
                offUUID = UUIDFetcher.getUUIDOf(playerStats);
                if (offUUID == null) {
                    player.sendMessage(ChatHelper.with(ChatColor.RED
                            + Main.local("stats.statsnotfound", ImmutableMap.of("player", playerStats))));
                    return true;
                }
            } catch (Exception e) {
                Main.getInstance().getBugsnag().notify(e);
                e.printStackTrace();
            }

            offPlayer = Main.getInstance().getServer().getOfflinePlayer(offUUID);
            if (offPlayer == null) {
                player.sendMessage(ChatHelper.with(ChatColor.RED
                        + Main.local("stats.statsnotfound", ImmutableMap.of("player", playerStats))));
                return true;
            }

            PlayerStatistic statistic =
                    Main.getInstance().getPlayerStatisticManager().getStatistic(offPlayer);
            if (statistic == null) {
                player.sendMessage(ChatHelper.with(ChatColor.RED
                        + Main.local("stats.statsnotfound", ImmutableMap.of("player", offPlayer.getName()))));
                return true;
            }

            this.sendStats(player, statistic);
            return true;
        } else if (args.size() == 0) {
            PlayerStatistic statistic =
                    Main.getInstance().getPlayerStatisticManager().getStatistic(player);
            if (statistic == null) {
                player.sendMessage(ChatHelper.with(ChatColor.RED
                        + Main.local("stats.statsnotfound", ImmutableMap.of("player", player.getName()))));
                return true;
            }

            this.sendStats(player, statistic);
            return true;
        }

        return false;
    }

    private void sendStats(Player player, PlayerStatistic statistic) {
        for (String line : statistic.createStatisticLines(false, ChatColor.GRAY, ChatColor.YELLOW)) {
            player.sendMessage(line);
        }
    }

    @Override
    public String getPermission() {
        return "base";
    }

}
