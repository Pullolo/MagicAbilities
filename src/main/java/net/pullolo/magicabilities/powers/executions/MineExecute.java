package net.pullolo.magicabilities.powers.executions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

public class MineExecute extends Execute{
    public MineExecute(BlockBreakEvent event, Player player) {
        super(event, player);
    }
}
