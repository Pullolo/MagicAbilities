package net.pullolo.magicabilities.data;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static net.pullolo.magicabilities.data.PlayerData.*;

public class DataEventsHandler implements Listener {
    private final DbManager dbManager;

    public DataEventsHandler(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if (event.getPlayer().getName().contains(" ")){
            event.getPlayer().kickPlayer("Invalid Name!");
        }
        setPlayerDataFromDb(event.getPlayer(), dbManager);
        //getPlayerQuestsOnJoin(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        //savePlayerQuestsOnLeave(event.getPlayer());
        savePlayerDataToDb(event.getPlayer(), dbManager);
        removePlayerData(event.getPlayer());
    }
}
