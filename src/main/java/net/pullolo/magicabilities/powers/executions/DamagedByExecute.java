package net.pullolo.magicabilities.powers.executions;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class DamagedByExecute extends Execute{
    public DamagedByExecute(EntityDamageByEntityEvent event, Player player) {
        super(event, player);
    }
}
