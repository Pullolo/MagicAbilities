package net.pullolo.magicabilities.data;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerData {
    private static final HashMap<Player, PlayerData> playerData = new HashMap<>();
    private final String name;

    public PlayerData(String name) {
        this.name = name;
    }
    public static PlayerData getPlayerData(Player p){
        return playerData.get(p);
    }
    //todo add onJoin
    public static void setPlayerDataFromDb(Player p){ //, DbManager db
        String playerName = p.getName();
        //playerData.put(p, db.getPlayerData(playerName));
    }
    public static void savePlayerDataToDb(Player p){ //, DbManager db
        String playerName = p.getName();
        //db.updatePlayer(playerName, playerData.get(p).getLevel(), playerData.get(p).getXp(), playerData.get(p).getStarEssence(), playerData.get(p).getWishes(), playerData.get(p).getDungeonEssence(), playerData.get(p).isUpdated());
    }
    public static void removePlayerData(Player p){
        playerData.remove(p);
    }
    public String getName() {
        return name;
    }
}
