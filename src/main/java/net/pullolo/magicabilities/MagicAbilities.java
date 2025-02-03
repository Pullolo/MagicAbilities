package net.pullolo.magicabilities;

import net.pullolo.magicabilities.commands.*;
import net.pullolo.magicabilities.cooldowns.Cooldowns;
import net.pullolo.magicabilities.data.DataEventsHandler;
import net.pullolo.magicabilities.data.DbManager;
import net.pullolo.magicabilities.data.PlayerData;
import net.pullolo.magicabilities.events.ExecutionEvents;
import net.pullolo.magicabilities.guis.AnimationManager;
import net.pullolo.magicabilities.guis.GuiManager;
import net.pullolo.magicabilities.cooldowns.CooldownApi;
import net.pullolo.magicabilities.misc.ParticleApi;
import net.pullolo.magicabilities.players.PowerPlayer;
import net.pullolo.magicabilities.powers.Power;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
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
        saveDefaultConfig();
        config = getConfig();
        new Cooldowns(createCooldownsConfig());
        particleApi = new ParticleApi(this);
        dbManager = new DbManager(this);
        dbManager.init();
        checkDb(dbManager);
        setPlayerData(dbManager);
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
        }
    }

    private void savePlayers(DbManager db){
        for (Player p : getServer().getOnlinePlayers()){
            PlayerData.savePlayerDataToDb(p, db);
            PlayerData.removePlayerData(p);
            players.get(p).remove();
            players.remove(p);
        }
    }

    private FileConfiguration createCooldownsConfig() {
        File customConfigFile = new File(getDataFolder(), "cooldowns.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("cooldowns.yml", false);
        }

        return YamlConfiguration.loadConfiguration(customConfigFile);
    }
}
