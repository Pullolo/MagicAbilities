package net.pullolo.magicabilities.misc;

import com.google.common.cache.CacheBuilder;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CooldownApi {
    private static Map<String, Map> cooldown = new ConcurrentHashMap();

    public CooldownApi() {
    }

    public static void createCooldown(String k, double defaultTime) {
        if (cooldown.containsKey(k)) {
            throw new IllegalArgumentException("Cooldown already exists.");
        } else {
            cooldown.put(k, CacheBuilder.newBuilder().expireAfterWrite((long) (defaultTime*1000), TimeUnit.MILLISECONDS).build().asMap());
        }
    }

    public static Map getCooldownMap(String k) {
        return cooldown.containsKey(k) ? (Map) cooldown.get(k) : null;
    }

    public static void addCooldown(String k, Player p, double seconds) {
        if (!cooldown.containsKey(k)) {
            throw new IllegalArgumentException(k + " does not exist");
        } else {
            long next = (long) (System.currentTimeMillis() + seconds * 1000L);
            ((Map) cooldown.get(k)).put(p.getUniqueId(), next);
        }
    }

    public static boolean isOnCooldown(String k, Player p, long now) {
        return cooldown.containsKey(k) && ((Map) cooldown.get(k)).containsKey(p.getUniqueId()) && now <= (Long) ((Map) cooldown.get(k)).get(p.getUniqueId());
    }

    public static boolean isOnCooldown(String k, Player p) {
        return cooldown.containsKey(k) && ((Map) cooldown.get(k)).containsKey(p.getUniqueId()) && System.currentTimeMillis() <= (Long) ((Map) cooldown.get(k)).get(p.getUniqueId());
    }

    public static int getCooldownForPlayerInt(String k, Player p, long now) {
        return (int) ((Long) ((Map) cooldown.get(k)).get(p.getUniqueId()) - now) / 1000;
    }

    public static int getCooldownForPlayerInt(String k, Player p) {
        return (int) ((Long) ((Map) cooldown.get(k)).get(p.getUniqueId()) - System.currentTimeMillis()) / 1000;
    }

    public static long getCooldownForPlayerLong(String k, Player p) {
        return (long) ((int) ((Long) ((Map) cooldown.get(k)).get(p.getUniqueId()) - System.currentTimeMillis()));
    }

    public static long getCooldownForPlayerLong(String k, Player p, long now) {
        return (Long) ((Map) cooldown.get(k)).get(p.getUniqueId()) - now;
    }

    public static void removeCooldown(String k, Player p) {
        if (!cooldown.containsKey(k)) {
            throw new IllegalArgumentException(k + " does not exist");
        } else {
            ((Map) cooldown.get(k)).remove(p.getUniqueId());
        }
    }
}
