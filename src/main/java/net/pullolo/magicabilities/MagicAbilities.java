package net.pullolo.magicabilities;

import net.pullolo.magicabilities.data.DbManager;
import net.pullolo.magicabilities.data.PlayerData;
import net.pullolo.magicabilities.guis.AnimationManager;
import net.pullolo.magicabilities.guis.GuiManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;

public final class MagicAbilities extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    private DbManager dbManager;
    private static FileConfiguration config;

    @Override
    public void onEnable() {
        config = getConfig();
        saveDefaultConfig();
        dbManager = new DbManager(this);
        dbManager.init();
        checkDb(dbManager);
        setPlayerData(dbManager);
        final GuiManager guiManager = new GuiManager(this);
        final AnimationManager animationManager = new AnimationManager(this, guiManager);
    }

    @Override
    public void onDisable() {
        dbManager.disconnect();
        savePlayers(dbManager);
    }

    public static Logger getLog(){
        return log;
    }

    public static void debugLog(String msg, boolean warning){
        if (!config.getBoolean("debug")){
            return;
        }
        if (warning) log.warning("[MagicAbilities:Debug] " + msg);
        else log.info("[MagicAbilities:Debug] " + msg);
    }
    private void checkDb(DbManager db){
        db.connect();
        if (db.isDbEnabled()){
            log.info("Database is operational");
        } else log.warning("Database is offline!");
    }
    private void setPlayerData(DbManager db){
        for (Player p : getServer().getOnlinePlayers()){
            PlayerData.setPlayerDataFromDb(p); //, db
            //QuestManager.getPlayerQuestsOnJoin(p);
        }
    }

    private void savePlayers(DbManager db){
        for (Player p : getServer().getOnlinePlayers()){
//            QuestManager.savePlayerQuestsOnLeave(p);
            PlayerData.savePlayerDataToDb(p); //, db
            PlayerData.removePlayerData(p);
        }
    }
}
