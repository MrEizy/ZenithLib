package net.zic.zenithlib.input.action;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

public abstract class ActionEvent extends Event {
    private final LivingEntity entity;
    private final Identifier action;

    protected ActionEvent(LivingEntity entity, Identifier action) {
        this.entity = entity;
        this.action = action;
    }
    public LivingEntity getEntity(){return entity;}
    public Identifier getAction(){return action;}

    public static class Start extends ActionEvent{

        public Start(LivingEntity entity, Identifier action) {
            super(entity, action);
        }
    }
    public static class End extends ActionEvent{

        public End(LivingEntity entity, Identifier action) {
            super(entity, action);
        }
    }
}
