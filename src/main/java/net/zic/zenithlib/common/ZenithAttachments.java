package net.zic.zenithlib.common;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;
import net.zic.zenithlib.input.action.PlayerActionManager;

import java.util.function.Supplier;

public class ZenithAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ZenithLib.MOD_ID);

    public static final Supplier<AttachmentType<PlayerActionManager>> ACTION_MANAGER = ATTACHMENT_TYPES.register(
            "action_manager",()-> AttachmentType.builder(
                    (holder)-> new PlayerActionManager((ServerPlayer) holder)
            ).build()
    );
    public static final Supplier<AttachmentType<EntityCooldownHandler>> COOLDOWN_HANDLER = ATTACHMENT_TYPES.register(
            "cooldown_handler",
            ()->AttachmentType.builder(
                    (holder)->new EntityCooldownHandler((Entity) holder)
            )
            .serialize(new EntityCooldownHandler.Provider())
            .sync(new EntityCooldownHandler.SyncHandler())
            .build()
    );

    public static void register(IEventBus bus){
        ATTACHMENT_TYPES.register(bus);
    }

}
