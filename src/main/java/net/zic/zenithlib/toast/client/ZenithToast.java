package net.zic.zenithlib.toast.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
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
    private static final int TEXT_RIGHT_PADDING = 7;

    private static final int TITLE_Y = 5;
    private static final int MESSAGE_Y = 18;

    private static final float MIN_TEXT_SCALE = 0.75F;

    private static final long SCROLL_DELAY_MS = 800L;
    private static final float SCROLL_SPEED = 18.0F;
    private static final int SCROLL_GAP = 18;

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

        int textWidth = width() - textX - TEXT_RIGHT_PADDING;

        drawFittedText(graphics, font, data.title(), textX, TITLE_Y, textWidth, data.titleColor(), fullyVisibleForMs);
        drawFittedText(graphics, font, data.message(), textX, MESSAGE_Y, textWidth, data.messageColor(), fullyVisibleForMs);
    }

    private static void drawFittedText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int availableWidth, int color, long fullyVisibleForMs) {
        int rawWidth = font.width(text);

        if (rawWidth <= availableWidth) {
            graphics.text(font, text, x, y, color, false);
            return;
        }

        float scale = Math.max(MIN_TEXT_SCALE, (float) availableWidth / rawWidth);

        if (rawWidth * scale <= availableWidth) {
            graphics.pose().pushMatrix();
            graphics.pose().translate(x, y);
            graphics.pose().scale(scale, scale);
            graphics.text(font, text, 0, 0, color, false);

            graphics.pose().popMatrix();
            return;
        }

        drawScrollingText(graphics, font, text, x, y, availableWidth, color, fullyVisibleForMs);
    }

    private static void drawScrollingText(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int availableWidth, int color, long fullyVisibleForMs) {
        float scale = MIN_TEXT_SCALE;
        float scaledWidth = font.width(text) * scale;
        float viewportWidth = availableWidth / scale;

        long scrollingTime = Math.max(0L, fullyVisibleForMs - SCROLL_DELAY_MS);

        float distance = scaledWidth + SCROLL_GAP;
        float offset = (scrollingTime / 1000.0F * SCROLL_SPEED) % distance;

        graphics.enableScissor(x, y, x + availableWidth, y + Math.max(1, (int) Math.ceil(font.lineHeight * scale)));
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);

        graphics.text(font, text, Math.round(-offset), 0, color, false);

        if (offset > scaledWidth - viewportWidth) {
            graphics.text(font, text, Math.round(distance - offset), 0, color, false);
        }

        graphics.pose().popMatrix();
        graphics.disableScissor();
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