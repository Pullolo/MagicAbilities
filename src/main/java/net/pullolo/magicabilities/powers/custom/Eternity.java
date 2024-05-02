package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.misc.GeneralMethods.rotateVector;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class Eternity extends Power implements IdlePower {
    private double ultMultiplier = 0.8;
    int combo = 0;
    final int reset = 6;
    int timer = 0;
    boolean onUlt = false;
    public Eternity(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof DealDamageExecute){
            onDamage((DealDamageExecute) ex);
            return;
        }
        if (ex instanceof LeftClickExecute){
            executeLeftClick((LeftClickExecute) ex);
            return;
        }
        if (ex instanceof SneakExecute){
            executeSneak((SneakExecute) ex);
        }
    }

    private void executeSneak(SneakExecute ex){
        Player p = ex.getPlayer();
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 1:
                if (CooldownApi.isOnCooldown("ET-2", p)) return;
                blink(p);
                CooldownApi.addCooldown("ET-2", p, 1);
                return;
        }
    }

    private void blink(Player p){
        Location l = p.getLocation().clone().add(0, 1, 0);
        Vector v = p.getLocation().getDirection().clone().normalize();
        int i = 3;
        while (l.clone().add(v).getBlock().isPassable() && l.clone().add(v).add(0, 1, 0).getBlock().isPassable() && i>0){
            l.add(v);
            i--;
        }
        p.teleport(l);
        new BukkitRunnable() {
            @Override
            public void run() {
                p.setVelocity(p.getLocation().getDirection().normalize().multiply(0.3));
            }
        }.runTaskLater(magicPlugin, 1);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1.2f);
    }

    private void executeLeftClick(LeftClickExecute execute){
        final Player p = execute.getPlayer();
        if (onUlt) {
            int i = 0;
            for (Entity e : p.getWorld().getNearbyEntities(p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(2)), 2, 2, 2)){
                if (e.equals(p)) continue;
                if (!(e instanceof LivingEntity)) continue;
                ((LivingEntity) e).damage(18*ultMultiplier, p);
                i++;
            }
            if (i<1){
                ultSlash(p);
            }
            return;
        }
        switch (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())){
            case 0:
                if (CooldownApi.isOnCooldown("ET-1", p)) return;
                ult(p);
                CooldownApi.addCooldown("ET-1", p, 40);
                return;
        }
    }

    private void ult(Player p) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 400, 1));
        tornToOblivion(p);
        p.sendMessage(ChatColor.LIGHT_PURPLE + "Slice them into pieces!");
        onUlt=true;
        new BukkitRunnable() {
            @Override
            public void run() {
                onUlt=false;
                ultMultiplier=0.8;
            }
        }.runTaskLater(magicPlugin, 400);
    }

    private void tornToOblivion(Player p){
        Location center = p.getLocation().clone().add(p.getLocation().getDirection().clone().setY(0).normalize().multiply(2)).add(0, 1.6, 0);
        Location l1 = center.clone().add(rotateVector(p.getLocation().getDirection().clone().setY(0).normalize(), 90).multiply(5));
        Location l2 = center.clone().add(rotateVector(p.getLocation().getDirection().clone().setY(0).normalize(), 90).multiply(-5));
        particleApi.drawColoredLine(l1, l2, 1, Color.PURPLE, 1, 0);
        slashStraight1(p);
        slashStraight2(p);
        for (Entity e : p.getWorld().getNearbyEntities(p.getLocation().clone().add(p.getLocation().getDirection().clone().normalize().multiply(2)), 2, 2, 2)){
            if (e.equals(p)) continue;
            if (!(e instanceof LivingEntity)) continue;
            ((LivingEntity) e).damage(25*ultMultiplier, p);
        }
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1.4f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 0.2f, 2f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1, 1.8f);
    }

    private void onDamage(DealDamageExecute execute){
        final Player p = execute.getPlayer();
        gainImmunity(p);
        if (onUlt){
            ((EntityDamageByEntityEvent) execute.getRawEvent()).setDamage(20*ultMultiplier);
            ultSlash(p);
        }
    }

    private void ultSlash(Player p){
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1, 1.4f);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_WITHER_BREAK_BLOCK, 0.2f, 2f);
        p.setVelocity(p.getLocation().getDirection().clone().normalize().multiply(0.2).setY(p.getVelocity().getY()));
        combo++;
        timer=0;
        switch (combo%4){
            case 0:
                slashStraight1(p);
                return;
            case 1:
                slashStraight2(p);
                return;
            case 2:
                slashDiag(p);
                return;
            case 3:
                slashUp(p);
                return;
        }
    }

    private void slashStraight1(Player p){
        double slashThickness = 0.3;
        double vM = 1.3;
        Vector v = p.getLocation().getDirection().clone().setY(0).normalize();
        final int rotation = 78;
        double height = 1.8;
        for (int i = 0; i < 160; i+=2){
            Location lineStart = p.getLocation().clone().add(0, height, 0).add(rotateVector(v, rotation-i).clone().multiply(vM));
            Location lineEnd = p.getLocation().clone().add(0, height, 0).add(rotateVector(v, rotation-i).clone().multiply(vM).add(rotateVector(v, rotation-i).clone().multiply(slashThickness)));
            particleApi.drawColoredLine(lineStart, lineEnd, 1, Color.PURPLE, 1, 0);
            vM+=0.02;
            height-=0.008;
        }
    }

    private void slashStraight2(Player p){
        double slashThickness = 0.3;
        double vM = 1.3;
        Vector v = p.getLocation().getDirection().clone().setY(0).normalize();
        final int rotation = -78;
        double height = 1.8;
        for (int i = 0; i < 160; i+=2){
            Location lineStart = p.getLocation().clone().add(0, height, 0).add(rotateVector(v, rotation+i).clone().multiply(vM));
            Location lineEnd = p.getLocation().clone().add(0, height, 0).add(rotateVector(v, rotation+i).clone().multiply(vM).add(rotateVector(v, rotation+i).clone().multiply(slashThickness)));
            particleApi.drawColoredLine(lineStart, lineEnd, 1, Color.PURPLE, 1, 0);
            vM+=0.02;
            height-=0.01;
        }
    }

    private void slashDiag(Player p){
        double slashThickness = 0.3;
        double vM = 1.3;
        Vector v = p.getLocation().getDirection().clone().setY(0).normalize();
        final int rotation = 68;
        double height = 2.3;
        for (int i = 0; i < 130; i+=2){
            Location lineStart = p.getLocation().clone().add(0, height, 0).add(rotateVector(v, rotation-i).clone().multiply(vM));
            Location lineEnd = p.getLocation().clone().add(0, height, 0).add(rotateVector(v, rotation-i).clone().multiply(vM).add(rotateVector(v, rotation-i).clone().multiply(slashThickness)));
            particleApi.drawColoredLine(lineStart, lineEnd, 1, Color.PURPLE, 1, 0);
            vM+=0.02;
            height-=0.04;
        }
    }
    private void slashUp(Player p){
        Vector v = p.getLocation().getDirection().clone().setY(0).normalize();
        Vector axis = rotateVector(v.clone(), 90);
        double vM = 1.3;
        int rotation = 68;
        for (int i = 0; i < 100; i+=2){
            for (int j = -10; j<10; j+=5){
                particleApi.spawnColoredParticles(p.getLocation().clone().add(0, 1, 0).add(axis.clone().normalize().multiply((double) j/30))
                        .add(v.clone().multiply(1.2)).add(v.clone().rotateAroundAxis(axis, Math.toRadians(rotation-i)).multiply(vM)), Color.PURPLE, 1, 1, 0, 0, 0);
            }
            vM+=0.02;
        }
    }

    private void gainImmunity(Player p){
        if (CooldownApi.isOnCooldown("ET-0", p)) return;
        p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, onUlt ? 40 : 20, 255));
        CooldownApi.addCooldown("ET-0", p, 2);
    }

    private void resetCombo(){
        timer=0;
        combo=0;
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        if (timer>reset){
            resetCombo();
        }
        timer++;

        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.SPELL_WITCH, 3, 0.1, 0.3, 0.1, 0.01);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 15);
        return r;
    }

    @Override
    public String getAbilityName(int ability) {
        switch (ability){
            case 0:
                return "&dTorn To Oblivion";
            case 1:
                return "&dBlink";
            default:
                return "&7none";
        }
    }
}
