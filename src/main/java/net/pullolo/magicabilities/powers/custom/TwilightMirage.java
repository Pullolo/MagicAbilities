package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Warden;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;

public class TwilightMirage extends Power implements IdlePower {

    public TwilightMirage(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof DamagedExecute){
            onDamaged((DamagedExecute) ex);
            return;
        }
        if (ex instanceof DealDamageExecute){
            onDamage((DealDamageExecute) ex);
            return;
        }
        if (!isEnabled()) return;

    }

    private void onDamage(DealDamageExecute execute){
        final Player p = execute.getPlayer();
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) execute.getRawEvent();
        final Entity e = event.getEntity();
        if (e instanceof Warden){
            event.setDamage(((Warden) e).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/2);
            return;
        }
    }

    private void onDeath(DamagedExecute execute){
        final Player p = execute.getPlayer();
        EntityDamageEvent event = (EntityDamageEvent) execute.getRawEvent();
        Warden w = p.getWorld().spawn(p.getLocation(), Warden.class);
        particleApi.spawnParticles(p.getLocation(), Particle.GLOW,
                100, 1, 1, 1, 0.3);
        if (event instanceof EntityDamageByEntityEvent){
            w.setAnger(((EntityDamageByEntityEvent) event).getDamager(), 140);
        }
    }

    private void onDamaged(DamagedExecute execute){
        final Player p = execute.getPlayer();
        final EntityDamageEvent event = (EntityDamageEvent) execute.getRawEvent();
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)){
            if (p.getLocation().getBlock().getLightLevel()<13){
                event.setCancelled(true);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 2, 2);
                particleApi.spawnParticles(p.getLocation().clone().add(0, 0.5, 0),
                        Particle.GUST, 1, 0, 0, 0, 0.1);
                return;
            }
        }
        if (event.getFinalDamage()>=p.getHealth()){
            if (CooldownApi.isOnCooldown("TM-0", p)){
                onDeath(execute);
                return;
            }
            CooldownApi.addCooldown("TM-0", p, 180);
            event.setCancelled(true);
            if (isNight(p)) {
                p.setHealth(6);
            } else {
                p.setHealth(4);
            }
            if (Bukkit.getOnlinePlayers().size()>1){
                ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                Collections.shuffle(players);
                for (Player player : players){
                    if (!player.equals(p)){
                        p.teleport(player);
                        break;
                    }
                }
            }
            p.setVelocity(p.getLocation().getDirection().clone().normalize().multiply(-0.4));
            p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
            particleApi.spawnParticles(p.getLocation(), Particle.TOTEM,
                    100, 1, 1, 1, 0.6);
            return;
        }
    }

    private boolean isNight(Player p){
        return !(p.getWorld().getTime()<12300 || p.getWorld().getTime()>23850);
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isInWater() && !isNight(p)){
                    p.damage(0.5);
                }
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.SCULK_SOUL, 4, 0.6, 0.6, 0.6, 0.01);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 15);
        return r;
    }

    @Override
    public String getAbilityName(int ability) {
        switch (ability){
            case 0:
                return "";
            default:
                return "&7none";
        }
    }
}
