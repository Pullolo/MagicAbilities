package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.ConsumeExecute;
import net.pullolo.magicabilities.powers.executions.Execute;
import net.pullolo.magicabilities.powers.executions.IdleExecute;
import net.pullolo.magicabilities.powers.executions.MineExecute;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Zombie;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;

public class AlcoholizmPower extends Power implements IdlePower {
    private boolean drunk = false;
    public AlcoholizmPower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof MineExecute){
            onMine((MineExecute) ex);
            return;
        }
        if (ex instanceof ConsumeExecute){
            onConsume(ex.getPlayer(), ((PlayerItemConsumeEvent) ex.getRawEvent()).getItem());
            return;
        }
    }

    private void onConsume(Player p, ItemStack i){
        if (i.getType().equals(Material.POTION)){
            if (i.getItemMeta() instanceof PotionMeta){
                if(((PotionMeta) i.getItemMeta()).getBasePotionType().equals(PotionType.AWKWARD)){
                    if (new Random().nextInt(20)==0){
                        for (int ii = 0; ii<20; ii++){
                            p.getWorld().spawn(p.getLocation(), Zombie.class);
                        }
                    }
                    drunk=true;
                    p.getActivePotionEffects().clear();
                    p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20*1500, 4));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 20*60, 4));
                    p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 20*1500, 2));
                    if (new Random().nextInt(10)==0) p.setVelocity(p.getLocation().getDirection().clone().normalize().multiply(3));
                }
            }
        } else if (new Random().nextInt(25)==0){
            p.getWorld().spawn(p.getLocation(), TNTPrimed.class, (en)->{
                en.setFuseTicks(40);
            });
        }
    }

    private void onMine(MineExecute execute){
        Player p = execute.getPlayer();
        if (new Random().nextInt(10)==0){
            if (p.getFoodLevel()>0){
                p.setFoodLevel(p.getFoodLevel()-1);
            }
        }
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (p.getFoodLevel()>=16){
                    if (!drunk){
                        p.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 41, 0));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 41, 0));
                    }
                }
                for (PotionEffect pot : p.getActivePotionEffects()){
                    if (pot.getType().equals(PotionEffectType.STRENGTH)){
                        return;
                    }
                }
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 41, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, 41, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 41, 0));
            }
        };
        r.runTaskTimer(magicPlugin, 0, 40);
        return r;
    }
}
