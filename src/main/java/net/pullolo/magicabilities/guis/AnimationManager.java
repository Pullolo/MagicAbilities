package net.pullolo.magicabilities.guis;

import de.themoep.inventorygui.InventoryGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class AnimationManager {
    public static final HashMap<Player, Boolean> skipAnim = new HashMap<>();

    private final GuiManager guis;
    private final JavaPlugin plugin;
    public AnimationManager(JavaPlugin plugin, GuiManager guis){
        this.plugin = plugin;
        this.guis = guis;
    }

    public static void skipAnimation(Player p){
        if (skipAnim.containsKey(p)){
            skipAnim.replace(p, true);
        }
    }
}
