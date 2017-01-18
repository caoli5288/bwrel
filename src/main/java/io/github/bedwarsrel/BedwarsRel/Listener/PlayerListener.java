package io.github.bedwarsrel.BedwarsRel.Listener;

import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Events.BedwarsOpenShopEvent;
import io.github.bedwarsrel.BedwarsRel.Game.BungeeGameCycle;
import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Game.Team;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Shop.NewItemShop;
import io.github.bedwarsrel.BedwarsRel.Villager.MerchantCategory;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class PlayerListener extends BaseListener {

    private static Class<?> spawn;

    static {
        try {
            spawn = Class.forName("io.github.bedwarsrel.BedwarsRel.Com." + Main.getInstance().getCurrentVersion() + ".PerformRespawnRunnable");
        } catch (ClassNotFoundException ex) {
            try {
                spawn = Class.forName("io.github.bedwarsrel.BedwarsRel.Com.Fallback.PerformRespawnRunnable");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (Main.getInstance().isHologramsEnabled()
                && Main.getInstance().getHolographicInteractor() != null && Main.getInstance()
                .getHolographicInteractor().getType().equalsIgnoreCase("HolographicDisplays")) {
            Main.getInstance().getHolographicInteractor().updateHolograms(player, 60L);
        }

        ArrayList<Game> games = Main.getInstance().getGameManager().getGames();
        if (games.size() == 0) {
            return;
        }

        if (Main.getInstance().isBungee()) {
            event.setJoinMessage(null);
            Game firstGame = games.get(0);

            if (firstGame.getState() == GameState.STOPPED && player.hasPermission("bw.setup")) {
                return;
            }

            if (!firstGame.playerJoins(player)) {
                Main.run(() -> {
                    if (firstGame.getCycle() instanceof BungeeGameCycle) {
                        ((BungeeGameCycle) firstGame.getCycle())
                                .bungeeSendToServer(Main.getInstance().getBungeeHub(), player, true);
                    }
                }, 5);
            }

        } else {
            Game game = Main.getInstance().getGameManager().getGameByLocation(player.getLocation());

            if (game != null) {
                if (game.getMainLobby() != null) {
                    player.teleport(game.getMainLobby());
                } else {
                    game.playerJoins(player);
                }
            }
        }

    }

    @EventHandler
    public void onSwitchWorld(PlayerChangedWorldEvent change) {
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(change.getPlayer());
        if (game != null) {
            if (game.getState() == GameState.RUNNING) {
                if (!game.getCycle().isEndGameRunning()) {
                    if (!game.getPlayerSettings(change.getPlayer()).isTeleporting()) {
                        game.playerLeave(change.getPlayer(), false);
                    } else {
                        game.getPlayerSettings(change.getPlayer()).setTeleporting(false);
                    }
                }
            } else if (game.getState() == GameState.WAITING) {
                if (!game.getPlayerSettings(change.getPlayer()).isTeleporting()) {
                    game.playerLeave(change.getPlayer(), false);
                } else {
                    game.getPlayerSettings(change.getPlayer()).setTeleporting(false);
                }
            }
        }

        if (!Main.getInstance().isHologramsEnabled()
                || Main.getInstance().getHolographicInteractor() == null) {
            return;
        }

        Main.getInstance().getHolographicInteractor().updateHolograms(change.getPlayer());
    }

  /*
   * GAME
   */

    @SuppressWarnings("deprecation")
    private void inGameInteractEntity(PlayerInteractEntityEvent iee, Game game, Player player) {

        if (Main.getInstance().getCurrentVersion().startsWith("v1_8")) {
            if (iee.getPlayer().getItemInHand().getType().equals(Material.MONSTER_EGG)
                    || iee.getPlayer().getItemInHand().getType().equals(Material.MONSTER_EGGS)
                    || iee.getPlayer().getItemInHand().getType().equals(Material.DRAGON_EGG)) {
                iee.setCancelled(true);
                return;
            }
        } else {
            if (iee.getPlayer().getInventory().getItemInMainHand().getType().equals(Material.MONSTER_EGG)
                    || iee.getPlayer().getInventory().getItemInMainHand().getType()
                    .equals(Material.MONSTER_EGGS)
                    || iee.getPlayer().getInventory().getItemInMainHand().getType()
                    .equals(Material.DRAGON_EGG)
                    || iee.getPlayer().getInventory().getItemInOffHand().getType()
                    .equals(Material.MONSTER_EGG)
                    || iee.getPlayer().getInventory().getItemInOffHand().getType()
                    .equals(Material.MONSTER_EGGS)
                    || iee.getPlayer().getInventory().getItemInOffHand().getType()
                    .equals(Material.DRAGON_EGG)) {
                iee.setCancelled(true);
                return;
            }
        }

        if (iee.getRightClicked() != null
                && !iee.getRightClicked().getType().equals(EntityType.VILLAGER)) {
            List<EntityType> preventClickTypes =
                    Arrays.asList(EntityType.ITEM_FRAME, EntityType.ARMOR_STAND);

            if (preventClickTypes.contains(iee.getRightClicked().getType())) {
                iee.setCancelled(true);
            }

            return;
        }

        iee.setCancelled(true);

        if (game.spectator(player)) {
            return;
        }

        BedwarsOpenShopEvent openShopEvent =
                new BedwarsOpenShopEvent(game, player, game.getItemShopCategories(), iee.getRightClicked());
        Main.getInstance().getServer().getPluginManager().callEvent(openShopEvent);

        if (openShopEvent.isCancelled()) {
            return;
        }

        if (game.getPlayerSettings(player).useOldShop()) {
            MerchantCategory.openCategorySelection(player, game);
        } else {
            NewItemShop itemShop = game.getNewItemShop(player);
            if (itemShop == null) {
                itemShop = game.openNewItemShop(player);
            }

            itemShop.setCurrentCategory(null);
            itemShop.openCategoryInventory(player);
        }
    }

    @EventHandler
    public void openInventory(InventoryOpenEvent ioe) {
        if (!(ioe.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) ioe.getPlayer();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (game == null) {
            return;
        }

        if (game.getState() != GameState.RUNNING) {
            return;
        }

        if (ioe.getInventory().getType() == InventoryType.ENCHANTING
                || ioe.getInventory().getType() == InventoryType.BREWING
                || (ioe.getInventory().getType() == InventoryType.CRAFTING
                && !Main.getInstance().getBooleanConfig("allow-crafting", false))) {
            ioe.setCancelled(true);
            return;
        } else if (ioe.getInventory().getType() == InventoryType.CRAFTING
                && Main.getInstance().getBooleanConfig("allow-crafting", false)) {
            return;
        }

        if (game.spectator(player)) {
            if (ioe.getInventory().getName().equals(Main.local("ingame.spectator"))) {
                return;
            }

            ioe.setCancelled(true);
        }

        if (ioe.getInventory().getHolder() == null) {
            return;
        }

        if (game.getRegion().getInventories().contains(ioe.getInventory())) {
            return;
        }

        game.getRegion().addInventory(ioe.getInventory());
    }

    @EventHandler
    public void onCraft(CraftItemEvent cie) {
        Player player = (Player) cie.getWhoClicked();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (game == null) {
            return;
        }

        if (game.getState() == GameState.STOPPED) {
            return;
        }

        if (Main.getInstance().getBooleanConfig("allow-crafting", false)) {
            return;
        }

        cie.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent pre) {
        Player p = pre.getPlayer();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(p);

        if (game == null) {
            return;
        }

        if (game.getState() == GameState.RUNNING) {
            game.getCycle().handle(pre, p);
            return;
        }

        if (game.getState() == GameState.WAITING) {
            pre.setRespawnLocation(game.getLobby());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(PlayerDeathEvent event) {
        Player p = event.getEntity();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(p);

        if (game == null) {
            return;
        }

        if (game.getState() == GameState.RUNNING) {
            event.setDroppedExp(0);
            event.setDeathMessage(null);

            if (!Main.getInstance().getBooleanConfig("player-drops", false)) {
                event.getDrops().clear();
            }

            try {
                if (!Main.getInstance().isSpigot()) {
                    BukkitRunnable r = (BukkitRunnable) spawn.getDeclaredConstructor(Player.class).newInstance(p);
                    r.runTaskLater(Main.getInstance(), 40);
                } else {
                    Main.run(() -> p.spigot().respawn(), 40);
                }

            } catch (Exception e) {
                Main.getInstance().getBugsnag().notify(e);
                e.printStackTrace();
            }

            event.setKeepInventory(false);

            Player killer = p.getKiller();
            if (killer == null) {
                killer = game.getPlayerDamager(p);
            }

            game.getCycle().onPlayerDies(p, killer);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent ice) {
        Player player = (Player) ice.getWhoClicked();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (game == null) {
            return;
        }

        if (game.getState() == GameState.WAITING) {
            this.onLobbyInventoryClick(ice, player, game);
        }

        if (game.getState() == GameState.RUNNING) {
            this.onIngameInventoryClick(ice, player, game);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void onIngameInventoryClick(InventoryClickEvent ice, Player player, Game game) {
        if (!ice.getInventory().getName().equals(Main.local("ingame.shop.name"))) {
            if (game.spectator(player)
                    || (game.getCycle() instanceof BungeeGameCycle && game.getCycle().isEndGameRunning()
                    && Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {

                ItemStack clickedStack = ice.getCurrentItem();
                if (clickedStack == null) {
                    return;
                }

                if (ice.getInventory().getName().equals(Main.local("ingame.spectator"))) {
                    ice.setCancelled(true);
                    if (!clickedStack.getType().equals(Material.SKULL_ITEM)) {
                        return;
                    }

                    SkullMeta meta = (SkullMeta) clickedStack.getItemMeta();
                    Player pl = Main.getInstance().getServer().getPlayer(meta.getOwner());
                    if (pl == null) {
                        return;
                    }

                    if (!game.isInGame(pl)) {
                        return;
                    }

                    player.teleport(pl);
                    player.closeInventory();
                    return;
                }

                Material clickedMat = ice.getCurrentItem().getType();
                if (clickedMat.equals(Material.SLIME_BALL)) {
                    game.playerLeave(player, false);
                }

                if (clickedMat.equals(Material.COMPASS)) {
                    game.openSpectatorCompass(player);
                }
            }
            return;
        }

        ice.setCancelled(true);
        ItemStack clickedStack = ice.getCurrentItem();

        if (clickedStack == null) {
            return;
        }

        if (game.getPlayerSettings(player).useOldShop()) {
            try {
                if (clickedStack.getType() == Material.SNOW_BALL) {
                    game.getPlayerSettings(player).setUseOldShop(false);

                    // open new shop
                    NewItemShop itemShop = game.openNewItemShop(player);
                    itemShop.setCurrentCategory(null);
                    itemShop.openCategoryInventory(player);
                    return;
                }

                MerchantCategory cat = game.getItemShopCategories().get(clickedStack.getType());
                if (cat == null) {
                    return;
                }

                Class clazz = Class.forName("io.github.bedwarsrel.BedwarsRel.Com."
                        + Main.getInstance().getCurrentVersion() + ".VillagerItemShop");
                Object villagerItemShop =
                        clazz.getDeclaredConstructor(Game.class, Player.class, MerchantCategory.class)
                                .newInstance(game, player, cat);

                Method openTrade = clazz.getDeclaredMethod("openTrading", new Class[]{});
                openTrade.invoke(villagerItemShop, new Object[]{});
            } catch (Exception ex) {
                Main.getInstance().getBugsnag().notify(ex);
                ex.printStackTrace();
            }
        } else {
            game.getNewItemShop(player).handleInventoryClick(ice, game, player);
        }
    }

    private String getChatFormat(String format, Team team, boolean isSpectator, boolean all) {
        String form = format;

        if (all) {
            form = form.replace("$all$", Main.local("ingame.all") + ChatColor.RESET);
        }

        form = form.replace("$player$",
                ((!isSpectator && team != null) ? team.getChatColor() : "") + "%1$s" + ChatColor.RESET);
        form = form.replace("$msg$", "%2$s");

        if (isSpectator) {
            form = form.replace("$team$", Main.local("ingame.spectator"));
        } else if (team != null) {
            form = form.replace("$team$", team.getDisplayName() + ChatColor.RESET);
        }

        return ChatColor.translateAlternateColorCodes('&', form);
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent ce) {
        Player player = ce.getPlayer();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (game == null) {
            boolean seperateGameChat = Main.getInstance().getBooleanConfig("seperate-game-chat", true);
            if (!seperateGameChat) {
                return;
            }

            Iterator<Player> recipients = ce.getRecipients().iterator();
            while (recipients.hasNext()) {
                Player recipient = recipients.next();
                Game recipientGame = Main.getInstance().getGameManager().getGameOfPlayer(recipient);
                if (recipientGame != null) {
                    recipients.remove();
                }
            }
            return;
        }

        if (game.getState() == GameState.STOPPED) {
            return;
        }

        Team team = game.getPlayerTeam(player);
        String message = ce.getMessage();
        boolean isSpectator = game.spectator(player);

        if (Main.getInstance().getBooleanConfig("overwrite-names", false)) {
            if (team == null) {
                player.setDisplayName(ChatColor.stripColor(player.getName()));

                player.setPlayerListName(ChatColor.stripColor(player.getName()));
            } else {
                player.setDisplayName(team.getChatColor() + ChatColor.stripColor(player.getName()));
                player.setPlayerListName(team.getChatColor() + ChatColor.stripColor(player.getName()));
            }

        }

        if (Main.getInstance().getBooleanConfig("teamname-on-tab", false)) {
            if (team == null || isSpectator) {
                player.setPlayerListName(ChatColor.stripColor(player.getDisplayName()));
            } else {
                player.setPlayerListName(team.getChatColor() + team.getName() + ChatColor.WHITE + " | "
                        + team.getChatColor() + ChatColor.stripColor(player.getDisplayName()));
            }
        }

        if (game.getState() != GameState.RUNNING && game.getState() == GameState.WAITING) {
            String format = null;
            if (team == null) {
                format = this.getChatFormat(
                        Main.getInstance().getStringConfig("lobby-chatformat", "$player$: $msg$"), null, false,
                        true);
            } else {
                format = this.getChatFormat(
                        Main.getInstance().getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"),
                        team, false, true);
            }

            ce.setFormat(format);

            if (!Main.getInstance().getBooleanConfig("seperate-game-chat", true)) {
                return;
            }

            Iterator<Player> recipiens = ce.getRecipients().iterator();
            while (recipiens.hasNext()) {
                Player recipient = recipiens.next();
                if (!game.isInGame(recipient)) {
                    recipiens.remove();
                }
            }

            return;
        }

        @SuppressWarnings("unchecked")
        List<String> toAllPrefixList = (List<String>) Main.getInstance().getConfig()
                .getList("chat-to-all-prefix", Arrays.asList("@"));

        String toAllPrefix = null;

        for (String oneToAllPrefix : toAllPrefixList) {
            if (message.trim().startsWith(oneToAllPrefix)) {
                toAllPrefix = oneToAllPrefix;
            }
        }

        if (toAllPrefix != null || isSpectator || (game.getCycle().isEndGameRunning()
                && Main.getInstance().getBooleanConfig("global-chat-after-end", true))) {
            boolean seperateSpectatorChat =
                    Main.getInstance().getBooleanConfig("seperate-spectator-chat", false);

            message = message.trim();
            String format = null;
            if (!isSpectator && !(game.getCycle().isEndGameRunning()
                    && Main.getInstance().getBooleanConfig("global-chat-after-end", true))) {
                ce.setMessage(message.substring(toAllPrefix.length(), message.length()).trim());
                format = this.getChatFormat(Main.getInstance().getStringConfig("ingame-chatformat-all",
                        "[$all$] <$team$>$player$: $msg$"), team, false, true);
            } else {
                ce.setMessage(message);
                format = this.getChatFormat(
                        Main.getInstance().getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"),
                        team, isSpectator, true);
            }

            ce.setFormat(format);

            if (!Main.getInstance().isBungee() || seperateSpectatorChat) {
                Iterator<Player> recipiens = ce.getRecipients().iterator();
                while (recipiens.hasNext()) {
                    Player recipient = recipiens.next();
                    if (!game.isInGame(recipient)) {
                        recipiens.remove();
                        continue;
                    }

                    if (!seperateSpectatorChat || (game.getCycle().isEndGameRunning()
                            && Main.getInstance().getBooleanConfig("global-chat-after-end", true))) {
                        continue;
                    }

                    if (isSpectator && !game.spectator(recipient)) {
                        recipiens.remove();
                    } else if (!isSpectator && game.spectator(recipient)) {
                        recipiens.remove();
                    }
                }
            }
        } else {
            message = message.trim();
            ce.setMessage(message);
            ce.setFormat(this.getChatFormat(
                    Main.getInstance().getStringConfig("ingame-chatformat", "<$team$>$player$: $msg$"), team,
                    false, false));

            Iterator<Player> recipiens = ce.getRecipients().iterator();
            while (recipiens.hasNext()) {
                Player recipient = recipiens.next();
                if (!game.isInGame(recipient) || !team.isInTeam(recipient)) {
                    recipiens.remove();
                }
            }
        }
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent ppie) {
        Player player = ppie.getPlayer();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (game == null) {
            game = Main.getInstance().getGameManager().getGameByLocation(player.getLocation());
            if (game == null) {
                return;
            }
        }

        if (game.getState() != GameState.WAITING && game.isInGame(player)) {
            return;
        }

        ppie.setCancelled(true);
    }

  /*
   * LOBBY & GAME
   */

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCommand(PlayerCommandPreprocessEvent pcpe) {
        Player player = pcpe.getPlayer();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (game == null) {
            return;
        }

        if (game.getState() == GameState.STOPPED) {
            return;
        }

        String message = pcpe.getMessage();
        if (!message.startsWith("/bw")) {

            for (String allowed : Main.getInstance().getAllowedCommands()) {
                if (!allowed.startsWith("/")) {
                    allowed = "/" + allowed;
                }

                if (message.startsWith(allowed.trim())) {
                    return;
                }
            }

            if (player.hasPermission("bw.cmd")) {
                return;
            }

            pcpe.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onSleep(PlayerBedEnterEvent bee) {

        Player p = bee.getPlayer();

        Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
        if (g == null) {
            return;
        }

        if (g.getState() == GameState.STOPPED) {
            return;
        }

        bee.setCancelled(true);
    }

    @EventHandler
    public void onInteractEntity(PlayerInteractEntityEvent iee) {
        Player p = iee.getPlayer();
        Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
        if (g == null) {
            return;
        }

        if (g.getState() == GameState.WAITING) {
            iee.setCancelled(true);
            return;
        }

        if (g.getState() == GameState.RUNNING) {
            this.inGameInteractEntity(iee, g, p);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFly(PlayerToggleFlightEvent tfe) {
        Player p = tfe.getPlayer();

        Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
        if (g == null) {
            return;
        }

        if (g.getState() == GameState.STOPPED) {
            return;
        }

        if (g.getState() == GameState.RUNNING && g.spectator(p)) {
            tfe.setCancelled(false);
            return;
        }

        tfe.setCancelled(true);
    }

  /*
   * LOBBY
   */

    @EventHandler(priority = EventPriority.HIGH)
    public void onHunger(FoodLevelChangeEvent flce) {
        if (!(flce.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) flce.getEntity();
        Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (game == null) {
            return;
        }

        if (game.getState() == GameState.RUNNING) {
            if (game.spectator(player) || game.getCycle().isEndGameRunning()) {
                flce.setCancelled(true);
                return;
            }

            flce.setCancelled(false);
            return;
        }

        flce.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent pie) {
        Player player = pie.getPlayer();
        Game g = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (g == null) {
            if (pie.getAction() != Action.RIGHT_CLICK_BLOCK
                    && pie.getAction() != Action.RIGHT_CLICK_AIR) {
                return;
            }

            Block clicked = pie.getClickedBlock();

            if (clicked == null) {
                return;
            }

            if (!(clicked.getState() instanceof Sign)) {
                return;
            }

            Game game = Main.getInstance().getGameManager().getGameBySignLocation(clicked.getLocation());
            if (game == null) {
                return;
            }

            if (game.playerJoins(player)) {
                player.sendMessage(ChatHelper.with(ChatColor.GREEN + Main.local("success.joined")));
            }
            return;
        }

        if (g.getState() == GameState.STOPPED) {
            return;
        }

        Material interactingMaterial = pie.getMaterial();
        Block clickedBlock = pie.getClickedBlock();

        if (g.getState() == GameState.RUNNING) {
            if (pie.getAction() == Action.PHYSICAL && clickedBlock != null
                    && (clickedBlock.getType() == Material.WHEAT
                    || clickedBlock.getType() == Material.SOIL)) {
                pie.setCancelled(true);
                return;
            }

            if (pie.getAction() != Action.RIGHT_CLICK_BLOCK
                    && pie.getAction() != Action.RIGHT_CLICK_AIR) {
                return;
            }

            if (clickedBlock != null && clickedBlock.getType() == Material.LEVER && !g.spectator(player)
                    && pie.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!g.getRegion().isPlacedUnbreakableBlock(clickedBlock)) {
                    g.getRegion().addPlacedUnbreakableBlock(clickedBlock, clickedBlock.getState());
                }
                return;
            }

            if (g.spectator(player)
                    || (g.getCycle() instanceof BungeeGameCycle && g.getCycle().isEndGameRunning()
                    && Main.getInstance().getBooleanConfig("bungeecord.endgame-in-lobby", true))) {
                if (interactingMaterial == Material.SLIME_BALL) {
                    g.playerLeave(player, false);
                    return;
                }

                if (interactingMaterial == Material.COMPASS) {
                    g.openSpectatorCompass(player);
                    pie.setCancelled(true);
                    return;
                }
            }

            // Spectators want to block
            if (clickedBlock != null) {
                try {
                    GameMode.valueOf("SPECTATOR");
                } catch (Exception ex) {
                    Main.getInstance().getBugsnag().notify(ex);
                    for (Player p : g.getFree()) {
                        if (!g.getRegion().isInRegion(p.getLocation())) {
                            continue;
                        }

                        if (pie.getClickedBlock().getLocation().distance(p.getLocation()) < 2) {
                            Location oldLocation = p.getLocation();
                            if (oldLocation.getY() >= pie.getClickedBlock().getLocation().getY()) {
                                oldLocation.setY(oldLocation.getY() + 2);
                            } else {
                                oldLocation.setY(oldLocation.getY() - 2);
                            }

                            p.teleport(oldLocation);
                        }
                    }
                }
            }

            if (clickedBlock != null && clickedBlock.getType() == Material.ENDER_CHEST
                    && !g.spectator(player)) {
                pie.setCancelled(true);

                Block chest = pie.getClickedBlock();
                Team chestTeam = g.getTeamOfEnderChest(chest);
                Team playerTeam = g.getPlayerTeam(player);

                if (chestTeam == null) {
                    return;
                }

                if (chestTeam.equals(playerTeam)) {
                    player.openInventory(chestTeam.getInventory());
                } else {
                    player.sendMessage(
                            ChatHelper.with(ChatColor.RED + Main.local("ingame.noturteamchest")));
                }

                return;
            }

            return;
        } else if (g.getState() == GameState.WAITING) {
            if (interactingMaterial == null) {
                pie.setCancelled(true);
                return;
            }

            if (pie.getAction() == Action.PHYSICAL) {
                if (clickedBlock != null && (clickedBlock.getType() == Material.WHEAT
                        || clickedBlock.getType() == Material.SOIL)) {
                    pie.setCancelled(true);
                    return;
                }
            }

            if (pie.getAction() != Action.RIGHT_CLICK_BLOCK
                    && pie.getAction() != Action.RIGHT_CLICK_AIR) {
                return;
            }

            switch (interactingMaterial) {
                case BED:
                    pie.setCancelled(true);
                    if (!g.isAutobalanceEnabled()) {
                        g.getPlayerStorage(player).openTeamSelection(g);
                    }

                    break;
                case DIAMOND:
                    pie.setCancelled(true);
                    if (player.isOp() || player.hasPermission("bw.setup")) {
                        g.start(player);
                    } else if (player.hasPermission("bw.vip.forcestart")) {
                        if (g.isStartable()) {
                            g.start(player);
                        } else {
                            if (!g.hasEnoughPlayers()) {
                                player.sendMessage(ChatHelper.with(
                                        ChatColor.RED + Main.local("lobby.cancelstart.not_enough_players")));
                            } else if (!g.hasEnoughTeams()) {
                                player.sendMessage(ChatHelper
                                        .with(ChatColor.RED + Main.local("lobby.cancelstart.not_enough_teams")));
                            }
                        }
                    }
                    break;
                case EMERALD:
                    pie.setCancelled(true);
                    if ((player.isOp() || player.hasPermission("bw.setup")
                            || player.hasPermission("bw.vip.reducecountdown"))
                            && g.getGameLobbyCountdown().getCounter() > g.getGameLobbyCountdown()
                            .getLobbytimeWhenFull()) {
                        g.getGameLobbyCountdown().setCounter(g.getGameLobbyCountdown().getLobbytimeWhenFull());
                    }
                    break;
                case SLIME_BALL:
                    pie.setCancelled(true);
                    g.playerLeave(player, false);
                    break;
                case LEATHER_CHESTPLATE:
                    pie.setCancelled(true);
                    player.updateInventory();
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onLobbyInventoryClick(InventoryClickEvent ice, Player player, Game game) {
        Inventory inv = ice.getInventory();
        ItemStack clickedStack = ice.getCurrentItem();

        if (!inv.getTitle().equals(Main.local("lobby.chooseteam"))) {
            ice.setCancelled(true);
            return;
        }

        if (clickedStack == null) {
            ice.setCancelled(true);
            return;
        }

        if (clickedStack.getType() != Material.WOOL) {
            ice.setCancelled(true);
            return;
        }

        ice.setCancelled(true);
        Team team = game.getTeamByDyeColor(DyeColor.getByData(clickedStack.getData().getData()));
        if (team == null) {
            return;
        }

        game.playerJoinTeam(player, team);
        player.closeInventory();
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent die) {
        Player p = die.getPlayer();
        Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
        if (g == null) {
            return;
        }

        if (g.getState() != GameState.WAITING) {
            if (g.spectator(p)) {
                die.setCancelled(true);
            }

            return;
        }

        die.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent pqe) {
        Player player = pqe.getPlayer();

        if (Main.getInstance().isBungee()) {
            pqe.setQuitMessage(null);
        }

        // Remove holographs
        if (Main.getInstance().isHologramsEnabled()
                && Main.getInstance().getHolographicInteractor() != null && Main.getInstance()
                .getHolographicInteractor().getType().equalsIgnoreCase("HolographicDisplays")) {
            Main.getInstance().getHolographicInteractor().unloadAllHolograms(player);
        }

        Game g = Main.getInstance().getGameManager().getGameOfPlayer(player);

        if (g == null) {
            return;
        }

        g.playerLeave(player, false);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent ede) {
        if (!(ede.getEntity() instanceof Player)) {
            if (!(ede instanceof EntityDamageByEntityEvent)) {
                return;
            }

            EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) ede;
            if (edbee.getDamager() == null || !(edbee.getDamager() instanceof Player)) {
                return;
            }

            Player player = (Player) edbee.getDamager();
            Game game = Main.getInstance().getGameManager().getGameOfPlayer(player);

            if (game == null) {
                return;
            }

            if (game.getState() == GameState.WAITING) {
                ede.setCancelled(true);
            }

            return;
        }

        Player p = (Player) ede.getEntity();
        Game g = Main.getInstance().getGameManager().getGameOfPlayer(p);
        if (g == null) {
            return;
        }

        if (g.getState() == GameState.STOPPED) {
            return;
        }

        if (g.getState() == GameState.RUNNING) {
            if (g.spectator(p)) {
                ede.setCancelled(true);
                return;
            }

            if (g.isProtected(p) && ede.getCause() != DamageCause.VOID) {
                ede.setCancelled(true);
                return;
            }

            if (Main.getInstance().getBooleanConfig("die-on-void", false)
                    && ede.getCause() == DamageCause.VOID) {
                ede.setCancelled(true);
                p.setHealth(0);
                return;
            }

            if (ede instanceof EntityDamageByEntityEvent) {
                EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) ede;

                if (edbee.getDamager() instanceof Player) {
                    Player damager = (Player) edbee.getDamager();
                    if (g.spectator(damager)) {
                        ede.setCancelled(true);
                        return;
                    }

                    g.setPlayerDamager(p, damager);
                } else if (edbee.getDamager().getType().equals(EntityType.ARROW)) {
                    Arrow arrow = (Arrow) edbee.getDamager();
                    if (arrow.getShooter() instanceof Player) {
                        Player shooter = (Player) arrow.getShooter();
                        if (g.spectator(shooter)) {
                            ede.setCancelled(true);
                            return;
                        }

                        g.setPlayerDamager(p, (Player) arrow.getShooter());
                    }
                }
            }

            if (!g.getCycle().isEndGameRunning()) {
                return;
            } else if (ede.getCause() == DamageCause.VOID) {
                p.teleport(g.getPlayerTeam(p).getSpawnLocation());
            }
        } else if (g.getState() == GameState.WAITING
                && ede.getCause() == EntityDamageEvent.DamageCause.VOID) {
            p.teleport(g.getLobby());
        }

        ede.setCancelled(true);
    }

}
