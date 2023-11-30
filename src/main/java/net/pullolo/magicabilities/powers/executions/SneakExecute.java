package net.pullolo.magicabilities.powers.executions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;

public class SneakExecute extends Execute{
    public SneakExecute(PlayerToggleSneakEvent event, Player player) {
        super(event, player);
    }
}
