package net.pullolo.magicabilities.powers.custom;

import net.pullolo.magicabilities.misc.CooldownApi;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.executions.Execute;
import net.pullolo.magicabilities.powers.executions.LeftClickExecute;
import net.pullolo.magicabilities.powers.executions.RightClickExecute;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Random;

import static net.pullolo.magicabilities.MagicAbilities.magicPlugin;
import static net.pullolo.magicabilities.MagicAbilities.particleApi;
import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class WarpPower extends Power {
    protected Location dest;
    public WarpPower(Player owner) {
        super(owner);
        dest = Bukkit.getWorlds().get(0).getSpawnLocation();
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
                if (CooldownApi.isOnCooldown("WARP-DEF", p)) return;
                Location pl = p.getLocation().clone().add(0, 1, 0).add(p.getLocation().getDirection().clone().normalize().multiply(2));
                ArrayList<Entity> tpEd = new ArrayList<>();
                notifyPlayers(p, pl, getDest().clone().add(0, 1, 0));
                openRift(pl, getDest().clone().add(0, 1, 0), tpEd, 15);
                openRift(getDest().clone().add(0, 1, 0), pl, tpEd, 15);
                if ((this instanceof SuperiorWarpPower)) {
                    CooldownApi.addCooldown("WARP-DEF", p, 120);
                } else {
                    CooldownApi.addCooldown("WARP-DEF", p, 180);
                }
                return;
        }
    }

    protected void openRift(Location l ,Location dest, ArrayList<Entity> tped, int time){
        new BukkitRunnable() {
            int i = 0;
            final int timeOpen = time;
            @Override
            public void run() {

                spawnPortalParticles(l);
                for (Entity e : l.getWorld().getNearbyEntities(l, 1, 1, 1)){
                    if (tped.contains(e)) continue;
                    if (e instanceof Player) ((Player) e).playSound(e.getLocation(), Sound.BLOCK_END_PORTAL_FRAME_FILL, 1, 1.3f);
                    e.teleport(dest);
                    tped.add(e);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (!e.isDead()) tped.remove(e);
                        }
                    }.runTaskLater(magicPlugin, 60);
                    if (e instanceof Player){
                        ((Player) e).playSound(e, Sound.BLOCK_PORTAL_TRAVEL, 0.4f, 2f);
                    }
                }

                i++;
                if (i>timeOpen*4){
                    cancel();
                    return;
                };
            }
        }.runTaskTimer(magicPlugin, 0, 5);
    }

    private void spawnPortalParticles(Location l){
        Color[] colors = new Color[4];
        colors[0] = Color.fromRGB(139,46,222);
        colors[1] = Color.fromRGB(100,20,212);
        colors[2] = Color.fromRGB(206,46,222);
        colors[3] = Color.PURPLE;
        Random r = new Random();
        for (int i = 0; i < 12; i++){
            Location loc = l.clone().add(new Vector((r.nextFloat(2)-1)*1, (r.nextFloat(2)-1)*1, (r.nextFloat(2)-1)*1));
            particleApi.drawColoredLine(l.clone(), loc, 1, colors[r.nextInt(colors.length)], 1, 0);
        }
    }

    protected void notifyPlayers(Player player ,Location l, Location dest) {
        if (this instanceof SuperiorWarpPower) return;
        Random r = new Random();
        for (Player p : Bukkit.getOnlinePlayers()){
            if (p.equals(player)) continue;
            if (!players.containsKey(p)) continue;
            if (!(players.get(p).getPower() instanceof SuperiorWarpPower)) continue;
            String xStyle = r.nextBoolean() ? ChatColor.MAGIC + "" : "";
            String yStyle = r.nextBoolean() ? ChatColor.MAGIC + "" : "";
            String zStyle = r.nextBoolean() ? ChatColor.MAGIC + "" : "";
            String wStyle = r.nextBoolean() ? ChatColor.MAGIC + "" : "";
            p.sendMessage(ChatColor.GRAY + "A rift has been opened at" + ChatColor.LIGHT_PURPLE +
                    " " + Math.round(l.getX()) + " " + Math.round(l.getY()) + " " + Math.round(l.getZ()) + ChatColor.GRAY + " in " + ChatColor.LIGHT_PURPLE +
                    l.getWorld().getName() + ChatColor.GRAY + " leading to " + ChatColor.LIGHT_PURPLE +
                    xStyle + Math.round(dest.getX()) + " " + yStyle + Math.round(dest.getY()) + " " + zStyle + Math.round(dest.getZ()) + ChatColor.GRAY + " in " + ChatColor.LIGHT_PURPLE + wStyle +
                    dest.getWorld().getName());
        }
    }

    public Location getDest() {
        return dest;
    }

    public void setDest(Location dest) {
        this.dest = dest;
    }

    @Override
    public String getAbilityName(int ability){
        switch (ability){
            case 0:
                return "&dRift";
            default:
                return "&7none";
        }
    }
}
