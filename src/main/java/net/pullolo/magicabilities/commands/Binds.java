package net.pullolo.magicabilities.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class Binds implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("binds")){
            return false;
        }
        if (!(sender instanceof Player)){
            return false;
        }
        Player p = (Player) sender;
        if (!players.containsKey(p)){
            p.sendMessage(ChatColor.RED + "Something went wrong!");
            return true;
        }
        if (args.length==0){
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7---------------------------"));
            for (int i = 0; i < 9; i++){
                p.sendMessage(ChatColor.translateAlternateColorCodes('&',
                        "&7Slot - &c" + i + "&7: ability - &a"+ players.get(p).getBinds().get(i)));
            }
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7---------------------------"));
            return true;
        }
        if (args.length==1 && args[0].equalsIgnoreCase("reset")){
            players.get(p).resetBinds();
            return true;
        }
        if (args.length==3){
            if (args[0].equalsIgnoreCase("change")){
                try {
                    int s1 = Integer.parseInt(args[1]);
                    int s2 = Integer.parseInt(args[2]);

                    players.get(p).changeBind(s1, s2);
                    return true;
                } catch (Exception e){
                    p.sendMessage(ChatColor.RED + "Something went wrong!");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String s, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("binds")){
            return null;
        }
        if (args.length==1){
            ArrayList<String> completion = new ArrayList<>();
            addToCompletion("change", args[0], completion);
            addToCompletion("reset", args[0], completion);
            return completion;
        }
        if (args.length==2 && args[0].equalsIgnoreCase("change")){
            ArrayList<String> completion = new ArrayList<>();
            for (int i = 0 ; i < 9; i++){
                addToCompletion(String.valueOf(i), args[1], completion);
            }
            return completion;
        }
        if (args.length==3 && args[0].equalsIgnoreCase("change")){
            ArrayList<String> completion = new ArrayList<>();
            for (int i = 0 ; i < 9; i++){
                addToCompletion(String.valueOf(i), args[2], completion);
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
