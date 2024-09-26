package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.Execute;
import net.pullolo.magicabilities.powers.executions.IdleExecute;
import net.pullolo.magicabilities.powers.executions.LeftClickExecute;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.*;
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

public class Curseweaver extends Power implements IdlePower {


    public Curseweaver(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof LeftClickExecute){
            executeLeftClick((LeftClickExecute) ex);
            return;
        }
    }

    private void executeLeftClick(LeftClickExecute ex){
        final Player p = ex.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 0:
                //if (CooldownApi.isOnCooldown("ICE-DEF", p)) return;
                cleave(p, 0, false);
                //CooldownApi.addCooldown("ICE-DEF", p, 1);
                return;
            case 1:
                blackFlash(p);
                return;
        }
    }

    private void blackFlash(Player p){
        final Random r = new Random();
        for (int i = 0; i<5; i++){
            new BukkitRunnable() {
                @Override
                public void run() {
                    for (int j = 0; j<8; j++){
                        shootLightningSparks(p, 3);
                    }
                }
            }.runTaskLater(magicPlugin, i);
        }
    }

    private void shootLightningSparks(Player p, int bolts){
        final Random r = new Random();

        Color[] colors = new Color[3];
        colors[0] = Color.fromRGB(31, 31, 31);
        colors[1] = Color.BLACK;
        colors[2] = Color.fromRGB(61, 61, 61);

        p.playSound(p.getLocation(), Sound.ENTITY_CREEPER_HURT, 1.3f, 2);

        Vector dir = new Vector(r.nextFloat()*2-1, r.nextFloat()*2-1, r.nextFloat()*2-1).normalize();

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

                dest = l.clone().add(rotateVector(dir.normalize().multiply(2.5), r.nextInt(81)-40));

                for (Entity e : particleApi.drawColoredLine(l, dest, 1, colors[r.nextInt(colors.length)], 1, 0)){
                    if(!(e instanceof Damageable)) continue;
                    if (hit.contains(e) || e.equals(p)) continue;
                    e.setFireTicks(21);
                    ((Damageable) e).damage(18, p);
                    hit.add(e);
                }

                l = dest;
                if (i > bolts){
                    particleApi.spawnParticles(dest, Particle.ELECTRIC_SPARK, 30, 0.1, 0.1,0.1, 1);
                    particleApi.spawnColoredParticles(dest, Color.BLACK, 2, 20, 1,1, 1);
                    p.getWorld().playSound(p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(3)),
                            Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.2f, 2);
                    p.getWorld().playSound(p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(3)),
                            Sound.ENTITY_WITHER_SHOOT, 0.1f, 1.3f);
                    cancel();
                    return;
                }
                i++;
            }
        }.runTaskTimer(magicPlugin, 0, 2);
    }

    private void cleave(Player p, int rotate, boolean mirror){
        ArmorStand as = p.getWorld().spawn(p.getLocation().add(0, 1.5, 0), ArmorStand.class, en -> {
            en.setVisible(false);
            en.setGravity(false);
            en.setSmall(true);
            en.setMarker(true);
        });

        Location dest = p.getLocation().clone().add(rotateVector(p.getLocation().getDirection(), rotate).clone().multiply(10));
        Vector v = dest.clone().subtract(p.getLocation().clone()).toVector();

        p.playSound(p.getLocation().clone(), Sound.ENTITY_BREEZE_CHARGE, 1, 1.7f);
        p.playSound(p.getLocation().clone(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 2);
        p.playSound(p.getLocation().clone(), Sound.ITEM_FIRECHARGE_USE, 1, 1.6f);

        Color[] colors = new Color[3];
        colors[0] = Color.RED;
        colors[1] = Color.fromRGB(217, 0, 72);
        colors[2] = Color.fromRGB(107, 0, 0);

        new BukkitRunnable(){
            int i = 1;
            final int distance = 50;
            final double speed = 1;
            int ring = 180;
            boolean secondShot = false;
            final ArrayList<Entity> hit = new ArrayList<>();
            final Random r = new Random();
            @Override
            public void run() {
                as.teleport(as.getLocation().add(v.normalize().multiply(speed)));
                //particleApi.spawnParticles(as.getLocation().clone(), Particle.DRAGON_BREATH, 10, 0.1, 0.1, 0.1, 0.01);

                for (int j = 0; j<ring; j++){
                    Location l = as.getLocation().clone().add(as.getLocation().clone().getDirection().clone().normalize().rotateAroundAxis(
                            rotateVector(as.getLocation().clone().getDirection().clone().setY(mirror ? 1.2 : -1.2).normalize(), 90).clone(), Math.toRadians(j-(ring/2))
                    ).clone().multiply((double) (distance - i)/distance));
                    particleApi.spawnColoredParticles(l.clone(), colors[r.nextInt(colors.length)], 0.7f, 1, 0.01, 0.01, 0.01);
                }

                for (Entity entity : as.getLocation().getChunk().getEntities()){
                    if (!as.isDead()){
                        if (entity instanceof ArmorStand || hit.contains(entity)){
                            continue;
                        }
                        if (as.getLocation().distanceSquared(entity.getLocation()) <= 3.8){
                            if (!entity.equals(p)){
                                if (entity instanceof LivingEntity){
                                    entity.setFireTicks(61);
                                    ((LivingEntity) entity).setNoDamageTicks(0);
                                    ((LivingEntity) entity).damage(11, p);
                                    hit.add(entity);
                                }
                            }
                        }
                    }
                }

                if (!as.getLocation().clone().getBlock().isPassable()){
                    if (!as.isDead()){
                        as.remove();
                        cancel();
                    }
                }
                if (i>distance){
                    if (!as.isDead()){
                        as.remove();
                        cancel();
                    }
                }
                if (!mirror && i>4 & !secondShot){
                    cleave(p, 0, true);
                    secondShot = true;
                }
                i++;
            }
        }.runTaskTimer(magicPlugin, 0, 1);
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.DRAGON_BREATH, 8, 0.1, 0.1, 0.1, 0.07);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 15);
        return r;
    }

    @Override
    public String getAbilityName(int ability) {
        switch (ability){
            case 0:
                return "&cCleave";
            case 1:
                return "&8Black Flash";
            default:
                return "&7none";
        }
    }
}
