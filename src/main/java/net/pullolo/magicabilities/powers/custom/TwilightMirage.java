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
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class TwilightMirage extends Power implements IdlePower {

    public TwilightMirage(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof DamagedByExecute){
            onDamagedBy((DamagedByExecute) ex);
            return;
        }
        if (ex instanceof DamagedExecute){
            onDamaged((DamagedExecute) ex);
            return;
        }
        if (ex instanceof DealDamageExecute){
            onDamage((DealDamageExecute) ex);
            return;
        }
        if (!isEnabled()) return;
        if (ex instanceof LeftClickExecute){
            executeLeftClick((LeftClickExecute) ex);
            return;
        }
    }

    private void onDamagedBy(DamagedByExecute execute){
        final Player p = execute.getPlayer();
        final EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) execute.getRawEvent();
        final Entity damager = event.getDamager();
        if (new Random().nextInt(4)==0){
            event.setCancelled(true);
            Vector v = damager.getLocation().clone().toVector().subtract(p.getLocation().clone().toVector()).normalize();
            Location toDisplay = p.getEyeLocation().clone().add(v);
            particleApi.spawnParticles(toDisplay, Particle.GUST, 1, 0, 0,0,1);
            p.getWorld().playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 0.8f);
            p.setVelocity(p.getLocation().getDirection().clone().normalize().multiply(-0.4));
            return;
        }
    }

    private void executeLeftClick(LeftClickExecute execute){
        final Player p = execute.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 0:
                if (CooldownApi.isOnCooldown("TM-1", p)) return;
                shriekTransition(p);
                CooldownApi.addCooldown("TM-1", p, 20);
                return;
            case 1:
                if (CooldownApi.isOnCooldown("TM-2", p)) return;
                twilightFloat(p);
                CooldownApi.addCooldown("TM-2", p, 10);
                return;
        }
    }

    private void twilightFloat(Player p){
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1, 1.3f);
        p.setVelocity(new Vector(0, 1, 0).normalize().multiply(2));
        particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0), Particle.GLOW,
                60, 1, 1, 1, 0.6);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 100, 0));
    }

    private void shriekTransition(Player p){
        int i = 20;
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_CHARGE, 1, 2);
        Location original = p.getLocation().clone();
        Location l = p.getLocation().clone().add(0, 1, 0);
        Vector v = p.getLocation().getDirection().clone().normalize();
        while (l.clone().add(v).getBlock().isPassable() && l.clone().add(v).add(0, 1, 0).getBlock().isPassable() && i>0){
            l.add(v);
            i--;
        }
        p.teleport(l);
        HashMap<Particle, Double> particles = new HashMap<>();
        particles.put(Particle.SONIC_BOOM, 1.0);
        for (Entity e : particleApi.drawMultiParticleLineWRTO(original, l, 0.08, particles, 0, 10)){
            if (!(e instanceof LivingEntity)) continue;
            if (e.equals(p)) continue;
            ((LivingEntity) e).damage(isNight(p) ? 22 : 10, p);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WARDEN_SONIC_BOOM, 1, 1.4f);
    }

    private void onDamage(DealDamageExecute execute){
        final Player p = execute.getPlayer();
        EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) execute.getRawEvent();
        final Entity e = event.getEntity();
        if (new Random().nextInt(3)==0 && e instanceof LivingEntity){
            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 300, 0));
        }
        if (e instanceof Warden){
            event.setDamage(((Warden) e).getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue()/2);
            return;
        }
    }

    private void onDeath(DamagedExecute execute){
        if (!(new Random().nextInt(10)==0)) return;
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
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 1));
                return;
            }
        }
        if (event.getFinalDamage()>=p.getHealth()){
            if (CooldownApi.isOnCooldown("TM-0", p)){
                if (!event.isCancelled()) onDeath(execute);
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
                if (p.getLocation().getBlock().getLightLevel()>14){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 0));
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
                return "&9Shriek Transition";
            case 1:
                return "&9Twilight Float";
            default:
                return "&7none";
        }
    }
}
