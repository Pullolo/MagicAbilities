package net.pullolo.magicabilities.powers.executions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class RightClickExecute extends Execute{
    public RightClickExecute(PlayerInteractEvent event, Player player) {
        super(event, player);
    }
}
