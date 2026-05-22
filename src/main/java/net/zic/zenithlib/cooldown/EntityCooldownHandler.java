package net.zic.zenithlib.cooldown;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import net.neoforged.neoforge.common.NeoForge;
import net.zic.zenithlib.nbt.NbtHelpers;
import net.zic.zenithlib.network.ByteBufHelpers;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;

//TODO currently listeners are not persistent if the player leaves
public class EntityCooldownHandler {
    //used when user does not define one

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

    public void addCooldown(Identifier identifier,int initialTicks){
        CooldownEvent.Start cooldownStartEvent= new CooldownEvent.Start(entity,identifier,initialTicks);
        NeoForge.EVENT_BUS.post(cooldownStartEvent);
        cooldowns.put(identifier,new Cooldown(cooldownStartEvent.getUpdatedCooldown()));

    }


    public void removeCooldown(Identifier identifier){
        if(!cooldowns.containsKey(identifier)) return;
        NeoForge.EVENT_BUS.post(new CooldownEvent.Finished(entity,identifier));
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
                    byteBuf.writeInt(cooldown.getTicksRemaining());
                },
                buf
        );
    }

    public void decode(ByteBuf buf){
        cooldowns.clear();
        ByteBufHelpers.decodeMap(
                cooldowns,ByteBufHelpers::decodeIdentifier,
                (byteBuf)->new Cooldown(byteBuf.readInt()),
                buf
        );
    }

    public void save(ValueOutput output){
        NbtHelpers.writeMap(
                output,
                "cooldowns",
                cooldowns,
                NbtHelpers::writeIdentifier,
                (out,id,cooldown)->out.putInt(id,cooldown.getTicksRemaining()));
    }
    public void load(ValueInput input){
        NbtHelpers.readMap(
                input,
                "cooldowns",
                cooldowns,
                NbtHelpers::readIdentifier,
                (in,id)->new Cooldown(in.getIntOr(id,0))
        );

    }

    private static class SyncHandle implements AttachmentSyncHandler<EntityCooldownHandler>{

        @Override
        public void write(@NonNull RegistryFriendlyByteBuf buf, EntityCooldownHandler attachment, boolean initialSync) {
            attachment.encode(buf);
        }

        @Override
        public @Nullable EntityCooldownHandler read(@NonNull IAttachmentHolder holder, @NonNull RegistryFriendlyByteBuf buf, @Nullable EntityCooldownHandler previousValue) {
            if(!(holder instanceof Entity entity)) return null;
            if(previousValue == null) previousValue = new EntityCooldownHandler(entity);
            previousValue.decode(buf);
            return previousValue;
        }

        @Override
        public boolean sendToPlayer(@NonNull IAttachmentHolder holder, @NonNull ServerPlayer to) {
            return holder == to;
        }
    }
    private static class Provider implements IAttachmentSerializer<EntityCooldownHandler>{

        @Override
        public EntityCooldownHandler read(@NonNull IAttachmentHolder holder, ValueInput input) {
            if(holder instanceof Entity entity){
                EntityCooldownHandler cooldownHandler = new EntityCooldownHandler(entity);
                cooldownHandler.load(input);
                return cooldownHandler;
            }
            return null;
        }

        @Override
        public boolean write(EntityCooldownHandler attachment, ValueOutput output) {
            attachment.save(output);
            return true;
        }
    }
}
