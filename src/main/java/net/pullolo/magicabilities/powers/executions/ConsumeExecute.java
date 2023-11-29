package net.pullolo.magicabilities.powers.executions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class ConsumeExecute extends Execute{
    public ConsumeExecute(PlayerItemConsumeEvent event, Player player) {
        super(event, player);
    }
}
