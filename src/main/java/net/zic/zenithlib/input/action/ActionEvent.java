package net.zic.zenithlib.input.action;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public abstract class ActionEvent extends Event {
    private final Player player;
    private final Identifier action;

    protected ActionEvent(Player player, Identifier action) {
        this.player = player;
        this.action = action;
    }
    public Player getPlayer(){return player;}
    public Identifier getAction(){return action;}

    public static class Start extends ActionEvent{

        public Start(Player player, Identifier action) {
            super(player, action);
        }
    }
    public static class End extends ActionEvent{

        public End(Player player, Identifier action) {
            super(player, action);
        }
    }
}
