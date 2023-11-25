package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.*;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import static net.pullolo.magicabilities.MagicAbilities.*;

public class IcePower extends Power implements IdlePower {
    public IcePower(Player owner) {
        super(owner);
    }

    @Override
    public void executePower(Execute ex) {
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
    }

    private void executeRightClick(RightClickExecute ex){
        final Player p = ex.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        p.sendMessage("hey r ;))))))");
    }

    private void executeLeftClick(LeftClickExecute ex){
        final Player p = ex.getPlayer();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        p.sendMessage("hey l ;))))))");
    }

    private void moveExecution(MoveExecute ex){
        final Player p = ex.getPlayer();
        final PlayerMoveEvent event = (PlayerMoveEvent) ex.getRawEvent();
        if (!p.equals(getOwner())){
            throw new RuntimeException("Event player does not match the power owner!");
        }
        setColdBlock(event.getTo());
    }

    private void damagedByExecute(DamagedByExecute ex){
        Entity damager = ((EntityDamageByEntityEvent) ex.getRawEvent()).getDamager();
        damager.setFreezeTicks(damager.getMaxFreezeTicks()*4); //around 7 sec
    }

    private void setColdBlock(Location loc){
        Block b = loc.getBlock();
        Block sb = loc.clone().add(0, -1, 0).getBlock();
        if (sb.getType().equals(Material.AIR)){
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
        if (b.isPassable() && !b.isLiquid()){
            if (sb.getType().equals(Material.FROSTED_ICE) || sb.getType().equals(Material.OBSIDIAN)){
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
                particleApi.spawnParticles(p.getLocation().clone().add(0, 1, 0),
                        Particle.SNOWFLAKE, 5, 0.3, 0.3, 0.3, 0.01);
            }
        };
        r.runTaskTimer(magicPlugin, 0, 15);
        return r;
    }
}
