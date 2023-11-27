package net.pullolo.magicabilities.commands;

import net.pullolo.magicabilities.powers.PowerType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class Setpower implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("setpower")){
            return false;
        }
        if (args.length!=2) return false;
        try {
            Player target = Bukkit.getPlayer(args[0]);
            PowerType p = PowerType.valueOf(args[1].toUpperCase());
            players.get(target).changePower(p);
            sender.sendMessage(ChatColor.GREEN + "Success!");
        } catch (Exception e){
            sender.sendMessage(ChatColor.RED + "Something went wrong!");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("setpower")){
            return null;
        }
        if (args.length==1){
            ArrayList<String> completion = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()){
                addToCompletion(p.getName(), args[0], completion);
            }
            return completion;
        }
        if (args.length==2){
            ArrayList<String> completion = new ArrayList<>();
            for (PowerType pow : PowerType.values()){
                addToCompletion(pow.toString().toLowerCase(), args[1], completion);
            }
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
