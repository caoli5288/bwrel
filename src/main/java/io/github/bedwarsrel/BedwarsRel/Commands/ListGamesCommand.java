package io.github.bedwarsrel.BedwarsRel.Commands;

import com.google.common.collect.ImmutableMap;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameCheckCode;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.bukkit.util.ChatPaginator.ChatPage;

import java.util.ArrayList;
import java.util.List;

public class ListGamesCommand extends BaseCommand {

    public ListGamesCommand(Main plugin) {
        super(plugin);
    }

    @Override
    public String getCommand() {
        return "list";
    }

    @Override
    public String getName() {
        return Main.local("commands.list.name");
    }

    @Override
    public String getDescription() {
        return Main.local("commands.list.desc");
    }

    @Override
    public String[] getArguments() {
        return new String[]{};
    }

    @Override
    public boolean execute(CommandSender sender, ArrayList<String> args) {
        if (!sender.hasPermission("bw." + this.getPermission())) {
            return false;
        }

        String paginate;
        int page = 1;
        ArrayList<Game> showedGames = new ArrayList<Game>();

        if (args != null) {
            if (args.size() == 0 || args.size() > 1) {
                paginate = "1";
            } else {
                paginate = args.get(0);
                if (paginate.isEmpty()) {
                    paginate = "1";
                }

                if (!Utils.isNumber(paginate)) {
                    paginate = "1";
                }
            }
        } else {
            paginate = "1";
        }

        page = Integer.parseInt(paginate);
        StringBuilder sb = new StringBuilder();
        sender.sendMessage(ChatColor.GREEN + "---------- Bedwars Games ----------");

        List<Game> games = Main.getInstance().getGameManager().getGames();
        for (Game game : games) {
            GameCheckCode code = game.checkGame();
            if (code != GameCheckCode.OK && !sender.hasPermission("bw.setup")) {
                continue;
            }

            showedGames.add(game);
            int players = 0;
            if (game.getState() == GameState.RUNNING) {
                players = game.getCurrentPlayerAmount();
            } else {
                players = game.getPlayers().size();
            }

            sb.append(ChatColor.YELLOW
                    + ((code != GameCheckCode.OK) ? ChatColor.RED + game.getName() + ChatColor.YELLOW
                    : game.getName())
                    + " - " + game.getRegion().getName() + " - "
                    + Main.local("sign.gamestate." + game.getState().toString().toLowerCase()) + ChatColor.YELLOW
                    + " - " + Main.local("sign.players") + ": " + ChatColor.WHITE + "[" + ChatColor.YELLOW
                    + players + ChatColor.WHITE + "/" + ChatColor.YELLOW + game.getMaxPlayers()
                    + ChatColor.WHITE + "]\n");
        }

        if (showedGames.size() == 0) {
            sb.append(ChatColor.RED + Main.local("errors.nogames"));
        }

        ChatPage chatPage = ChatPaginator.paginate(sb.toString(), page);
        for (String line : chatPage.getLines()) {
            sender.sendMessage(line);
        }
        sender.sendMessage(ChatColor.GREEN + "---------- "
                + Main.local("default.pages",
                ImmutableMap.of("current", String.valueOf(chatPage.getPageNumber()), "max",
                        String.valueOf(chatPage.getTotalPages())))
                + " ----------");

        return true;
    }

    @Override
    public String getPermission() {
        return "base";
    }

}
