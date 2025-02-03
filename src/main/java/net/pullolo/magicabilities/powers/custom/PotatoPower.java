package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.cooldowns.CooldownApi;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.ConsumeExecute;
import net.pullolo.magicabilities.powers.executions.Execute;
import net.pullolo.magicabilities.powers.executions.RightClickExecute;
import net.pullolo.magicabilities.powers.executions.SneakExecute;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;
import static net.pullolo.magicabilities.cooldowns.Cooldowns.cooldowns;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class PotatoPower extends Power {
    private static final String potato_get = "potato.get";
    private static final String potato_shoot = "potato.shoot";
    public PotatoPower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof ConsumeExecute){
            consumeExecute((ConsumeExecute) ex);
            return;
        }
        if (!isEnabled()) return;
        if (ex instanceof RightClickExecute){
            executeRightClick((RightClickExecute) ex);
            return;
        }
        if (ex instanceof SneakExecute){
            sneakExecute((SneakExecute) ex);
            return;
        }
    }

    private void sneakExecute(SneakExecute execute){
        final Player p = execute.getPlayer();
        if (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())!=0) return;
        if (CooldownApi.isOnCooldown(potato_get, p)) {
            onCooldownInfo(CooldownApi.getCooldownForPlayerLong(potato_get, p));
            return;
        }
        p.getInventory().addItem(new ItemStack(Material.POTATO, 1));
        CooldownApi.addCooldown(potato_get, p, cooldowns.get(potato_get));
        return;
    }

    private void consumeExecute(ConsumeExecute execute){
        final Player p = execute.getPlayer();
        final PlayerItemConsumeEvent event = (PlayerItemConsumeEvent) execute.getRawEvent();
        if (event.getItem().getType().equals(Material.POTATO)){
            event.setCancelled(true);
            return;
        }
        if (event.getItem().getType().toString().toLowerCase().contains("potato")){
            p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 300, 0));
            p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 300, 0));
            return;
        }
        p.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 300, 1));
        p.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 600, 0));
    }

    private void executeRightClick(RightClickExecute execute){
        final Player p = execute.getPlayer();
        if (!(p.getInventory().getItemInMainHand().getType().equals(Material.POTATO))) return;
        ((PlayerInteractEvent) execute.getRawEvent()).setCancelled(true);
        if (CooldownApi.isOnCooldown(potato_shoot, p)) {
            onCooldownInfo(CooldownApi.getCooldownForPlayerLong(potato_shoot, p));
            return;
        }
        p.getInventory().getItemInMainHand().setAmount(p.getInventory().getItemInMainHand().getAmount()-1);
        throwPotato(p);
        CooldownApi.addCooldown(potato_shoot, p, cooldowns.get(potato_shoot));
    }

    private void throwPotato(Player p){
        ArmorStand as = p.getWorld().spawn(p.getLocation().add(0, 1.5, 0), ArmorStand.class, en -> {
            en.setVisible(false);
            en.setGravity(false);
            en.setSmall(true);
            en.setMarker(true);
            en.setSmall(true);
            en.getEquipment().setItemInMainHand(new ItemStack(Material.POTATO));
            en.setRightArmPose(new EulerAngle(352,0,0));
        });

        Location dest = p.getLocation().add(p.getLocation().getDirection().multiply(10));
        Vector v = dest.subtract(p.getLocation()).toVector().normalize();
        HashMap<Particle, Double> particlesOpt = new HashMap<>();
        particlesOpt.put(Particle.CRIT, 1.0);

        int distance = 40;
        int s = 1;
        new BukkitRunnable() {
            Location l1 = as.getLocation().clone();
            int i = 0;
            @Override
            public void run() {
                if (p == null){
                    as.remove();
                    cancel();
                }

                as.teleport(as.getLocation().add(v.clone().multiply(s)));
                for (Entity entity : as.getLocation().getChunk().getEntities()){
                    if (!as.isDead()){
                        if (entity.equals(as)) continue;
                        if (!(entity instanceof LivingEntity)) continue;
                        if (entity.equals(p)) continue;
                        if (as.getLocation().distanceSquared(entity.getLocation()) > 3.5) continue;
                        ((LivingEntity) entity).damage(2.5, p);
                        as.remove();
                        cancel();
                    }
                }

                Location l2 = as.getLocation().clone();
                particleApi.drawMultiParticleLine(l1.clone().add(0, 0.4, 0), l2.clone().add(0, 0.4, 0), 0.06, particlesOpt, 0);

                l1 = l2.clone();

                if (!as.getLocation().add(0, 1, 0).getBlock().isPassable()){
                    if (!as.isDead()){
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
    public String getAbilityName(int ability){
        switch (ability){
            case 0:
                return "&6Get Potato";
            default:
                return "&7none";
        }
    }
}
