package net.zic.zenithlib.cooldown;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;

public class EntityCooldownHandler {
    //used when user does not define one
    private static final CooldownListener EMPTY = (entity, cooldown) -> {};
    private final Entity entity;
    private final HashMap<Identifier,Cooldown> cooldowns = new HashMap<>();


    public EntityCooldownHandler(Entity entity){
        this.entity = entity;
    }
    public Entity getEntity(){return entity;}


    public void addCooldown(Identifier identifier,int initialTicks,CooldownListener listener){
        cooldowns.put(identifier,new Cooldown(identifier,listener,initialTicks));
    }

    //if you start a cooldown with no Listener it will silently finish
    public void addCooldown(Identifier identifier,int initialTicks){
        cooldowns.put(identifier,new Cooldown(identifier,EMPTY,initialTicks));
    }

    public void removeCooldown(Identifier identifier){
        removeCooldown(identifier,false);
    }

    public void removeCooldown(Identifier identifier,boolean suppressListener){
        Cooldown cooldown = cooldowns.remove(identifier);

        if(!suppressListener && cooldown != null) cooldown.getListener().finished(entity,identifier);
    }
}
