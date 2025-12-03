package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.cooldowns.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.*;
import static net.pullolo.magicabilities.cooldowns.Cooldowns.cooldowns;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.misc.GeneralMethods.rotateVector;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class UnstablePower extends WarpPower implements IdlePower {
    private static final String warp_default = "warp.default";
    private static final String unstable_heal_by_others = "unstable.heal-by-others";
    private static final String unstable_heal_self = "unstable.heal-self";
    private final Random random = new Random();
    public UnstablePower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof MoveExecute){
            moveExecution((MoveExecute) ex);
            return;
        }
        if (ex instanceof InteractedOnByExecute){
            onInteracted((InteractedOnByExecute) ex);
            return;
        }
        if (ex instanceof DamagedExecute){
            onDamage((DamagedExecute) ex);
            return;
        }
        if (ex instanceof DeathExecute){
            onDeath((DeathExecute) ex);
            return;
        }
        if (ex instanceof ConsumeExecute){
            onEat(ex.getPlayer(), ((PlayerItemConsumeEvent) ex.getRawEvent()).getItem().getType());
            return;
        }
        if (!isEnabled()) return;
        if (ex instanceof SneakExecute){
            executeSneak((SneakExecute) ex);
            return;
        }
        if (ex instanceof LeftClickExecute){
            executeLeftClick((LeftClickExecute) ex);
            return;
        }
    }

    private void executeSneak(SneakExecute ex) {
        final Player p = ex.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 2:
                if (CooldownApi.isOnCooldown(unstable_heal_self, p)) {
                    onCooldownInfo(CooldownApi.getCooldownForPlayerLong(unstable_heal_self, p));
                    return;
                }
                healSelf(p);
                CooldownApi.addCooldown(unstable_heal_self, p, cooldowns.get(unstable_heal_self));
                return;
        }
    }

    private void healSelf(Player p){
        if (random.nextInt(200)==0) {
            glitch(p, 10, 3);
            return;
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.7f);
        heal(p);
        new BukkitRunnable() {
            int i = 0;
            final int length = 20;
            @Override
            public void run() {
                i++;

                Location l = p.getLocation().clone().add(0, ((double) i*2/length), 0);
                particleApi.spawnColoredParticles(l.clone().add(rotateVector(new Vector(1, 0, 0), i*10)), Color.LIME, 1, 1, 0, 0, 0);
                particleApi.spawnColoredParticles(l.clone().add(rotateVector(new Vector(-1, 0, 0), i*10)), Color.LIME, 1, 1, 0, 0, 0);

                if (i>length){
                    cancel();
                    return;
                }
            }
        }.runTaskTimer(magicPlugin, 0, 1);
    }

    private void executeLeftClick(LeftClickExecute ex){
        final Player p = ex.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 0:
                if (CooldownApi.isOnCooldown(warp_default, p)) {
                    onCooldownInfo(CooldownApi.getCooldownForPlayerLong(warp_default, p));
                    return;
                }
                Location pl = p.getLocation().clone().add(0, 1, 0).add(p.getLocation().getDirection().clone().normalize().multiply(2));
                ArrayList<Entity> tpEd = new ArrayList<>();
                notifyPlayers(p, pl, getDest().clone().add(0, 1, 0));
                openRift(pl, getDest().clone().add(0, 1, 0), tpEd, 15);
                openRift(getDest().clone().add(0, 1, 0), pl, tpEd, 15);
                CooldownApi.addCooldown(warp_default, p, cooldowns.get(warp_default)*1.2);
                return;
            case 1:
                if (CooldownApi.isOnCooldown(warp_default, p)) {
                    onCooldownInfo(CooldownApi.getCooldownForPlayerLong(warp_default, p));
                    return;
                }
                switchDim(p);
                CooldownApi.addCooldown(warp_default, p, cooldowns.get(warp_default)*1.2);
                return;
        }
    }

    private void onDamage(DamagedExecute execute) {
        Player p = execute.getPlayer();
        EntityDamageEvent event = (EntityDamageEvent) execute.getRawEvent();
        if (event.getCause().equals(EntityDamageEvent.DamageCause.FALL) && random.nextInt(3)==0){
            event.setCancelled(true);
            return;
        }
        if (event.getFinalDamage()>p.getHealth() && random.nextInt(5)==0){
            event.setCancelled(true);
            p.getWorld().playSound(p.getLocation(), Sound.ITEM_TOTEM_USE, 1, 1);
            p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 1));
            p.setFireTicks(0);
            particleApi.spawnParticles(p.getLocation(), Particle.TOTEM_OF_UNDYING, 100, 0.4, 0.4, 0.4, 0.3);
            explode(p);
            return;
        }
        if (random.nextBoolean()) glitch(p, 10, 3);
        if (random.nextInt(50)==0) switchDim(p);
        if (random.nextInt(50)==0) p.setFireTicks(60);
    }

    private void onInteracted(InteractedOnByExecute ex) {
        Player p = ex.getPlayer();
        if (CooldownApi.isOnCooldown(unstable_heal_by_others, p)) {
            onCooldownInfo(CooldownApi.getCooldownForPlayerLong(unstable_heal_by_others, p));
            return;
        }
        if (random.nextInt(40)==0){
            heal(p);
            CooldownApi.addCooldown(unstable_heal_by_others, p, cooldowns.get(unstable_heal_by_others));
        } else if (random.nextInt(70)==0){
            explode(p);
        } else particleApi.spawnParticles(p.getLocation(), Particle.LARGE_SMOKE, 5, 1, 1, 1, 0.2);
    }

    private void explode(Player p) {
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1.5f);
        particleApi.spawnParticles(p.getLocation(), Particle.EXPLOSION, 2, 0 ,0, 0, 1);
        spawnExplodeParticles(p.getLocation());
        for (Entity e : p.getNearbyEntities(6, 6, 6)){
            if (!(e instanceof Damageable)) continue;
            if (e.equals(p)) continue;
            ((Damageable) e).damage(16, p);
        }
    }

    private void spawnExplodeParticles(Location l){
        Color[] colors = new Color[6];
        colors[0] = Color.fromRGB(0, 109, 181);
        colors[1] = Color.fromRGB(33, 0, 181);
        colors[2] = Color.fromRGB(255, 0, 208);
        colors[3] = Color.fromRGB(255, 0, 0);
        colors[4] = Color.fromRGB(46, 126, 255);
        colors[5] = Color.PURPLE;
        Random r = new Random();
        for (int i = 0; i < 16; i++){
            Location loc = l.clone().add(new Vector((r.nextFloat(2)-1)*8, (r.nextFloat(2)-1)*8, (r.nextFloat(2)-1)*8));
            particleApi.drawColoredLine(l.clone(), loc, 1, colors[r.nextInt(colors.length)], 1, 0);
        }
    }

    private void moveExecution(MoveExecute ex) {
        Player p = ex.getPlayer();
        if (random.nextInt(50)==0){
            particleApi.spawnParticles(p.getLocation(), Particle.ENCHANT, 30, 1, 1, 1, 0.1);
        }
        if (random.nextInt(400)==0){
            p.setVelocity(p.getLocation().getDirection().multiply(2).add(new Vector(0, 2, 0)));
        }
        if (random.nextInt(9000)==0){
            switchDim(p);
        }
    }

    public void onEat(Player p, Material item){
        p.addPotionEffect(new PotionEffect(PotionEffectType.values()[random.nextInt(PotionEffectType.values().length)], 100, 10));
        nearTp(p, 2);
        if (item.equals(Material.GOLDEN_APPLE) || random.nextInt(10)==0){
            heal(p);
            return;
        }
        if (item.equals(Material.ENCHANTED_GOLDEN_APPLE)){
            for (int i =0; i<10;i++) heal(p);
        }
    }

    private void onDeath(DeathExecute execute){
        Player p = execute.getPlayer();
        PlayerDeathEvent event = (PlayerDeathEvent) execute.getRawEvent();
        explode(p);
        if (p.getAttribute(Attribute.MAX_HEALTH).getBaseValue()<3){
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(2);
            event.setKeepInventory(false);
            event.setKeepLevel(false);

        } else {
            event.setKeepInventory(true);
            event.setKeepLevel(true);
            event.getDrops().clear();
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(p.getAttribute(Attribute.MAX_HEALTH).getBaseValue()-2);
        }
    }

    private void heal(Player p){
        particleApi.spawnParticles(p.getLocation(), Particle.HAPPY_VILLAGER, 30, 0.3, 0.3, 0.3, 1);
        if (p.getAttribute(Attribute.MAX_HEALTH).getBaseValue()<20){
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(p.getAttribute(Attribute.MAX_HEALTH).getBaseValue()+2);
        }
        if (p.getAttribute(Attribute.MAX_HEALTH).getBaseValue()>20){
            p.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20);
        }
    }

    private void switchDim(Player p) {
        List<World> worlds = Bukkit.getWorlds();
        worlds.remove(p.getWorld());
        Location d = new Location(worlds.get(random.nextInt(worlds.size())), p.getLocation().getX(), p.getLocation().getY(), p.getLocation().getZ()).add(0, 1, 0);
        ArrayList<Entity> entities = new ArrayList<>();
        Location l = p.getLocation().clone().add(0, 1, 0);
        notifyPlayers(p, l, d.clone().add(0, 1, 0).clone().add(0, 1, 0));
        openRift(l, d.clone().add(0, 1, 0), entities, 5);
        openRift(d.clone().add(0, 1, 0), l, entities, 5);
        particleApi.spawnParticles(p.getLocation(), Particle.ELECTRIC_SPARK, 100, 1, 1, 1, 0.5);
        p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 2));
        p.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 200, 2));
    }

    private void nearTp(Player p, int range){
        p.teleport(p.getLocation().clone().add(-range+(random.nextInt(range*2)+1), -range+(random.nextInt(range*2)+1), -range+(random.nextInt(range*2)+1)));
        particleApi.spawnParticles(p.getLocation(), Particle.ELECTRIC_SPARK, 100, 1, 1, 1, 0.5);
    }

    private void glitch(Player p, int range, int delay){
        Location original = p.getLocation().clone();
        nearTp(p, range);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!original.getWorld().getEnvironment().equals(p.getLocation().getWorld().getEnvironment())) return;
                p.teleport(original);
                particleApi.spawnParticles(p.getLocation(), Particle.ELECTRIC_SPARK, 100, 1, 1, 1, 0.5);
            }
        }.runTaskLater(magicPlugin, delay);
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                if (random.nextInt(10)==0){
                    glitch(p, 5, 4);
                }
                if (random.nextInt(100)==0){
                    switchDim(p);
                }
                if (random.nextInt(120)==0){
                    p.getWorld().spawn(p.getLocation(), LightningStrike.class);
                }

                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.ENCHANT, 5, 0.3, 0.3, 0.3, 0.01);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 40);
        return r;
    }

    @Override
    public String getAbilityName(int ability) {
        switch (ability){
            case 0:
                return "&dRift";
            case 1:
                return "&dSwitch Dimension";
            case 2:
                return "&aSmall Heal";
            default:
                return "&7none";
        }
    }
}
