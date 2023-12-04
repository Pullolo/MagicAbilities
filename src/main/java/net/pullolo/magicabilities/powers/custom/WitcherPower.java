package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.DamagedExecute;
import net.pullolo.magicabilities.powers.executions.Execute;
import net.pullolo.magicabilities.powers.executions.IdleExecute;
import net.pullolo.magicabilities.powers.executions.LeftClickExecute;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class WitcherPower extends Power implements IdlePower {

    private boolean shield = false;
    private BukkitRunnable quenParticles = null;

    public WitcherPower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof LeftClickExecute){
            executeLeftClick((LeftClickExecute) ex);
            return;
        }
        if (ex instanceof DamagedExecute){
            damagedExecute((DamagedExecute) ex);
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
                if (CooldownApi.isOnCooldown("WITCHER-0", p)) return;
                igni(p);
                CooldownApi.addCooldown("WITCHER-0", p, 3);
                return;
            case 1:
                if(CooldownApi.isOnCooldown("WITCHER-1",p)) return;
                aard(p);
                CooldownApi.addCooldown("WITCHER-1", p, 6);
                return;
            case 2:
                if (shield) return;
                if(CooldownApi.isOnCooldown("WITCHER-2",p)) return;
                quen(p);
                return;
            case 3:
                if(CooldownApi.isOnCooldown("WITCHER-3",p)) return;
                aksji(p);
                CooldownApi.addCooldown("WITCHER-3", p, 10);
                return;
            default:
                return;
        }
    }

    private void damagedExecute(DamagedExecute execute){
        Player player = execute.getPlayer();
        if (!shield) return;
        EntityDamageEvent event = (EntityDamageEvent) execute.getRawEvent();
        event.setCancelled(true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1.3f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1.4f);
        player.setVelocity(player.getLocation().getDirection().clone().normalize().multiply(-0.4));
        CooldownApi.addCooldown("WITCHER-2", player, 10);
        quenParticles.cancel();
        quenParticles=null;
        shield=false;

    }
    private void aksji(Player p){
        Color c = Color.fromRGB(0,255,0);
        double lineSize = 1;
        Location l = p.getLocation().add(0, 1.6, 0).clone().add(p.getLocation().getDirection().clone().setY(0).normalize()).add(0, 0.2, 0).add(rotateVector(p.getLocation().getDirection().clone().setY(0).normalize().multiply(lineSize/2) ,-90));
        for (int i = 0; i < 10; i++){
            particleApi.spawnColoredParticles(l, c,1,2, 0.01, 0.01, 0.01);
            l.add(rotateVector(p.getLocation().getDirection().clone().setY(0).normalize().multiply(lineSize/10) ,90));
        }
        for (int i = 0; i<5; i++){
            particleApi.spawnColoredParticles(l, c,1,2, 0.01, 0.01, 0.01);
            l.add(rotateVector(p.getLocation().getDirection().clone().setY(0).normalize().multiply(lineSize/10) ,-90)).add(0, -lineSize/8, 0);
        }
        for (int i = 0; i<4; i++){
            particleApi.spawnColoredParticles(l, c,1,2, 0.01, 0.01, 0.01);
            l.add(rotateVector(p.getLocation().getDirection().clone().setY(0).normalize().multiply(lineSize/10) ,-90)).add(0, lineSize/8, 0);
        }
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(3)), 4, 4, 4)){
            if (!(e instanceof LivingEntity)) continue;
            if (e.equals(p)) continue;
            ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 80, 255));
        }
    }
    private void quen(Player p){
        quenParticles = new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                Location l = p.getLocation().clone().add(0, 1, 0);
                l.add(rotateVector(p.getLocation().getDirection().setX(1).setZ(1).clone().setY(0).normalize().multiply(1), i*6));
                l.add(0, Math.sin((double) i/5)*0.5, 0);
                particleApi.spawnColoredParticles(l, Color.ORANGE, 1, 2, 0.01, 0.01, 0.01);
                i++;
            }
        };
        quenParticles.runTaskTimer(magicPlugin, 0, 1);
        shield=true;
    }

    private void aard(Player p){
        ArrayList<Entity> hit = new ArrayList<>();
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PHANTOM_HURT, 1, 2);
        for (int i = 0; i < 7; i++){
            int j = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0).add(p.getLocation().getDirection().clone().normalize().multiply(j*1.5)), Particle.CLOUD, 30, 0.3, 0.3, 0.3, 0.01);
                    for (Entity e : p.getWorld().getNearbyEntities(p.getLocation().clone().add(0, 1, 0).add(p.getLocation().getDirection().clone().normalize().multiply(j)), 1.5, 1.5, 1.5)){
                        if (!(e instanceof LivingEntity)) continue;
                        if (e.equals(p)) continue;
                        if (hit.contains(e)) continue;
                        ((LivingEntity) e).damage(2, p);
                        e.setVelocity(p.getLocation().getDirection().clone().normalize().multiply(1.3).add(new Vector(0, 0.2, 0)));
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 80, 0));
                        ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                        hit.add(e);
                    }
                }
            }.runTaskLater(magicPlugin, i);
        }
    }

    private void igni(Player p){
        ArrayList<Entity> hit = new ArrayList<>();
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 1);
        for (int i = 0; i < 5; i++){
            particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0).add(p.getLocation().getDirection().clone().normalize().multiply(i)), Particle.FLAME, 50, 0.3, 0.3, 0.3, 0.01);
            for (Entity e : p.getWorld().getNearbyEntities(p.getLocation().clone().add(0, 1, 0).add(p.getLocation().getDirection().clone().normalize().multiply(i)), 1.5, 1.5, 1.5)){
                if (!(e instanceof LivingEntity)) continue;
                if (e.equals(p)) continue;
                if (hit.contains(e)) continue;
                ((LivingEntity) e).damage(8, p);
                e.setFireTicks(60);
                hit.add(e);
            }
        }
    }

    private Vector rotateVector(Vector vector, double whatAngle) {
        whatAngle = Math.toRadians(whatAngle);
        double sin = Math.sin(whatAngle);
        double cos = Math.cos(whatAngle);
        double x = vector.getX() * cos + vector.getZ() * sin;
        double z = vector.getX() * -sin + vector.getZ() * cos;

        return vector.setX(x).setZ(z);
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
            }
        };
        r.runTaskTimer(magicPlugin, 0, 40);
        return r;
    }

    @Override
    public String getAbilityName(int ability) {
        switch (ability){
            case 0:
                return "&cIgni";
            case 1:
                return "&9Aard";
            case 2:
                return "&6Quen";
            case 3:
                return "&aAksji";
            case 4:
                return "&5Yrden";
            default:
                return "&7none";
        }
    }
}
