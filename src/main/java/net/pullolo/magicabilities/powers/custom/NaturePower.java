package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.cooldowns.CooldownApi;
import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.Execute;
import net.pullolo.magicabilities.powers.executions.IdleExecute;
import net.pullolo.magicabilities.powers.executions.MoveExecute;
import net.pullolo.magicabilities.powers.executions.SneakExecute;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.TreeType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;
import static net.pullolo.magicabilities.cooldowns.Cooldowns.cooldowns;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class NaturePower extends Power implements IdlePower {
    private static final String nature_tree = "nature.tree";
    public NaturePower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
        if (ex instanceof MoveExecute){
            onMove((MoveExecute) ex);
            return;
        }
        if (!isEnabled()) return;
        if (ex instanceof SneakExecute){
            onSneak((SneakExecute) ex);
            return;
        }
    }

    private void onSneak(SneakExecute execute){
        final Player p = execute.getPlayer();
        if (getPlayerData(p).getBinds().get(players.get(p).getActiveSlot())!=0) return;
        if (CooldownApi.isOnCooldown(nature_tree, p)) {
            onCooldownInfo(CooldownApi.getCooldownForPlayerLong(nature_tree, p));
            return;
        }
        if (p.getWorld().generateTree(p.getLocation().clone().add(p.getLocation().getDirection().clone().setY(0).normalize()), TreeType.values()[new Random().nextInt(TreeType.values().length)])){
            CooldownApi.addCooldown(nature_tree, p, cooldowns.get(nature_tree));
        } else p.sendMessage(ChatColor.RED + "Couldn't spawn a tree here!");
    }

    private void onMove(MoveExecute execute){
        final Player p = execute.getPlayer();
        final PlayerMoveEvent event = (PlayerMoveEvent) execute.getRawEvent();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Misza to gej!");
        }
        if (!(p.isOnGround() && !p.isSwimming())){
            return;
        }
        if (event.getTo().getBlock().getType().equals(Material.AIR) && event.getTo().clone().add(0, -1, 0).getBlock().getType().equals(Material.GRASS_BLOCK)){
            event.getTo().getBlock().setType(Material.SHORT_GRASS);
        }
    }

    @Override
    public BukkitRunnable executeIdle(IdleExecute ex) {
        final Player p = ex.getPlayer();
        BukkitRunnable r = new BukkitRunnable() {
            @Override
            public void run() {
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0), Particle.CHERRY_LEAVES, 10, 0.4, 0.5, 0.4, 1);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 20);
        return r;
    }

    @Override
    public String getAbilityName(int ability) {
        switch (ability){
            case 0:
                return "&aGenerate Tree";
            default:
                return "&7none";
        }
    }
}
