package net.pullolo.magicabilities.powers.executions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DealDamageExecute extends Execute{
    public DealDamageExecute(EntityDamageByEntityEvent event, Player player) {
        super(event, player);
    }
}
