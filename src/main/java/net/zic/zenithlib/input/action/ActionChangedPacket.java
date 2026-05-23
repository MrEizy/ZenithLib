package net.zic.zenithlib.input.action;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.common.ZenithAttachments;

public record ActionChangedPacket(Identifier identifier,boolean isDown) implements CustomPacketPayload {
    public static final Type<ActionChangedPacket> TYPE = new Type<>(Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID,"action_changed_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ActionChangedPacket> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            ActionChangedPacket::identifier,
            ByteBufCodecs.BOOL,
            ActionChangedPacket::isDown,
            ActionChangedPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    public static void handlePayload(ActionChangedPacket payload, IPayloadContext context) {
        context.enqueueWork(()->{
            context.player().getData(ZenithAttachments.ACTION_MANAGER).handleActionUpdate(payload.identifier,payload.isDown);
        });
    }
}
