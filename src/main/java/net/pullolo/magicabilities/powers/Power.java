package net.pullolo.magicabilities.powers;

import net.pullolo.magicabilities.powers.custom.IcePower;
import net.pullolo.magicabilities.powers.executions.Execute;
import org.bukkit.entity.Player;

public abstract class Power {
    private final Player owner;

    public Power(Player owner) {
        this.owner = owner;
    }

    public abstract void executePower(Execute ex);

    public Player getOwner() {
        return owner;
    }

    public static Power getPowerFromPowerType(Player p, PowerType powerType){
        switch (powerType){
            case ICE:
                return new IcePower(p);
            default:
                return new Power(p) {
                    @Override
                    public void executePower(Execute ex) {
                        //do nothing
                    }
                };
        }
    }
}
