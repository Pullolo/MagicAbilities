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
    private int activeSlot;
    private BukkitRunnable idlePower = null;
    private final HashMap<Integer, Integer> binds;

    public PowerPlayer(Power power, HashMap<Integer, Integer> binds) {
        this.power = power;
        this.activeSlot=this.power.getOwner().getInventory().getHeldItemSlot();
        this.binds = binds;
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
        if (idlePower!=null) idlePower.cancel();
        idlePower=null;
        Player owner = this.power.getOwner();
        getPlayerData(owner).setPower(power);
        this.power=Power.getPowerFromPowerType(owner, power);
        if (this.power instanceof IdlePower){
            idlePower=((IdlePower) this.power).executeIdle(new IdleExecute(null, owner));
        }
    }

    public void remove(){
        if (idlePower!=null) idlePower.cancel();
        idlePower=null;
    }

    public int getActiveSlot() {
        return activeSlot;
    }

    public void setActiveSlot(int activeSlot) {
        this.activeSlot = activeSlot;
    }

    public void changeBind(int ab, int slot){
        binds.replace(ab, slot);
        getPlayerData(power.getOwner()).setBinds(binds);
    }

    public void resetBinds(){
        binds.clear();
        for (int i = 0; i<9; i++){
            binds.put(i, i);
        }
        getPlayerData(power.getOwner()).setBinds(binds);
    }

    public HashMap<Integer, Integer> getBinds() {
        return binds;
    }
}
