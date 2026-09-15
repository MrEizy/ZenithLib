package net.zic.zenithlib.toast;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.zenithlib.toast.client.ZenithToast;

import java.util.Objects;

/** Public API for showing ZenithLib toasts */
public final class ZenithToasts {
    private ZenithToasts() {}

    public static void show(ServerPlayer player, ZenithToastData toast) {
        PacketDistributor.sendToPlayer(
                Objects.requireNonNull(player, "player"),
                new ZenithToastPayload(Objects.requireNonNull(toast, "toast"))
        );
    }

    public static void show(ServerPlayer player, Component title, Component message, ItemStack icon) {
        show(player, ZenithToastData.builder()
                .title(title)
                .message(message)
                .icon(icon)
                .build()
        );
    }

    public static void showLocal(ZenithToastData toast) {
        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            ZenithToast.show(Objects.requireNonNull(toast, "toast"));
        }
    }

    public static void showLocal(Component title, Component message, ItemStack icon) {
        showLocal(ZenithToastData.builder()
                .title(title)
                .message(message)
                .icon(icon)
                .build()
        );
    }
}
