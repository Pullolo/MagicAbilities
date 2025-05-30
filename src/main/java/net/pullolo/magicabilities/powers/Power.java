package net.pullolo.magicabilities.powers;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import net.pullolo.magicabilities.powers.custom.*;
import net.pullolo.magicabilities.powers.executions.Execute;
import org.bukkit.entity.Player;

import static net.pullolo.magicabilities.data.PlayerData.getPlayerData;

public abstract class Power {
    private final Player owner;
    private boolean enabled = true;

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
            case WARP:
                return new WarpPower(p);
            case SUPERIOR_WARP:
                return new SuperiorWarpPower(p);
            case LIGHTNING:
                return new LightningPower(p);
            case UNSTABLE:
                return new UnstablePower(p);
            case ALCOHOLIZM:
                return new AlcoholizmPower(p);
            case SHOGUN:
                return new ShogunPower(p);
            case POTATO:
                return new PotatoPower(p);
            case FIRE:
                return new FirePower(p);
            case WITCHER:
                return new WitcherPower(p);
            case NATURE:
                return new NaturePower(p);
            case TWILIGHT_MIRAGE:
                return new TwilightMirage(p);
            case ETERNITY:
                return new Eternity(p);
            case CURSEWEAVER:
                return new Curseweaver(p);
            default:
                return new Power(p) {
                    @Override
                    public void executePower(Execute ex) {
                        //do nothing
                    }
                };
        }
    }

    public String getAbilityName(int ability) {
        return "&7none";
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void onCooldownInfo(long time){
        String s = (float) ((int) time/100)/10 == 0 ? "0.1" : ((float) ((int) time/100)/10 + "");

        owner.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.RED + "On cooldown for " +
                s + "s."));
    }
}
