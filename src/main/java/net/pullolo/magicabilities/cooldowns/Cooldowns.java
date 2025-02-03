package net.pullolo.magicabilities.cooldowns;

import org.bukkit.Bukkit;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;

public class Cooldowns {
    public static Cooldowns cooldowns = null;
    private static final HashMap<String, Double> cds = new HashMap<>();
    private final FileConfiguration config;

    public Cooldowns(FileConfiguration config) {
        if (cooldowns!=null) throw new RuntimeException("Cooldowns instance already exists!");
        cooldowns = this;
        this.config=config;
        try {
            createCooldowns();
        } catch (Exception e){
            Bukkit.getServer().getLogger().warning("Couldn't create cooldowns!");
        }
    }

    private void createCooldowns(){
        for (String s: config.getKeys(false)){
            for (String key: ((MemorySection) config.get(s)).getKeys(false)){
                String fullKey = s+"."+key;
                if (cds.containsKey(fullKey)) cds.replace(fullKey, config.getDouble(fullKey));
                else cds.put(fullKey, config.getDouble(fullKey));
            }
        }
        for (String s : cds.keySet()){
            CooldownApi.createCooldown(s, cds.get(s));
        }
    }

    public Double get(String s){
        return cds.get(s);
    }

    public boolean containsKey(String s){
        return cds.containsKey(s);
    }
}

