package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.misc.GeneralMethods;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
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

public class LightningPower extends Power implements IdlePower {
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
        }
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
            default:
                return "&7none";
        }
    }
}
