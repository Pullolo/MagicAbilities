package net.pullolo.magicabilities.guis;

import de.themoep.inventorygui.InventoryGui;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

import static net.pullolo.magicabilities.guis.AnimationManager.skipAnim;

public class AnimationBuilder {

    private final JavaPlugin plugin;
    private int animPeriod = 10;
    private final ArrayList<InventoryGui> animGuis;
    private final Player player;

    public AnimationBuilder(JavaPlugin plugin, Player player, ArrayList<InventoryGui> animGuis){
        this.plugin=plugin;
        this.animGuis=animGuis;
        this.player=player;
    }

    public AnimationBuilder setAnimationPeriod(int period){
        this.animPeriod=period;
        return this;
    }

    public void run(){
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (!(player!=null && skipAnim.get(player)!=null && !skipAnim.get(player))){
                    removePlayerFromAnimation(player);
                    this.cancel();
                    return;
                }
                if (i!=0) animGuis.get(i-1).close(player);
                animGuis.get(i).show(player);
                i++;
                if (i > animGuis.size()-1) {
                    removePlayerFromAnimation(player);
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0, animPeriod);
    }

    private void addPlayerToAnimation(Player p){
        skipAnim.put(p, false);
    }

    private void removePlayerFromAnimation(Player p){
        skipAnim.remove(p);
    }
}
