package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.cooldowns.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
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
import static net.pullolo.magicabilities.cooldowns.Cooldowns.cooldowns;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.misc.GeneralMethods.rotateVector;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class TwilightMirage extends Power implements IdlePower {
    private static final String tm_shriek = "twilight-mirage.shriek-transition";
    private static final String tm_float = "twilight-mirage.float";
    private static final String tm_missile = "twilight-mirage.missile";
    private static final String tm_healing = "twilight-mirage.healing";

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
                if (CooldownApi.isOnCooldown(tm_shriek, p)) {
                    onCooldownInfo(CooldownApi.getCooldownForPlayerLong(tm_shriek, p));
                    return;
                }
                shriekTransition(p);
                CooldownApi.addCooldown(tm_shriek, p, cooldowns.get(tm_shriek));
                return;
            case 1:
                if (CooldownApi.isOnCooldown(tm_float, p)) {
                    onCooldownInfo(CooldownApi.getCooldownForPlayerLong(tm_float, p));
                    return;
                }
                twilightFloat(p);
                CooldownApi.addCooldown(tm_float, p, cooldowns.get(tm_float));
                return;
            case 2:
                if (CooldownApi.isOnCooldown(tm_missile, p)) {
                    onCooldownInfo(CooldownApi.getCooldownForPlayerLong(tm_missile, p));
                    return;
                }
                magicMissile(p, 0);
                CooldownApi.addCooldown(tm_missile, p, cooldowns.get(tm_missile));
                return;
            case 3:
                if (CooldownApi.isOnCooldown(tm_healing, p)) {
                    onCooldownInfo(CooldownApi.getCooldownForPlayerLong(tm_healing, p));
                    return;
                }
                healingMirage(p);
                CooldownApi.addCooldown(tm_healing, p, cooldowns.get(tm_healing));
                return;
        }
    }

    private void healingMirage(Player p){
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1, 0.5f);
        Location hmLoc = p.getLocation().clone();
        final Vector step = hmLoc.getDirection().clone().setY(0).normalize().multiply(4);
        new BukkitRunnable() {
            int i = 0;
            @Override
            public void run() {
                if (i%20==0){
                    for (Entity e : hmLoc.getWorld().getNearbyEntities(hmLoc, 3, 3, 3)){
                        if (!(e instanceof Player)) continue;
                        ((Player) e).addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 41, isNight(p) ? 2 : 1));
                    }
                    for (int j = 0; j<90; j++){
                        Location loc = hmLoc.clone().add(rotateVector(step.clone(), j*4));
                        particleApi.spawnParticles(loc.clone(), Particle.EGG_CRACK, 1, 0, 0, 0, 0.01);
                    }
                }

                particleApi.spawnParticles(hmLoc, Particle.GLOW, 5, 2, 2, 2, 1);

                if (i>120){
                    cancel();
                    return;
                }
                i++;
            }
        }.runTaskTimer(magicPlugin, 0, 1);
    }

    private void magicMissile(Player p, int vectorRotate){
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1, 2);
        ArmorStand as = p.getWorld().spawn(p.getLocation().add(0, 1.5, 0), ArmorStand.class, en -> {
            en.setVisible(false);
            en.setGravity(false);
            en.setSmall(true);
            en.setMarker(true);
        });

        Location dest = p.getLocation().add(rotateVector(p.getLocation().getDirection(), vectorRotate).multiply(10));
        Vector v = dest.subtract(p.getLocation()).toVector();

        double s = 1;
        int d = 20;

        Color[] colors = new Color[3];
        colors[0] = Color.fromRGB(255, 59, 232);
        colors[1] = Color.fromRGB(0, 255, 217);
        colors[2] = Color.fromRGB(240, 214, 255);

        new BukkitRunnable(){
            final Random r = new Random();
            final int distance = d;
            final double speed = s;
            int i = 1;

            @Override
            public void run() {
                if (p == null){
                    as.remove();
                    cancel();
                }

                particleApi.spawnColoredParticles(as.getLocation(), colors[r.nextInt(colors.length)], 1, 3, 0.01, 0.01, 0.01);
                if (r.nextBoolean()) particleApi.spawnParticles(as.getLocation(), Particle.GLOW, r.nextInt(10)+1, 0.01, 0.01, 0.01, 0.01);
                as.teleport(as.getLocation().add(v.normalize().multiply(speed)));

                for (Entity entity : as.getLocation().getChunk().getEntities()){
                    if (!as.isDead()){
                        if (entity instanceof ArmorStand){
                            continue;
                        }
                        if (as.getLocation().distanceSquared(entity.getLocation()) <= 3.8){
                            if (!entity.equals(p)){
                                if (entity instanceof LivingEntity){
                                    ((LivingEntity) entity).damage(isNight(p) ? 22 : 14, p);
                                    ((LivingEntity) entity).addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 100, 0));
                                    spellExplode(p, as.getLocation().clone());
                                    as.remove();
                                    cancel();
                                }
                            }
                        }
                    }
                }

                boolean l = as.getLocation().clone().getBlock().isLiquid();
                if (!as.getLocation().clone().getBlock().isPassable() || l){
                    if (!as.isDead()){
                        spellExplode(p, as.getLocation().clone());
                        as.remove();
                        cancel();
                    }
                }

                if (i > distance){
                    if (!as.isDead()){
                        spellExplode(p, as.getLocation().clone());
                        as.remove();
                        cancel();
                    }
                }
                i++;
            }
        }.runTaskTimer(magicPlugin, 0, 1);
    }

    private void spellExplode(Player p, Location l){
        particleApi.spawnParticles(l, Particle.GUST, 1, 0, 0, 0, 1);
        particleApi.spawnParticles(l, Particle.GLOW, 100, 1, 1, 1, 10);
        particleApi.spawnParticles(l, Particle.FIREWORK, 100, 0.1, 0.1, 0.1, 0.4);
        l.getWorld().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0.6f);
        l.getWorld().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST, 1, 0.9f);
        l.getWorld().playSound(l, Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR, 1, 1);
        l.getWorld().playSound(l, Sound.ENTITY_WARDEN_SONIC_BOOM, 1, 2);
        for (Entity e : l.getWorld().getNearbyEntities(l, 2, 2, 2)){
            if (e instanceof LivingEntity){
                ((LivingEntity) e).damage(isNight(p) ? 16 : 10, p);
            }
        }
    }

    private void twilightFloat(Player p){
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1, 1.3f);
        p.setVelocity(new Vector(0, 1, 0).normalize().multiply(0.8));
        particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0), Particle.GLOW,
                60, 1, 1, 1, 0.6);
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 55, 0));
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
            particleApi.spawnParticles(p.getLocation(), Particle.TOTEM_OF_UNDYING,
                    100, 1, 1, 1, 0.6);
            return;
        }
    }

    private boolean isNight(Player p){
        return !(p.getWorld().getTime()<12300 || p.getWorld().getTime()>23850);
    }

    private boolean isClearWeather(Player p){
        return p.getWorld().isClearWeather();
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (p.isInWater() && !isNight(p) && isClearWeather(p)){
                    p.damage(0.5);
                }
                if (p.getLocation().getBlock().getLightLevel()>14){
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0));
                }
                if (p.hasPotionEffect(PotionEffectType.WITHER)){
                    p.removePotionEffect(PotionEffectType.WITHER);
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
                return "&9Twilight Leap";
            case 2:
                return "&9Magic Missile";
            case 3:
                return "&9Healing Mirage";
            default:
                return "&7none";
        }
    }
}
