package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.misc.GeneralMethods.rotateVector;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class ShogunPower extends Power implements IdlePower {
    public ShogunPower(Player owner) {
        super(owner);
        owner.setAllowFlight(true);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof RightClickExecute){
            executeRightClick((RightClickExecute) ex);
            return;
        }
        if (ex instanceof DamagedByExecute){
            block((DamagedByExecute) ex);
            return;
        }
        if (ex instanceof SneakExecute){
            executeDoubleJump((SneakExecute) ex);
            return;
        }
        if (ex instanceof DamagedExecute){
            damagedExecute((DamagedExecute) ex);
            return;
        }
    }

    private void executeRightClick(RightClickExecute ex) {
        final Player player = ex.getPlayer();
        if (!player.getInventory().getItemInMainHand().getType().toString().toLowerCase().contains("sword")) return;
        if(CooldownApi.isOnCooldown("SHOGUN-AB0", player)) return;
        dash(player);
        CooldownApi.addCooldown("SHOGUN-AB0", player, 12);
    }

    private void dash(Player p){
        p.setVelocity(p.getLocation().getDirection().normalize().multiply(2).setY(0.3));
        BukkitRunnable r = new BukkitRunnable() {
            final ArrayList<Entity> alreadyDamaged = new ArrayList<>();
            int i = 0;
            @Override
            public void run() {
                i++;

                for (Entity entity : p.getNearbyEntities(1, 1, 1)){
                    if (!entity.equals(p) && !alreadyDamaged.contains(entity)){
                        if (entity instanceof LivingEntity){
                            ((LivingEntity) entity).damage(20, p);
                            alreadyDamaged.add(entity);
                        }
                    }
                }

                if (i>14){
                    cancel();
                }
            }
        };
        r.runTaskTimer(magicPlugin, 0, 1);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1F, 0.8F);
    }

    private void damagedExecute(DamagedExecute execute){
        final EntityDamageEvent event = (EntityDamageEvent) execute.getRawEvent();
        if (!event.getCause().equals(EntityDamageEvent.DamageCause.FALL)) return;
        event.setCancelled(true);
        spawnParticles(execute.getPlayer());
    }

    private void executeDoubleJump(SneakExecute execute){
        final Player p = execute.getPlayer();
        if (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())!=0) return;
        if (CooldownApi.isOnCooldown("DJ", p)) return;
        p.setVelocity(p.getLocation().getDirection().clone().normalize().multiply(1.4).add(new Vector(0, 0.3, 0)));
        spawnParticles(p);
        CooldownApi.addCooldown("DJ", p, 6);
        return;
    }

    private void spawnParticles(Player p){
        particleApi.spawnParticles(p.getLocation().clone().add(0, 0.3, 0), Particle.SMOKE_NORMAL, 30, 0.1, 0.05, 0.1, 0.1);
    }

    private void block(DamagedByExecute execute){
        if (new Random().nextBoolean()) return;
        final Player player = execute.getPlayer();
        if (!player.getInventory().getItemInMainHand().getType().toString().toLowerCase().contains("sword")) return;
        ((EntityDamageByEntityEvent) execute.getRawEvent()).setCancelled(true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1.3f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1.4f);
        player.setVelocity(player.getLocation().getDirection().clone().normalize().multiply(-0.4));
        Location l1 = player.getLocation().clone().add(0, 2, 0).add(player.getLocation().getDirection().clone().normalize().multiply(0.5).add(rotateVector(player.getLocation().getDirection().clone().normalize().multiply(0.3), 90)));
        Location l2 = player.getLocation().clone().add(player.getLocation().getDirection().clone().normalize().multiply(0.5)).add(rotateVector(player.getLocation().getDirection().clone().normalize().multiply(0.3), -90));
        for (Entity e: particleApi.drawColoredLine(l1, l2, 1, Color.GRAY, 1, 0)){
            if (!(e instanceof LivingEntity)) continue;
            if (e.equals(player)) continue;
            ((Damageable) e).damage(4, player);
        }
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 16, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 16, 1));
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.CRIT, 5, 0.3, 0.3, 0.3, 0.01);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 15);
        return r;
    }
}
