package net.zic.zenithlib.cooldown;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.Event;

public abstract class CooldownEvent extends Event {
    private final Entity entity;
    private final Identifier cooldown;

    protected CooldownEvent(Entity entity, Identifier cooldown) {
        this.entity = entity;
        this.cooldown = cooldown;

    }



    public Entity getEntity(){return entity;}
    public Identifier getCooldownIdentifier(){return cooldown;}


    public static class Start extends CooldownEvent{
        private final int initialCooldown;
        private int updatedCooldown;
        protected Start(Entity entity, Identifier cooldown, int initialCooldown) {
            super(entity, cooldown);
            this.initialCooldown = initialCooldown;
            this.updatedCooldown =initialCooldown;
        }
        public int getInitialCooldown(){return initialCooldown;}
        public int getUpdatedCooldown(){return updatedCooldown;}
        public void setUpdatedCooldown(int newCooldown){this.updatedCooldown = newCooldown;}
    }

    public static class Finished extends CooldownEvent {
        protected Finished(Entity entity, Identifier cooldown) {
            super(entity, cooldown);
        }
    }
}
