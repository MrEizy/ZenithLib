package net.zic.zenithlib.cooldown;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.zic.zenithlib.network.ByteBufHelpers;

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


    //─────TICK HANDLING───────────────────────────────────────────────
    public void tick(){
        HashSet<Identifier> finished = new HashSet<>();
        for(Identifier identifier : cooldowns.keySet()){
            if(cooldowns.get(identifier).tick()) finished.add(identifier);
        }

        for(Identifier identifier : finished) removeCooldown(identifier);
    }

    //─────NETWORK───────────────────────────────────────────────
    /*
        we do not necessary have to sync it ever tick if we decide to give it ticker on the client.
        in that scenario whatever is hadnling this ticker can keep track and every X ticks sync (configurable?)
     */


    //we do not send the listener with the cooldown
    public void encode(ByteBuf buf){
        ByteBufHelpers.encodeMap(
                cooldowns,
                ByteBufHelpers::encodeIdentifier,
                (cooldown,byteBuf)->{
                    ByteBufHelpers.encodeIdentifier(cooldown.getIdentifier(),byteBuf);
                    byteBuf.writeInt(cooldown.getTicksRemaining());
                },
                buf
        );
    }

    public void decode(ByteBuf buf){
        cooldowns.clear();
        ByteBufHelpers.decodeMap(
                cooldowns,ByteBufHelpers::decodeIdentifier,
                (byteBuf)->new Cooldown(ByteBufHelpers.decodeIdentifier(byteBuf),EMPTY,buf.readInt()),
                buf
        );
    }

    public void save(CompoundTag tag){

    }
    public void load(CompoundTag tag){

    }
}
