package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.Execute;
import net.pullolo.magicabilities.powers.executions.IdleExecute;
import net.pullolo.magicabilities.powers.executions.LeftClickExecute;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
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
                p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1.5f);
                fireBlast(execute, 1, 0);
                CooldownApi.addCooldown("FIRE-0", p, 2);
                return;
            case 1:
                if (CooldownApi.isOnCooldown("FIRE-1", p)) return;
                p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1.5f);
                int rotation = -20;
                for (int i = 0; i<5; i++){
                    fireBlast(execute, 0.8, rotation);
                    rotation+=10;
                }
                CooldownApi.addCooldown("FIRE-1", p, 2);
                return;
            case 2:
                if (CooldownApi.isOnCooldown("FIRE-2", p)) return;
                p.getWorld().playSound(p.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1, 1.5f);
                fireSurge(execute);
                CooldownApi.addCooldown("FIRE-2", p, 6);
                return;
            default:
                return;
        }
    }

    private void fireSurge(LeftClickExecute execute){
        Player p = execute.getPlayer();
        ArmorStand as = p.getWorld().spawn(p.getLocation().add(0, 1.5, 0), ArmorStand.class, en -> {
            en.setVisible(false);
            en.setGravity(false);
            en.setSmall(true);
            en.setMarker(true);
        });

        Location dest = p.getLocation().add(p.getLocation().getDirection().multiply(10));
        Vector v = dest.subtract(p.getLocation()).toVector();

        double s = 1;
        int d = 10;

        new BukkitRunnable(){
            final Random r = new Random();
            final int distance = d;
            final double speed = s;
            int i = 1;
            final HashMap<Block, Material> old = new HashMap<>();

            @Override
            public void run() {
                if (p == null){
                    as.remove();
                    cancel();
                }

                Location ground = as.getLocation().clone();
                while (ground.getBlock().isPassable() && !ground.getBlock().isLiquid() && ground.getY()>0){
                    ground.add(0, -1, 0);
                }
                ground.add(0, 1, 0);

                if (r.nextBoolean()) particleApi.spawnParticles(ground.clone().add(0, 1, 0), Particle.FLAME, r.nextInt(10)+1, 0.1, 0.1, 0.1, 0.01);
                as.teleport(as.getLocation().add(v.normalize().multiply(speed)));
                if(!old.containsKey(ground.clone().getBlock()) && !ground.clone().getBlock().getType().equals(Material.FIRE) && (ground.clone().getBlock().getType().equals(Material.AIR) || ground.clone().getBlock().getType().equals(Material.GRASS))) {
                    old.put(ground.clone().getBlock(), ground.clone().getBlock().getType());
                    ground.clone().getBlock().setType(Material.FIRE);
                }

                if (i > distance){
                    if (!as.isDead()){
                        for (Entity entity : ground.clone().getWorld().getNearbyEntities(ground.clone(),2.4, 3, 2.4)){
                            if (!as.isDead()){
                                if (entity instanceof ArmorStand){
                                    continue;
                                }
                                if (!entity.equals(p)){
                                    if (entity instanceof LivingEntity){
                                        entity.setFireTicks(80);
                                        ((LivingEntity) entity).damage(6, p);
                                    }
                                }
                            }
                        }
                        particleApi.spawnParticles(ground.clone(), Particle.FLAME, 100, 1, 1, 1, 0.1);
                        restoreBlocks(old);
                        as.remove();
                        cancel();
                    }
                }
                i++;
            }
        }.runTaskTimer(magicPlugin, 0, 1);
    }

    private void fireBlast(LeftClickExecute execute, double initM, int vectorRotate){
        Player p = execute.getPlayer();
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
                                    entity.setFireTicks(80);
                                    ((LivingEntity) entity).damage(12*initM, p);
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
                        if (l) {
                            as.getWorld().playSound(as.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 0.7f, 1.3f);
                        } else {
                            as.getWorld().playSound(as.getLocation(), Sound.ENTITY_PLAYER_HURT_ON_FIRE, 0.7f, 1.3f);
                        }
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

    private void restoreBlocks(HashMap<Block, Material> blocks){
        new BukkitRunnable() {
            @Override
            public void run() {
                if (blocks.isEmpty()) return;
                for (Block b : blocks.keySet()){
                    b.setType(blocks.get(b));
                }
                blocks.clear();
            }
        }.runTaskLater(magicPlugin, 20);
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
            case 1:
                return "&cFire Barrage";
            case 2:
                return "&cFire Surge";
            default:
                return "&7none";
        }
    }
}
