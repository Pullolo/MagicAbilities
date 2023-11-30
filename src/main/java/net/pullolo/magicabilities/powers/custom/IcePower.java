package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.misc.GeneralMethods;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.MagicAbilities.*;
import static net.pullolo.magicabilities.misc.GeneralMethods.rotateVector;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class IcePower extends Power implements IdlePower {
    public IcePower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex.getPlayer().getWorld().getEnvironment().equals(World.Environment.NETHER)){
            return;
        }
        if (ex instanceof MoveExecute){
            moveExecution((MoveExecute) ex);
            return;
        }
        if (ex instanceof DamagedByExecute){
            damagedByExecute((DamagedByExecute) ex);
            return;
        }
        if (ex instanceof LeftClickExecute){
            executeLeftClick((LeftClickExecute) ex);
            return;
        }
        if (ex instanceof RightClickExecute){
            executeRightClick((RightClickExecute) ex);
            return;
        }
        if (ex instanceof DamagedExecute){
            executeDamaged((DamagedExecute) ex);
            return;
        }
    }

    private void executeDamaged(DamagedExecute execute){
        EntityDamageEvent event = (EntityDamageEvent) execute.getRawEvent();
        final Player p = execute.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL)){
            if (p.getLocation().clone().add(0, -1, 0).getBlock().getType().equals(Material.ICE)
                    || p.getLocation().clone().add(0, -1, 0).getBlock().getType().equals(Material.SNOW_BLOCK)
                    || p.getLocation().clone().add(0, -1, 0).getBlock().getType().equals(Material.FROSTED_ICE)
                    || p.getLocation().clone().getBlock().getType().equals(Material.SNOW)
            ){
                event.setCancelled(true);
            }
        }
    }

    private void executeRightClick(RightClickExecute ex){
        final Player p = ex.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
    }

    private void executeLeftClick(LeftClickExecute ex){
        final Player p = ex.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 0:
                if (CooldownApi.isOnCooldown("ICE-DEF", p)) return;
                shootIce(ex, 1, 0);
                CooldownApi.addCooldown("ICE-DEF", p, 1);
                return;
            case 1:
                if (CooldownApi.isOnCooldown("ICE-1", p)) return;
                for (int i = 0; i < 6; i++){
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            shootIce(ex, 0.6, 0);
                        }
                    }.runTaskLater(magicPlugin, i*10);
                }
                CooldownApi.addCooldown("ICE-1", p, 6.5);
                return;
            case 2:
                if (CooldownApi.isOnCooldown("ICE-2", p)) return;
                iceSpikesFromBelow(ex, 1.1);
                CooldownApi.addCooldown("ICE-2", p, 1.6);
                return;
            case 3:
                if (CooldownApi.isOnCooldown("ICE-3", p)) return;
                explode(ex, 1.4);
                CooldownApi.addCooldown("ICE-3", p, 10);
                return;
            case 4:
                if (CooldownApi.isOnCooldown("ICE-4", p)) return;
                int rotation = -20;
                for (int i = 1; i<5; i++){
                    shootIce(ex, 0.8, rotation);
                    rotation+=10;
                }
                CooldownApi.addCooldown("ICE-4", p, 3);
                return;
            case 5:
                if (CooldownApi.isOnCooldown("ICE-5", p)) return;
                iceSlash(ex);
                CooldownApi.addCooldown("ICE-5", p, 12);
                return;
            case 8:
                if (CooldownApi.isOnCooldown("ICE-8", p)) return;
                phaseChange(ex);
                CooldownApi.addCooldown("ICE-8", p, 5);
                return;

        }
    }

    private void iceSlash(LeftClickExecute execute){
        final Player p = execute.getPlayer();
        final ArrayList<Entity> hit = new ArrayList<>();
        final Location start = p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(2));
        for (int i = 0 ; i< 6; i++){
            final int j = i;
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location l = p.getLocation().clone().add(0, 4-(j*0.6), 0).add(p.getLocation().getDirection().clone().normalize().multiply(j*2));
                    createIceLine(p, start, l, hit);
                }
            }.runTaskLater(magicPlugin, 2*i);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location l = p.getLocation().clone().add(0, 4-(j*0.6), 0).add(rotateVector(p.getLocation().getDirection().clone().normalize().multiply(j*2), 9));
                    createIceLine(p, start, l, hit);
                }
            }.runTaskLater(magicPlugin, 2*i);
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location l = p.getLocation().clone().add(0, 4-(j*0.6), 0).add(rotateVector(p.getLocation().getDirection().clone().normalize().multiply(j*2), -9));
                    createIceLine(p, start, l, hit);
                }
            }.runTaskLater(magicPlugin, 2*i);
        }
    }

    private void createIceLine(Player p, Location l1, Location l2, ArrayList<Entity> hit){
        Color[] colors = new Color[3];
        colors[0] = Color.fromRGB(92, 226, 250);
        colors[1] = Color.fromRGB(214, 249, 255);
        colors[2] = Color.fromRGB(2, 188, 250);
        p.getLocation().getWorld().playSound(p.getLocation(), Sound.BLOCK_SNOW_STEP, 1.3f, 1.1f);
        for (Entity e: particleApi.drawColoredLine(l1, l2, 1, colors[new Random().nextInt(colors.length)], new Random().nextInt(2)+1, 0)){
            if (!(e instanceof LivingEntity)) continue;
            if (e.equals(p)) continue;
            if (hit.contains(e)) continue;
            ((Damageable) e).damage(calculateDamage(p, (LivingEntity) e, 1.6), p);
            e.setFreezeTicks(e.getMaxFreezeTicks()*6);
            spawnSpellHitParticles(e.getLocation().clone().add(0, 1,0));
        }
    }

    private void explode(LeftClickExecute ex, double initM) {
        Player p = ex.getPlayer();
        for (Entity entity : p.getNearbyEntities(5, 5, 5)){
            if (!entity.equals(p)){
                if (entity instanceof LivingEntity){
                    ((LivingEntity) entity).damage(calculateDamage(p, (LivingEntity) entity, initM), p);
                    entity.setFreezeTicks(entity.getMaxFreezeTicks()*6);
                    spawnSpellHitParticles(p.getLocation().clone().add(0, 1,0));
                }
            }
        }
        p.getWorld().playSound(p.getLocation(), Sound.BLOCK_GLASS_BREAK, 1, 0.9f+new Random().nextFloat());
        particleApi.spawnParticles(p.getLocation(), Particle.FIREWORKS_SPARK, 60, 0.01, 0.01, 0.01, 0.5);
        particleApi.spawnParticles(p.getLocation(), Particle.SNOWFLAKE, 60, 0.01, 0.01, 0.01, 0.3);
        spawnIceSpikesParticles(p.getLocation().clone().add(0, 1, 0));
    }

    private void moveExecution(MoveExecute ex){
        final Player p = ex.getPlayer();
        final PlayerMoveEvent event = (PlayerMoveEvent) ex.getRawEvent();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        if (!(p.isOnGround() && !p.isSwimming())){
            return;
        }
        setColdBlock(event.getTo(), p);
    }

    private void damagedByExecute(DamagedByExecute ex){
        Entity damager = ((EntityDamageByEntityEvent) ex.getRawEvent()).getDamager();
        damager.setFreezeTicks(damager.getMaxFreezeTicks()*4); //around 7 sec
    }

    private void iceSpikesFromBelow(LeftClickExecute execute, double initM){
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

                if (r.nextBoolean()) particleApi.spawnParticles(ground.clone().add(0, 1, 0), Particle.SNOWFLAKE, r.nextInt(10)+1, 0.1, 0.1, 0.1, 0.01);
                as.teleport(as.getLocation().add(v.normalize().multiply(speed)));
                old.put(ground.clone().getBlock(), ground.clone().getBlock().getType());
                ground.clone().getBlock().setType(Material.ICE);


                if (i > distance){
                    if (!as.isDead()){
                        for (Entity entity : ground.clone().getWorld().getNearbyEntities(ground.clone(),2.4, 3, 2.4)){
                            if (!as.isDead()){
                                if (entity instanceof ArmorStand){
                                    continue;
                                }
                                if (!entity.equals(p)){
                                    if (entity instanceof LivingEntity){
                                        ((LivingEntity) entity).damage(calculateDamage(p, (LivingEntity) entity, initM), p);
                                        entity.setFreezeTicks(entity.getMaxFreezeTicks()*6);
                                        spawnSpellHitParticles(ground.clone().add(0, 1,0));
                                    }
                                }
                            }
                        }
                        spawnFromBelowParticles(ground.clone());
                        restoreBlocks(old);
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
        }.runTaskLater(magicPlugin, 10);
    }

    private void shootIce(LeftClickExecute execute, double initM, int vectorRotate){
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
        colors[0] = Color.fromRGB(92, 226, 250);
        colors[1] = Color.fromRGB(214, 249, 255);
        colors[2] = Color.fromRGB(2, 188, 250);

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

                particleApi.spawnColoredParticles(as.getLocation(), colors[r.nextInt(colors.length)], 1, 10, 0.1, 0.1, 0.1);
                if (r.nextBoolean()) particleApi.spawnParticles(as.getLocation(), Particle.SNOWFLAKE, r.nextInt(10)+1, 0.1, 0.1, 0.1, 0.01);
                as.teleport(as.getLocation().add(v.normalize().multiply(speed)));

                for (Entity entity : as.getLocation().getChunk().getEntities()){
                    if (!as.isDead()){
                        if (entity instanceof ArmorStand){
                            continue;
                        }
                        if (as.getLocation().distanceSquared(entity.getLocation()) <= 3.8){
                            if (!entity.equals(p)){
                                if (entity instanceof LivingEntity){
                                    ((LivingEntity) entity).damage(calculateDamage(p, (LivingEntity) entity, initM), p);
                                    entity.setFreezeTicks(entity.getMaxFreezeTicks()*6);
                                    spawnSpellHitParticles(as.getLocation().clone());
                                    as.remove();
                                    cancel();
                                }
                            }
                        }
                    }
                }

                boolean l = as.getLocation().clone().getBlock().isLiquid();
                if (!as.getLocation().clone().getBlock().isPassable() || l){
                    if (l) setColdBlock(as.getLocation().clone().add(0, 1,0), null);
                    if (!as.isDead()){
                        spawnSpellHitParticles(as.getLocation().clone());
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

    private void spawnSpellHitParticles(Location l){
        Color[] colors = new Color[3];
        colors[0] = Color.fromRGB(92, 226, 250);
        colors[1] = Color.fromRGB(214, 249, 255);
        colors[2] = Color.fromRGB(2, 188, 250);
        particleApi.spawnParticles(l.clone(),
                Particle.SNOWFLAKE, 40, 0.4, 0.4, 0.4, 0.01);
        particleApi.spawnColoredParticles(l, colors[new Random().nextInt(colors.length)], 1, 10, 0.7, 0.7, 0.7);
        particleApi.spawnParticles(l, Particle.SNOWBALL, 60, 1, 1, 1, 1);
        l.getWorld().playSound(l, Sound.BLOCK_GLASS_BREAK, 1, 1+new Random().nextFloat());
    }

    private void spawnFromBelowParticles(Location ground){
        Color[] colors = new Color[6];
        colors[0] = Color.fromRGB(92, 226, 250);
        colors[1] = Color.fromRGB(214, 249, 255);
        colors[2] = Color.fromRGB(2, 188, 250);
        colors[3] = Color.WHITE;
        colors[4] = Color.WHITE;
        colors[5] = Color.WHITE;
        Random r = new Random();
        for (int i = 0; i < 12; i++){
            Location l = ground.clone().add(new Vector(r.nextFloat(4)-2, 2.3, r.nextFloat(4)-2));
            particleApi.drawColoredLine(ground.clone(), l, 1, colors[r.nextInt(colors.length)], 1, 0);
        }
    }

    private void spawnIceSpikesParticles(Location l){
        Color[] colors = new Color[6];
        colors[0] = Color.fromRGB(92, 226, 250);
        colors[1] = Color.fromRGB(214, 249, 255);
        colors[2] = Color.fromRGB(2, 188, 250);
        colors[3] = Color.WHITE;
        colors[4] = Color.WHITE;
        colors[5] = Color.WHITE;
        Random r = new Random();
        for (int i = 0; i < 32; i++){
            Location loc = l.clone().add(new Vector((r.nextFloat(2)-1)*8, (r.nextFloat(2)-1)*8, (r.nextFloat(2)-1)*8));
            particleApi.drawColoredLine(l.clone(), loc, 1, colors[r.nextInt(colors.length)], 1, 0);
        }
    }

    private void phaseChange(LeftClickExecute execute){
        for (Block b : getBlocksToFreeze(execute.getPlayer().getLocation().add(execute.getPlayer().getLocation().getDirection().normalize().multiply(3)), 4, execute.getPlayer())){
            if (execute.getPlayer().isSneaking()){
                if (b.getType().equals(Material.ICE) || b.getType().equals(Material.FROSTED_ICE)){
                    b.setType(Material.WATER);
                    continue;
                }
            } else {
                if (b.getType().equals(Material.WATER)){
                    b.setType(Material.FROSTED_ICE);
                    continue;
                }
                if (b.getType().equals(Material.LAVA)){
                    b.setType(Material.OBSIDIAN);
                    continue;
                }
            }
        }
    }

    private ArrayList<Block> getBlocksToFreeze(final Location center, final double radius, Player player) {
        final ArrayList<Block> blocks = new ArrayList<>();
        for (final Location l : GeneralMethods.getCircle(center, (int)radius, 1, false, true, 0)) {
            final Block b = l.getBlock();
            loop: for (int i = 1; i <= 1; i++) {
                for (final BlockFace face : this.getBlockFacesTowardsPlayer(center, player)) {
                    if (GeneralMethods.isAir(b.getRelative(face, i).getType())) {
                        blocks.add(b);
                        break loop;
                    }
                }
            }
        }
        return blocks;
    }

    private ArrayList<BlockFace> getBlockFacesTowardsPlayer(final Location center, Player player) {
        final ArrayList<BlockFace> faces = new ArrayList<>();
        final Vector toPlayer = GeneralMethods.getDirection(center, player.getEyeLocation());
        final double[] vars = { toPlayer.getX(), toPlayer.getY(), toPlayer.getZ() };
        for (int i = 0; i < 3; i++) {
            if (vars[i] != 0) {
                faces.add(GeneralMethods.getBlockFaceFromValue(i, vars[i]));
            } else {
                continue;
            }
        }
        return faces;
    }

    private double calculateDamage(Player p, LivingEntity damaged, double m){
        if (p.getLocation().getBlock().getTemperature()<0.5){
            m=m*1.8;
        } else if (p.getLocation().getBlock().getTemperature()>0.94){
            m=m*0.3;
        }
        if (damaged.isFrozen()){
            return 9.0*m;
        }
        return 13.0*m;
    }

    private void setColdBlock(Location loc, Player p){
        Block b = loc.getBlock();
        Block sb = loc.clone().add(0, -1, 0).getBlock();
        if (sb.getType().equals(Material.AIR)){
            if (p!=null && p.isSneaking()){
                sb.setType(Material.ICE);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        sb.setType(Material.AIR);
                    }
                }.runTaskLater(magicPlugin, 80);
            }
            return;
        }
        if (sb.isLiquid()){
            switch (sb.getType()){
                case WATER:
                    sb.setType(Material.FROSTED_ICE);
                    return;
                case LAVA:
                    sb.setType(Material.OBSIDIAN);
                    return;
            }
        }
        if (b.getType().equals(Material.AIR)){
            if (sb.getType().equals(Material.FROSTED_ICE) || sb.getType().equals(Material.ICE) || sb.getType().equals(Material.OBSIDIAN) || sb.getType().equals(Material.SNOW)){
                return;
            }
            b.setType(Material.SNOW);
            return;
        }
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (p.getFireTicks()>20){
                    p.setFireTicks(20);
                }
                p.setFreezeTicks(0);
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.SNOWFLAKE, 5, 0.3, 0.3, 0.3, 0.01);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 15);
        return r;
    }
}
