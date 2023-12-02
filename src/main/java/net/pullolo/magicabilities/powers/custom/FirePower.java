package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.Execute;
import net.pullolo.magicabilities.powers.executions.IdleExecute;
import net.pullolo.magicabilities.powers.executions.LeftClickExecute;
import org.bukkit.*;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.misc.GeneralMethods.rotateVector;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class FirePower extends Power implements IdlePower {
    public FirePower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof LeftClickExecute){
            executeLeftClick((LeftClickExecute) ex);
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
                if (CooldownApi.isOnCooldown("FIRE-0", p)) return;
                fireBlast(execute, 1, 0);
                CooldownApi.addCooldown("FIRE-0", p, 2);
                return;
            case 1:
                if (CooldownApi.isOnCooldown("FIRE-1", p)) return;
                int rotation = -20;
                for (int i = 1; i<5; i++){
                    fireBlast(execute, 0.8, rotation);
                    rotation+=10;
                }
                CooldownApi.addCooldown("FIRE-1", p, 2);
                return;
            default:
                return;
        }
    }

    private void fireBlast(LeftClickExecute execute, double initM, int vectorRotate){
        Player p = execute.getPlayer();
        p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1.5f);
        ArmorStand as = p.getWorld().spawn(p.getLocation().add(0, 1.5, 0), ArmorStand.class, en -> {
            en.setVisible(false);
            en.setGravity(false);
            en.setSmall(true);
            en.setMarker(true);
        });

        Location dest = p.getLocation().add(rotateVector(p.getLocation().getDirection(), vectorRotate).multiply(10));
        Vector v = dest.subtract(p.getLocation()).toVector();

        double s = 1;
        int d = 40;

        Color[] colors = new Color[3];
        colors[0] = Color.fromRGB(255, 72, 5);
        colors[1] = Color.fromRGB(255, 119, 0);
        colors[2] = Color.fromRGB(255, 68, 0);

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

                if (r.nextBoolean()) particleApi.spawnColoredParticles(as.getLocation(), colors[r.nextInt(colors.length)], 1, 10, 0.1, 0.1, 0.1);
                particleApi.spawnParticles(as.getLocation(), Particle.FLAME, r.nextInt(10)+1, 0.1, 0.1, 0.1, 0.01);
                as.teleport(as.getLocation().add(v.normalize().multiply(speed)));

                for (Entity entity : as.getLocation().getChunk().getEntities()){
                    if (!as.isDead()){
                        if (entity instanceof ArmorStand){
                            continue;
                        }
                        if (as.getLocation().distanceSquared(entity.getLocation()) <= 3.8){
                            if (!entity.equals(p)){
                                if (entity instanceof LivingEntity){
                                    ((LivingEntity) entity).damage(12*initM, p);
                                    entity.setFireTicks(80);
                                    as.getWorld().playSound(as.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1.3f);
                                    as.remove();
                                    cancel();
                                }
                            }
                        }
                    }
                }

                boolean l = as.getLocation().clone().getBlock().isLiquid();
                if (!as.getLocation().clone().getBlock().isPassable() || l){
                    if (!l && as.getLocation().clone().add(0, 1, 0).getBlock().getType().equals(Material.AIR)){
                        as.getLocation().clone().add(0, 1, 0).getBlock().setType(Material.FIRE);
                    }
                    if (!as.isDead()){
                        as.getWorld().playSound(as.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 1, 1.3f);
                        as.remove();
                        cancel();
                    }
                }

                if (i > distance){
                    if (!as.isDead()){
                        as.remove();
                        cancel();
                    }
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
                if (p.getFireTicks()>0) p.setFireTicks(0);
                p.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 21, 0));
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.FLAME, 5, 0.3, 0.3, 0.3, 0.01);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 20);
        return r;
    }
    @Override
    public String getAbilityName(int ability) {
        switch (ability){
            case 0:
                return "&cFire Blast";
            default:
                return "&7none";
        }
    }

}
