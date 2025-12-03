package net.pullolo.magicabilities.commands;

import net.pullolo.magicabilities.powers.custom.WarpPower;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class Destination implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("destination")){
            return false;
        }
        if (!(sender instanceof Player)){
            return false;
        }
        Player p = (Player) sender;
        if (!(players.get(p).getPower() instanceof WarpPower)){
            p.sendMessage(ChatColor.RED + "You can't use this command!");
            return true;
        }
        if (args.length==0){
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7---------------------------"));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Destination is set to:"));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7World: &c" + ((WarpPower) players.get(p).getPower()).getDest().getWorld().getName()));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7X: &c" + ((WarpPower) players.get(p).getPower()).getDest().getX()));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Y: &c" + ((WarpPower) players.get(p).getPower()).getDest().getY()));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7Z: &c" + ((WarpPower) players.get(p).getPower()).getDest().getZ()));
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7---------------------------"));
            return true;
        }
        if (args.length==1){
            try {
                ((WarpPower) players.get(p).getPower()).setDest(Bukkit.getPlayer(args[0]).getLocation());
                p.sendMessage(ChatColor.GREEN + "Destination set!");
                return true;
            } catch (Exception e){
                p.sendMessage(ChatColor.RED + "Something went wrong!");
                return true;
            }
        }
        if (args.length==3){
            try {
                Location dest = new Location(p.getWorld(), Double.parseDouble(args[0]), Double.parseDouble(args[1]), Double.parseDouble(args[2]));
                if (p.getLocation().distance(dest)>20000){
                    p.sendMessage(ChatColor.RED + "Distance is to large!");
                    return true;
                }
                if (!isInsideWorldBorder(dest)){
                    p.sendMessage(ChatColor.RED + "Cant set destination outside the world border!");
                    return true;
                }
                ((WarpPower) players.get(p).getPower()).setDest(dest);
                p.sendMessage(ChatColor.GREEN + "Destination set!");
                return true;
            } catch (Exception e){
                p.sendMessage(ChatColor.RED + "Something went wrong!");
                return true;
            }
        }
        return true;
    }

    private boolean isInsideWorldBorder(Location loc) {
        World world = loc.getWorld();
        if (world == null) {
            return false;
        }

        WorldBorder border = world.getWorldBorder();
        Location center = border.getCenter();
        double size = border.getSize() / 2.0;

        double x = loc.getX() - center.getX();
        double z = loc.getZ() - center.getZ();

        return Math.abs(x) <= size && Math.abs(z) <= size;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("destination")){
            return null;
        }
        if (!(sender instanceof Player)){
            return null;
        }
        Player p = (Player) sender;
        if (args.length==1){
            ArrayList<String> completion = new ArrayList<>();
            for (Player pp : Bukkit.getOnlinePlayers()){
                addToCompletion(pp.getName(), args[0], completion);
            }
            addToCompletion(String.valueOf(p.getLocation().getX()), args[0], completion);
            return completion;
        }
        if (args.length==2){
            ArrayList<String> completion = new ArrayList<>();
            addToCompletion(String.valueOf(p.getLocation().getY()), args[1], completion);
            return completion;
        }
        if (args.length==3){
            ArrayList<String> completion = new ArrayList<>();
            addToCompletion(String.valueOf(p.getLocation().getZ()), args[2], completion);
            return completion;
        }
        return new ArrayList<>();
    }

    private void addToCompletion(String arg, String userInput, List<String> completion){
        if (arg.regionMatches(true, 0, userInput, 0, userInput.length()) || userInput.length() == 0){
            completion.add(arg);
        }
    }
}
