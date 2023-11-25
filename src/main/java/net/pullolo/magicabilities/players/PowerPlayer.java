package net.pullolo.magicabilities.players;

import net.pullolo.magicabilities.powers.IdlePower;
import net.pullolo.magicabilities.powers.Power;
import net.pullolo.magicabilities.powers.PowerType;
import net.pullolo.magicabilities.powers.executions.IdleExecute;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;

public class PowerPlayer {
    public static final HashMap<Player, PowerPlayer> players = new HashMap<>();

    private Power power;
    private BukkitRunnable idlePower = null;

    public PowerPlayer(Power power) {
        this.power = power;
        if (players.containsKey(power.getOwner())){
            throw new RuntimeException("Power players Hashmap already has this Player!");
        }
        players.put(power.getOwner(), this);
        if (power instanceof IdlePower){
            idlePower=((IdlePower) power).executeIdle(new IdleExecute(null, power.getOwner()));
        }
    }

    public Power getPower() {
        return power;
    }

    public void changePower(PowerType power){
        idlePower.cancel();
        idlePower=null;
        Player owner = this.power.getOwner();
        getPlayerData(owner).setPower(power);
        this.power=Power.getPowerFromPowerType(owner, power);
        if (this.power instanceof IdlePower){
            idlePower=((IdlePower) this.power).executeIdle(new IdleExecute(null, owner));
        }
    }

    public void remove(){
        idlePower.cancel();
        idlePower=null;
    }
}
