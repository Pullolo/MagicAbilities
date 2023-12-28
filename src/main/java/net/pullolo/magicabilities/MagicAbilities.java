package net.pullolo.magicabilities;

import net.pullolo.magicabilities.commands.*;
import net.pullolo.magicabilities.data.DataEventsHandler;
import net.pullolo.magicabilities.data.DbManager;
import net.pullolo.magicabilities.data.PlayerData;
import net.pullolo.magicabilities.events.ExecutionEvents;
import net.pullolo.magicabilities.guis.AnimationManager;
import net.pullolo.magicabilities.guis.GuiManager;
import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.misc.ParticleApi;
import net.pullolo.magicabilities.players.PowerPlayer;
import net.pullolo.magicabilities.powers.Power;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public final class MagicAbilities extends JavaPlugin {

    private static final Logger log = Logger.getLogger("Minecraft");
    public static ParticleApi particleApi;
    private DbManager dbManager;
    private static FileConfiguration config;
    public static JavaPlugin magicPlugin;

    @Override
    public void onEnable() {
        magicPlugin=this;
        config = getConfig();
        saveDefaultConfig();
        particleApi = new ParticleApi(this);
        dbManager = new DbManager(this);
        dbManager.init();
        checkDb(dbManager);
        setPlayerData(dbManager);
        createCooldowns();
        getServer().getPluginManager().registerEvents(new DataEventsHandler(dbManager), this);
        getServer().getPluginManager().registerEvents(new ExecutionEvents(), this);
        registerCommand(new Binds(), "binds");
        registerCommand(new Destination(), "destination");
        registerCommand(new Setpower(), "setpower");
        registerCommand(new Enable(), "enable");
        registerCommand(new Disable(), "disable");
        final GuiManager guiManager = new GuiManager(this);
        final AnimationManager animationManager = new AnimationManager(this, guiManager);
    }

    @Override
    public void onDisable() {
        dbManager.disconnect();
        savePlayers(dbManager);
    }

    private void registerCommand(CommandExecutor cmd, String cmdName){
        if (cmd instanceof TabCompleter){
            getCommand(cmdName).setExecutor(cmd);
            getCommand(cmdName).setTabCompleter((TabCompleter) cmd);
        } else {
            throw new RuntimeException("Provided object is not a command executor and a tab completer at the same time!");
        }
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
            PlayerData.setPlayerDataFromDb(p, db);
            new PowerPlayer(Power.getPowerFromPowerType(p, getPlayerData(p).getPower()), getPlayerData(p).getBinds(), getPlayerData(p).isEnabled());
            //QuestManager.getPlayerQuestsOnJoin(p);
        }
    }

    private void savePlayers(DbManager db){
        for (Player p : getServer().getOnlinePlayers()){
//          QuestManager.savePlayerQuestsOnLeave(p);
            PlayerData.savePlayerDataToDb(p, db);
            PlayerData.removePlayerData(p);
            players.get(p).remove();
            players.remove(p);
        }
    }

    private void createCooldowns(){
        CooldownApi.createCooldown("ICE-DEF", 1);
        CooldownApi.createCooldown("ICE-1", 6.5);
        CooldownApi.createCooldown("ICE-2", 1.6);
        CooldownApi.createCooldown("ICE-3", 10);
        CooldownApi.createCooldown("ICE-4", 3);
        CooldownApi.createCooldown("ICE-5", 12);
        CooldownApi.createCooldown("ICE-8", 5);
        CooldownApi.createCooldown("WARP-DEF", 180);
        CooldownApi.createCooldown("LIG-1", 20);
        CooldownApi.createCooldown("LIG-2", 4);
        CooldownApi.createCooldown("LIG-3", 15);
        CooldownApi.createCooldown("LIG-4", 20);
        CooldownApi.createCooldown("LIG-PAS", 30);
        CooldownApi.createCooldown("UNS-H1", 300);
        CooldownApi.createCooldown("DJ", 6);
        CooldownApi.createCooldown("SHOGUN-AB0", 12);
        CooldownApi.createCooldown("POTATO-0", 30);
        CooldownApi.createCooldown("POTATO-1", 0.5);
        CooldownApi.createCooldown("FIRE-0", 2);
        CooldownApi.createCooldown("FIRE-1", 4);
        CooldownApi.createCooldown("FIRE-2", 6);
        CooldownApi.createCooldown("WITCHER-0", 3);
        CooldownApi.createCooldown("WITCHER-1", 6);
        CooldownApi.createCooldown("WITCHER-2", 10);
        CooldownApi.createCooldown("WITCHER-3", 10);
        CooldownApi.createCooldown("WITCHER-4", 18);
        CooldownApi.createCooldown("NATURE-0", 10);
        CooldownApi.createCooldown("TM-0", 180);
    }
}
