package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.misc.GeneralMethods;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.Removeable;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.*;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.misc.GeneralMethods.rotateVector;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class LightningPower extends Power implements IdlePower, Removeable {
    private BukkitRunnable orbParticles = null;
    private boolean orb = false;

    public LightningPower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof LeftClickExecute){
            executeLeftClick((LeftClickExecute) ex);
            return;
        }
        if (ex instanceof DealDamageExecute){
            dealDamageExecute((DealDamageExecute) ex);
            return;
        }
        if (ex instanceof DamagedExecute){
            preventSelfDamage((DamagedExecute) ex);
            return;
        }
        if (ex instanceof DamagedByExecute){
            damagedByExecute((DamagedByExecute) ex);
            return;
        }
    }

    private void damagedByExecute(DamagedByExecute ex) {
        final Player p = ex.getPlayer();
        if (CooldownApi.isOnCooldown("LIG-PAS", p)) return;
        final EntityDamageByEntityEvent event = (EntityDamageByEntityEvent) ex.getRawEvent();
        Entity damager = event.getDamager();
        if (!(damager instanceof LivingEntity)) return;
        event.setDamage(event.getFinalDamage()/3);
        ((LivingEntity) damager).damage(10, p);
        damager.setFireTicks(21);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CREEPER_HURT, 1.3f, 2);
        p.getWorld().playSound(p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(3)),
                Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2);
        for (int i = 0; i<15; i++) electricDischarge(p, 2);
        CooldownApi.addCooldown("LIG-PAS", p, 30);
    }

    private void electricDischarge(Player p, double rad) {
        final Color[] colors = new Color[3];
        colors[0] = Color.fromRGB(0, 180, 250);
        colors[1] = Color.BLUE;
        colors[2] = Color.AQUA;
        Random r = new Random();
        Location l = p.getLocation().clone().add(0, 1, 0);
        Vector v1 = p.getLocation().getDirection().clone().setX(1).setZ(1).clone().setY(0).normalize().multiply(rad);
        v1.rotateAroundZ(r.nextInt(360));
        v1.rotateAroundX(r.nextInt(360));
        v1.rotateAroundY(r.nextInt(360));
        Vector v2 = p.getLocation().getDirection().clone().setX(1).setZ(1).clone().setY(0).normalize().multiply(rad);
        v2.rotateAroundZ(r.nextInt(360));
        v2.rotateAroundX(r.nextInt(360));
        v2.rotateAroundY(r.nextInt(360));
        Location t1 = l.clone().add(v1);
        Location t2 = l.clone().add(v2);
        for (Entity e : particleApi.drawColoredLine(t1, t2, 1, colors[r.nextInt(colors.length)], 1, 0)){
            if (!(e instanceof LivingEntity)) continue;
            if (e.equals(p)) continue;
            e.setFireTicks(21);
            ((Damageable) e).damage(5, p);
        }
    }

    private void preventSelfDamage(DamagedExecute execute){
        EntityDamageEvent event = (EntityDamageEvent) execute.getRawEvent();
        if (event.getCause().equals(EntityDamageEvent.DamageCause.LIGHTNING)) event.setCancelled(true);
    }

    private void dealDamageExecute(DealDamageExecute execute){
        Player p = execute.getPlayer();
        Entity damaged = ((EntityDamageByEntityEvent) execute.getRawEvent()).getEntity();
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 0:
                if (CooldownApi.isOnCooldown("LIG-1", p)) return;
                lightningStrike(damaged.getLocation());
                CooldownApi.addCooldown("LIG-1", p, 20);
                return;
        }
        return;
    }

    private void executeLeftClick(LeftClickExecute execute){
        Player p = execute.getPlayer();
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 1:
                if (CooldownApi.isOnCooldown("LIG-2", p)) return;
                shootLightningSparks(p);
                CooldownApi.addCooldown("LIG-2", p, 3);
                return;
            case 2:
                if (orb) return;
                if (CooldownApi.isOnCooldown("LIG-3", p)) return;
                lightningOrb(p);
                return;
            case 3:
                if (CooldownApi.isOnCooldown("LIG-4", p)) return;
                tp(p);
                return;
        }
    }

    private void tp(Player p) {
        if (!p.getWorld().hasStorm()){
            p.sendMessage(ChatColor.RED + "You can use this ability only during storms!");
            return;
        }
        for (int i = 0; i<50; i++){
            if (!p.getEyeLocation().add(p.getEyeLocation().getDirection().clone().normalize().multiply(i)).getBlock().isPassable()){
                p.teleport(p.getEyeLocation().add(p.getEyeLocation().getDirection().clone().normalize().multiply(i)).clone().add(0, 1, 0));
                p.getWorld().spawn(p.getLocation(), LightningStrike.class);
                CooldownApi.addCooldown("LIG-4", p, 20);
                return;
            }
        }
        p.sendMessage(ChatColor.RED + "Couldn't teleport!");
    }

    private void lightningOrb(Player p) {
        final Color[] colors = new Color[3];
        colors[0] = Color.fromRGB(0, 180, 250);
        colors[1] = Color.BLUE;
        colors[2] = Color.AQUA;
        orbParticles = new BukkitRunnable() {
            int i = 0;
            int counter;
            Random r = new Random();

            @Override
            public void run() {
                if (i%5==0){
                    p.getWorld().playSound(p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(3)),
                            Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 2);
                }
                Location l = p.getLocation().clone().add(0, 1, 0);
                for (int j = 0; j<3; j++){
                    Vector v1 = p.getLocation().getDirection().clone().setX(1).setZ(1).clone().setY(0).normalize().multiply(5);
                    v1.rotateAroundZ(r.nextInt(360));
                    v1.rotateAroundX(r.nextInt(360));
                    v1.rotateAroundY(r.nextInt(360));
                    Vector v2 = p.getLocation().getDirection().clone().setX(1).setZ(1).clone().setY(0).normalize().multiply(5);
                    v2.rotateAroundZ(r.nextInt(360));
                    v2.rotateAroundX(r.nextInt(360));
                    v2.rotateAroundY(r.nextInt(360));
                    Location t1 = l.clone().add(v1);
                    Location t2 = l.clone().add(v2);
                    counter=0;
                    for (Entity e : particleApi.drawColoredLine(t1, t2, 1, colors[r.nextInt(colors.length)], 1, 0)){
                        if (!(e instanceof LivingEntity)) continue;
                        if (e.equals(p)) continue;
                        e.setFireTicks(21);
                        ((Damageable) e).damage(8, p);
                        counter++;
                    }
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20, (int) counter/3));
                }
                i++;
                if (i>60){
                    remove();
                    CooldownApi.addCooldown("LIG-3", p, 15);
                }
            }
        };
        orbParticles.runTaskTimer(magicPlugin, 0, 1);
        orb=true;
    }

    @Override
    public void remove(){
        if (orbParticles!=null){
            orbParticles.cancel();
            orbParticles=null;
        }
        orb=false;
    }

    private void lightningStrike(Location l){
        l.getWorld().spawn(l, LightningStrike.class);
    }

    private void shootLightningSparks(Player p){
        final Random r = new Random();

        Color[] colors = new Color[3];
        colors[0] = Color.fromRGB(0, 180, 250);
        colors[1] = Color.BLUE;
        colors[2] = Color.AQUA;

        p.playSound(p.getLocation(), Sound.ENTITY_CREEPER_HURT, 1.3f, 2);

        new BukkitRunnable(){
            Location l = p.getLocation().clone().add(0, 1, 0);
            Location dest;
            int i = 0;
            ArrayList<Entity> hit = new ArrayList<>();

            @Override
            public void run() {
                if (p == null){
                    cancel();
                }

                dest = l.clone().add(rotateVector(p.getLocation().getDirection().clone().normalize().multiply(2.5), r.nextInt(81)-40));

                for (Entity e : particleApi.drawColoredLine(l, dest, 1, colors[r.nextInt(colors.length)], 1, 0)){
                    if(!(e instanceof Damageable)) continue;
                    if (hit.contains(e) || e.equals(p)) continue;
                    e.setFireTicks(21);
                    ((Damageable) e).damage(16, p);
                    hit.add(e);
                }

                l = dest;
                if (i > 3){
                    particleApi.spawnParticles(dest, Particle.ELECTRIC_SPARK, 30, 0.1, 0.1,0.1, 1);
                    particleApi.spawnColoredParticles(dest, Color.BLUE, 2, 20, 1,1, 1);
                    p.getWorld().playSound(p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(3)),
                            Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 2);
                    cancel();
                    return;
                }
                i++;
            }
        }.runTaskTimer(magicPlugin, 0, 2);
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 21, 1));
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.ELECTRIC_SPARK, 5, 0.3, 0.3, 0.3, 0.01);
                if (!CooldownApi.isOnCooldown("LIG-PAS", p) && new Random().nextInt(5)==0){
                    electricDischarge(p, 1);
                    p.getWorld().playSound(p.getLocation(), Sound.ENTITY_CREEPER_HURT, 1.3f, 2);
                }
            }
        };
        r.runTaskTimer(magicPlugin, 0, 20);
        return r;
    }

    @Override
    public String getAbilityName(int ability){
        switch (ability){
            case 0:
                return "&3Lightning Strike";
            case 1:
                return "&3Lightning Shot";
            case 2:
                return "&3Electric Field";
            case 3:
                return "&3Electron Transmission";
            default:
                return "&7none";
        }
    }
}
