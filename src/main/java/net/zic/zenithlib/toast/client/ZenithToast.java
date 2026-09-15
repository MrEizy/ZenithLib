package net.zic.zenithlib.toast.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.sounds.SoundEvent;
import net.zic.zenithlib.toast.ZenithToastData;
import org.jspecify.annotations.Nullable;

/** Client renderer for ZenithLib toast data */
public final class ZenithToast implements Toast {
    public static final int WIDTH = 180;
    public static final int HEIGHT = 32;

    private static final int ICON_X = 8;
    private static final int ICON_Y = 8;
    private static final int TEXT_X_WITH_ICON = 30;
    private static final int TEXT_X_WITHOUT_ICON = 8;
    private static final int TITLE_Y = 6;
    private static final int MESSAGE_Y = 18;

    private final ZenithToastData data;
    private Visibility visibility = Visibility.SHOW;

    private ZenithToast(ZenithToastData data) {
        this.data = data;
    }

    public static void show(ZenithToastData data) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> minecraft.getToastManager().addToast(new ZenithToast(data)));
    }

    @Override
    public Visibility getWantedVisibility() {
        return visibility;
    }

    @Override
    public void update(ToastManager manager, long fullyVisibleForMs) {
        double displayTime = data.durationMs() * manager.getNotificationDisplayTimeMultiplier();
        visibility = fullyVisibleForMs >= displayTime ? Visibility.HIDE : Visibility.SHOW;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                data.background(),
                0,
                0,
                width(),
                height()
        );

        int textX = TEXT_X_WITHOUT_ICON;
        if (!data.icon().isEmpty()) {
            graphics.item(data.icon(), ICON_X, ICON_Y);
            textX = TEXT_X_WITH_ICON;
        }

        graphics.text(font, data.title(), textX, TITLE_Y, data.titleColor(), false);
        graphics.text(font, data.message(), textX, MESSAGE_Y, data.messageColor(), false);
    }

    @Override
    public @Nullable SoundEvent getSoundEvent() {
        return data.sound().orElse(null);
    }

    @Override
    public int width() {
        return WIDTH;
    }

    @Override
    public int height() {
        return HEIGHT;
    }
}
