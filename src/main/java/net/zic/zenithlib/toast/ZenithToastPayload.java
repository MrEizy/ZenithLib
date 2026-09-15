package net.zic.zenithlib.toast;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.toast.client.ZenithToast;

/** Clientbound payload carrying a ZenithLib toast description. */
public record ZenithToastPayload(ZenithToastData data) implements CustomPacketPayload {
    public static final Type<ZenithToastPayload> TYPE = new Type<>(
            Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID, "show_toast")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ZenithToastPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ZenithToastData.STREAM_CODEC,
                    ZenithToastPayload::data,
                    ZenithToastPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handlePayload(ZenithToastPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (FMLEnvironment.getDist() == Dist.CLIENT) {
                ZenithToast.show(payload.data());
            }
        });
    }
}
