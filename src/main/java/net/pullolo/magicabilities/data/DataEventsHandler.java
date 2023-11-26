package net.pullolo.magicabilities.data;

import net.pullolo.magicabilities.players.PowerPlayer;
import net.pullolo.magicabilities.powers.Power;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;

import static net.pullolo.magicabilities.MagicAbilities.debugLog;
import static net.pullolo.magicabilities.data.PlayerData.*;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class DataEventsHandler implements Listener {
    private final DbManager dbManager;

    public DataEventsHandler(DbManager dbManager) {
        this.dbManager = dbManager;
    }

    @EventHandler
    public void onSlotSwap(PlayerItemHeldEvent event){
        if (!players.containsKey(event.getPlayer())){
            return;
        }
        players.get(event.getPlayer()).setActiveSlot(event.getNewSlot());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        if (event.getPlayer().getName().contains(" ")){
            event.getPlayer().kickPlayer("Invalid Name!");
        }
        setPlayerDataFromDb(event.getPlayer(), dbManager);
        new PowerPlayer(Power.getPowerFromPowerType(event.getPlayer(), getPlayerData(event.getPlayer()).getPower()));
        //getPlayerQuestsOnJoin(event.getPlayer());
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event){
        //savePlayerQuestsOnLeave(event.getPlayer());
        savePlayerDataToDb(event.getPlayer(), dbManager);
        removePlayerData(event.getPlayer());
        players.get(event.getPlayer()).remove();
        players.remove(event.getPlayer());
    }
}
