package net.zic.zenithlib.common;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.cooldown.EntityCooldownHandler;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;
import net.zic.zenithlib.input.action.PlayerActionManager;
import net.zic.zenithlib.stats.ZenithStatHandler;
import net.zic.zenithlib.stats.ZenithStatHolder;

import java.util.function.Supplier;

public class ZenithAttachments {
    private static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, ZenithLib.MOD_ID);

    public static final Supplier<AttachmentType<PlayerActionManager>> ACTION_MANAGER = ATTACHMENT_TYPES.register(
            "action_manager",()-> AttachmentType.builder(
                    (holder)-> new PlayerActionManager((Player) holder)
            ).build()
    );
    public static final Supplier<AttachmentType<ZenithAttributeHolder>> ATTRIBUTE_HOLDER = ATTACHMENT_TYPES.register(
            "attribute_holder",()-> AttachmentType.builder(
                    (holder)-> new ZenithAttributeHolder((LivingEntity) holder)
            )
                    .sync(new ZenithAttributeHolder.SyncHandler())
                    .serialize(new ZenithAttributeHolder.Provider())
                    .copyOnDeath()
                    .build()
    );
    public static final Supplier<AttachmentType<ZenithStatHolder>> STAT_HOLDER = ATTACHMENT_TYPES.register(
            "stat_holder",()-> AttachmentType.builder(
                            (holder)-> new ZenithStatHolder((LivingEntity) holder)
                    )
                    .sync(new ZenithStatHolder.SyncHandler())
                    .copyOnDeath()
                    .build()
    );
    public static final Supplier<AttachmentType<EntityCooldownHandler>> COOLDOWN_HANDLER = ATTACHMENT_TYPES.register(
            "cooldown_handler",
            ()->AttachmentType.builder(
                    (holder)->new EntityCooldownHandler((Entity) holder)
            )
            .serialize(new EntityCooldownHandler.Provider())
            .sync(new EntityCooldownHandler.SyncHandler())
            .copyOnDeath()
            .build()
    );

    public static void register(IEventBus bus){
        ATTACHMENT_TYPES.register(bus);
    }

}
