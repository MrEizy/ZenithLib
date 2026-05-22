package net.zic.zenithlib.cooldown;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class EntityCooldownHandler {
    //used when user does not define one
    private static final CooldownListener EMPTY = (entity, cooldown) -> {};
    private final Entity entity;
    private final HashMap<Identifier,Cooldown> cooldowns = new HashMap<>();


    public EntityCooldownHandler(Entity entity){
        this.entity = entity;
    }
    //─────GETTERS───────────────────────────────────────────────
    public Entity getEntity(){return entity;}
    public boolean isOnCooldown(Identifier identifier){return cooldowns.containsKey(identifier);}
    public int getCooldown(Identifier identifier){return cooldowns.containsKey(identifier) ? cooldowns.get(identifier).getTicksRemaining() : 0;}


    //─────SETTERS───────────────────────────────────────────────


    public void setCooldown(Identifier identifier,int ticksRemaining){if(cooldowns.containsKey(identifier)) cooldowns.get(identifier).setTicksRemaining(ticksRemaining);}

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



    public void tick(){
        HashSet<Identifier> finished = new HashSet<>();
        for(Identifier identifier : cooldowns.keySet()){
            if(cooldowns.get(identifier).tick()) finished.add(identifier);
        }

        for(Identifier identifier : finished) removeCooldown(identifier);
    }
}
