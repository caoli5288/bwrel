package io.github.bedwarsrel.BedwarsRel.Shop.Specials;

import io.github.bedwarsrel.BedwarsRel.Game.Game;
import io.github.bedwarsrel.BedwarsRel.Game.GameState;
import io.github.bedwarsrel.BedwarsRel.Main;
import io.github.bedwarsrel.BedwarsRel.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

public class MagnetShoeListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(EntityDamageByEntityEvent ev) {
        if (ev.isCancelled()) {
            return;
        }

        if (!(ev.getEntity() instanceof Player)) {
            return;
        }

        Game game = Main.getInstance().getGameManager().getGameOfPlayer((Player) ev.getEntity());
        if (game == null) {
            return;
        }

        if (game.getState() != GameState.RUNNING) {
            return;
        }

        Player player = (Player) ev.getEntity();
        ItemStack boots = player.getInventory().getBoots();

        if (boots == null) {
            return;
        }

        MagnetShoe shoe = new MagnetShoe();
        if (boots.getType() != shoe.getItemMaterial()) {
            return;
        }

        if (this.rollKnockbackDice()) {
            ev.setCancelled(true);
            player.damage(ev.getDamage());
        }
    }

    private boolean rollKnockbackDice() {
        int target = Main.getInstance().getIntConfig("specials.magnetshoe.probability", 75);
        int roll = Utils.randInt(0, 100);

        return (roll <= target);
    }

}
