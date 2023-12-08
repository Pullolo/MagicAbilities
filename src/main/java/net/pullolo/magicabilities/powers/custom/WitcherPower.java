package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.Removeable;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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

public class WitcherPower extends Power implements IdlePower, Removeable {

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
        if (ex instanceof DamagedByExecute && !shield){
            block((DamagedByExecute) ex);
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
            case 4:
                if(CooldownApi.isOnCooldown("WITCHER-4",p)) return;
                yrden(p);
                CooldownApi.addCooldown("WITCHER-4", p, 18);
                return;
            default:
                return;
        }
    }

    private void yrden(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1, 0.5f);
        Color c = Color.fromRGB(191,85,236);
        Location l = p.getLocation().clone().add(p.getLocation().getDirection().clone().setY(0).normalize().multiply(2));
        final Vector step = l.getDirection().clone().setY(0).normalize().multiply(4);
        new BukkitRunnable() {
            ArrayList<LivingEntity> modified = new ArrayList<>();
            int i = 0;
            @Override
            public void run() {
                i++;

                for (int j = 0; j<90; j++){
                    Location loc = l.clone().add(rotateVector(step.clone(), j*4));
                    while (loc.clone().add(0, -1, 0).getBlock().isPassable() && loc.getY()>-70){
                        loc.add(0, -1, 0);
                    }
                    if (j%5==0){
                        for (int k = 0; k<5; k++){
                            Vector toCenter = loc.clone().add(0, (double) k/6, 0).toVector().subtract(l.clone().toVector()).normalize().multiply(-1);
                            particleApi.spawnColoredParticles(loc.clone().add(0, (double) k/6, 0).add(toCenter.multiply((double) k/10)), c, 1, 1, 0.01, 0.01, 0.01);
                        }
                    }
                    particleApi.spawnColoredParticles(loc.clone(), c, 1, 1, 0.01, 0.01, 0.01);
                }
                for (Entity e : l.getWorld().getNearbyEntities(l.clone(), 3.5, 4, 3.5)){
                    if (!(e instanceof LivingEntity)) continue;
                    if (e instanceof Player) continue;
                    ((LivingEntity) e).addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 5, 0));
                    if (((LivingEntity) e).hasAI()){
                        ((LivingEntity) e).setAI(false);
                        modified.add((LivingEntity) e);
                    }
                }
                if (i>160){
                    for (LivingEntity e : modified){
                        if (!(!e.isDead() && e.isValid())) continue;
                        e.setAI(true);
                    }
                    modified.clear();
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(magicPlugin, 0, 1);
    }

    private void damagedExecute(DamagedExecute execute){
        Player player = execute.getPlayer();
        if (!shield) return;
        EntityDamageEvent event = (EntityDamageEvent) execute.getRawEvent();
        event.setCancelled(true);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1.3f);
        player.getWorld().playSound(player.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1.4f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 2);
        player.setVelocity(player.getLocation().getDirection().clone().normalize().multiply(-0.4));
        CooldownApi.addCooldown("WITCHER-2", player, 10);
        quenParticles.cancel();
        quenParticles=null;
        shield=false;

    }
    private void aksji(Player p){
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1, 0.7f);
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
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_PREPARE_SUMMON, 1, 2);
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
                e.setFireTicks(60);
                ((LivingEntity) e).damage(8, p);
                hit.add(e);
            }
        }
    }

    private void block(DamagedByExecute execute){
        if (!(new Random().nextInt(4)==0)) return;
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

    private Vector rotateVector(Vector vector, double whatAngle) {
        whatAngle = Math.toRadians(whatAngle);
        double sin = Math.sin(whatAngle);
        double cos = Math.cos(whatAngle);
        double x = vector.getX() * cos + vector.getZ() * sin;
        double z = vector.getX() * -sin + vector.getZ() * cos;

        return vector.setX(x).setZ(z);
    }

    @Override
    public void remove(){
        if (quenParticles!=null){
            quenParticles.cancel();
            quenParticles=null;
        }
        shield=false;
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 600, 0));
                if (p.getHealth()<5) p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 0));
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
                return "&bAard";
            case 2:
                return "&eQuen";
            case 3:
                return "&aAksji";
            case 4:
                return "&5Yrden";
            default:
                return "&7none";
        }
    }
}
