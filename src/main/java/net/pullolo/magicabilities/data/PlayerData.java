package net.pullolo.magicabilities.data;

import net.pullolo.magicabilities.powers.Power;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerData {
    private static final HashMap<Player, PlayerData> playerData = new HashMap<>();
    private final String name;
    private Power power;

    public PlayerData(String name, Power power) {
        this.name = name;
        this.power = power;
    }
    public static PlayerData getPlayerData(Player p){
        return playerData.get(p);
    }
    public static void setPlayerDataFromDb(Player p, DbManager db){
        String playerName = p.getName();
        playerData.put(p, db.getPlayerData(playerName));
    }
    public static void savePlayerDataToDb(Player p, DbManager db){
        String playerName = p.getName();
        db.updatePlayer(playerName, playerData.get(p));
    }
    public static void removePlayerData(Player p){
        playerData.remove(p);
    }
    public String getName() {
        return name;
    }

    public Power getPower() {
        return power;
    }

    public void setPower(Power power) {
        this.power = power;
    }
}
