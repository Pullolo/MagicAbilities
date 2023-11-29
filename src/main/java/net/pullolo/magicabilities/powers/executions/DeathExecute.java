package net.pullolo.magicabilities.powers.executions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathExecute extends Execute{
    public DeathExecute(PlayerDeathEvent event, Player player) {
        super(event, player);
    }
}
