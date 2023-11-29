package net.pullolo.magicabilities.events;

import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;

import static net.pullolo.magicabilities.MagicAbilities.debugLog;
import static net.pullolo.magicabilities.MagicAbilities.getLog;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class ExecutionEvents implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event){
        Player p = event.getPlayer();
        if (!players.containsKey(p)){
            return;
        }
        if (event.getHand()==null || !event.getHand().equals(EquipmentSlot.HAND)){
            return;
        }
        Power pow = players.get(p).getPower();
        if (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.LEFT_CLICK_AIR)){
            pow.executePower(new LeftClickExecute(event, p));
            return;
        }
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_AIR)){
            pow.executePower(new RightClickExecute(event, p));
            return;
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event){
        Player p = event.getPlayer();
        if (!players.containsKey(p)){
            return;
        }
        players.get(p).getPower().executePower(new MoveExecute(event, p));
    }

    @EventHandler
    public void onDamagedBy(EntityDamageByEntityEvent event){
        if (!(event.getEntity() instanceof Player)){
            return;
        }
        Player p = (Player) event.getEntity();
        if (!players.containsKey(p)){
            return;
        }
        players.get(p).getPower().executePower(new DamagedByExecute(event, p));
    }

    @EventHandler
    public void onDamageDealt(EntityDamageByEntityEvent event){
        if (!(event.getDamager() instanceof Player)){
            return;
        }
        Player p = (Player) event.getDamager();
        if (!players.containsKey(p)){
            return;
        }
        players.get(p).getPower().executePower(new DealDamageExecute(event, p));
    }

    @EventHandler
    public void onDamaged(EntityDamageEvent event){
        if (!(event.getEntity() instanceof Player)){
            return;
        }
        Player p = (Player) event.getEntity();
        if (!players.containsKey(p)){
            return;
        }
        players.get(p).getPower().executePower(new DamagedExecute(event, p));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player p = event.getEntity();
        if (!players.containsKey(p)){
            return;
        }
        players.get(p).getPower().executePower(new DeathExecute(event, p));
    }

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event){
        Player p = event.getPlayer();
        if (!players.containsKey(p)){
            return;
        }
        players.get(p).getPower().executePower(new ConsumeExecute(event, p));
    }

    @EventHandler
    public void onEnClick(PlayerInteractEntityEvent event){
        if (!(event.getRightClicked() instanceof Player)){
            return;
        }
        Player target = (Player) event.getRightClicked();
        if (!players.containsKey(target)){
            return;
        }
        players.get(target).getPower().executePower(new InteractedOnByExecute(event, target));
    }
}
