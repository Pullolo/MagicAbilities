package net.pullolo.magicabilities.powers.executions;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class Execute {
    private final Player player;
    private final Event rawEvent;

    public Execute(Event event, Player player) {
        this.rawEvent = event;
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Event getRawEvent() {
        return rawEvent;
    }
}
