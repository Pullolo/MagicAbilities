package net.pullolo.magicabilities.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;
import static net.pullolo.magicabilities.players.PowerPlayer.players;

public class Enable implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equalsIgnoreCase("enable")){
            return false;
        }
        if (!(sender instanceof Player)){
            return true;
        }
        Player p = (Player) sender;
        if (!players.containsKey(p)) return true;
        players.get(p).getPower().setEnabled(true);
        getPlayerData(p).setEnabled(true);
        p.sendMessage(ChatColor.GREEN + "Your power is now enabled!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        return new ArrayList<>();
    }
}
