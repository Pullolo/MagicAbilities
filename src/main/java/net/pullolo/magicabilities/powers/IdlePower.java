package net.pullolo.magicabilities.powers;

import net.pullolo.magicabilities.powers.executions.IdleExecute;
import org.bukkit.scheduler.BukkitRunnable;

public interface IdlePower {
    BukkitRunnable executeIdle(IdleExecute ex);
}
