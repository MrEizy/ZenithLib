package net.zic.zenithlib.tooltip.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.zic.zenithlib.tooltip.api.ZenithTooltipTheme;
import net.zic.zenithlib.tooltip.api.element.EntityPreviewElement;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Draws the entity represented by a spawn egg as a client-only tooltip preview.
 */
final class ZenithTooltipEntityPreviewRenderer {
    private static final int CHAMBER_INSET = 3;
    private static final int MIN_SCALE = 4;
    private static final int MAX_SCALE = 40;

    private static final int MAX_ADAPTIVE_WIDTH = 92;
    private static final float WIDTH_GROWTH_START = 0.85F;
    private static final float WIDTH_GROWTH_PER_BLOCK = 12.0F;
    private static final float MAX_BULK_WIDTH_GROWTH = 30.0F;
    private static final float LONG_BODY_ASPECT_START = 1.15F;
    private static final float LONG_BODY_GROWTH_PER_ASPECT = 10.0F;
    private static final float MAX_LONG_BODY_WIDTH_GROWTH = 12.0F;

    private static final float STANDARD_WIDTH_BUFFER = 1.18F;
    private static final float WIDE_WIDTH_BUFFER = 1.36F;
    private static final float EXTRA_WIDE_WIDTH_BUFFER = 1.54F;
    private static final float HEIGHT_BUFFER = 1.12F;
    private static final float WIDE_ASPECT_THRESHOLD = 1.35F;
    private static final float EXTRA_WIDE_ASPECT_THRESHOLD = 2.15F;
    private static final float BROAD_ENTITY_THRESHOLD = 1.20F;
    private static final float GIANT_ENTITY_THRESHOLD = 2.50F;
    private static final float STANDARD_OCCUPANCY = 0.90F;
    private static final float BROAD_OCCUPANCY = 0.84F;
    private static final float GIANT_OCCUPANCY = 0.80F;
    private static final float SILHOUETTE_BUFFER_START = 1.15F;
    private static final float MAX_SILHOUETTE_BUFFER = 1.28F;

    private static final float FRONT_FACING_CORRECTION_DEGREES = 180.0F;
    private static final float STATIC_PRESENTATION_YAW_DEGREES = -16.0F;
    private static final float ROTATION_ARC_DEGREES = 26.0F;
    private static final long ROTATION_PERIOD_MILLIS = 28000L;

    private static ItemStack cachedStack = ItemStack.EMPTY;
    private static @Nullable Level cachedLevel;
    private static @Nullable LivingEntity cachedEntity;

    private ZenithTooltipEntityPreviewRenderer() {}


    static ZenithTooltipLayout.PreparedEntityPreview prepare(
            ItemStack stack,
            EntityPreviewElement preview,
            int maximumWidth
    ) {
        int width = Math.min(preview.width(), maximumWidth);

        if (preview.adaptiveWidth()) {
            LivingEntity entity = entityFor(stack);
            if (entity != null) {
                width = adaptiveWidth(entity, width, maximumWidth);
            }
        }

        return new ZenithTooltipLayout.PreparedEntityPreview(
                width,
                preview.height(),
                preview.rotate(),
                preview.placement()
        );
    }

    static void render(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            ItemStack stack,
            ZenithTooltipTheme theme,
            ZenithTooltipLayout.PreparedEntityPreview preview
    ) {
        renderChamber(graphics, x, y, preview.width(), preview.height(), theme);

        LivingEntity entity = entityFor(stack);
        if (entity == null) {
            return;
        }

        int contentWidth = Math.max(1, preview.width() - CHAMBER_INSET * 2);
        int contentHeight = Math.max(1, preview.height() - CHAMBER_INSET * 2);
        int scale = fitScale(entity, contentWidth, contentHeight);
        float yawOffset = preview.rotate() ? turntableYawOffset() : STATIC_PRESENTATION_YAW_DEGREES;

        graphics.enableScissor(
                x + CHAMBER_INSET,
                y + CHAMBER_INSET,
                x + preview.width() - CHAMBER_INSET,
                y + preview.height() - CHAMBER_INSET
        );

        try {
            EntityRenderState renderState = neutralRenderState(entity);
            Quaternionf rotation = new Quaternionf()
                    .rotationZ((float) Math.PI)
                    .rotateY((float) Math.toRadians(FRONT_FACING_CORRECTION_DEGREES + yawOffset));
            Vector3f translation = new Vector3f(0.0F, entity.getBbHeight() / 2.0F, 0.0F);

            graphics.entity(
                    renderState,
                    scale,
                    translation,
                    rotation,
                    null,
                    x + CHAMBER_INSET,
                    y + CHAMBER_INSET,
                    x + preview.width() - CHAMBER_INSET,
                    y + preview.height() - CHAMBER_INSET
            );
        } catch (RuntimeException ignored) {
        } finally {
            graphics.disableScissor();
        }
    }

    private static EntityRenderState neutralRenderState(LivingEntity entity) {
        EntityRenderState renderState = Minecraft.getInstance()
                .getEntityRenderDispatcher()
                .extractEntity(entity, 1.0F);

        if (renderState instanceof LivingEntityRenderState livingState) {
            livingState.bodyRot = 0.0F;
            livingState.yRot = 0.0F;
            livingState.xRot = 0.0F;
        }

        return renderState;
    }

    private static int adaptiveWidth(LivingEntity entity, int minimumWidth, int maximumWidth) {
        float entityWidth = Math.max(0.25F, entity.getBbWidth());
        float entityHeight = Math.max(0.25F, entity.getBbHeight());
        float aspect = entityWidth / entityHeight;
        float bulkGrowth = Math.min(
                MAX_BULK_WIDTH_GROWTH,
                Math.max(0.0F, entityWidth - WIDTH_GROWTH_START) * WIDTH_GROWTH_PER_BLOCK
        );
        float shapeGrowth = Math.min(
                MAX_LONG_BODY_WIDTH_GROWTH,
                Math.max(0.0F, aspect - LONG_BODY_ASPECT_START) * LONG_BODY_GROWTH_PER_ASPECT
        );
        int adaptiveWidth = minimumWidth + (int) Math.ceil(bulkGrowth + shapeGrowth);
        int widthLimit = Math.min(maximumWidth, MAX_ADAPTIVE_WIDTH);
        return Math.min(widthLimit, adaptiveWidth);
    }

    private static void renderChamber(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            ZenithTooltipTheme theme
    ) {
        graphics.fill(x, y, x + width, y + height, theme.background());
        graphics.outline(x, y, width, height, theme.borderTop());
        graphics.outline(x + 1, y + 1, width - 2, height - 2, theme.borderBottom());
        graphics.fill(x + 4, y + height - 6, x + width - 4, y + height - 5, theme.accent());
    }

    private static float turntableYawOffset() {
        double phase = (Util.getMillis() % ROTATION_PERIOD_MILLIS)
                / (double) ROTATION_PERIOD_MILLIS
                * Math.PI
                * 2.0D;
        return (float) Math.sin(phase) * ROTATION_ARC_DEGREES;
    }

    private static int fitScale(LivingEntity entity, int width, int height) {
        float entityWidth = Math.max(0.25F, entity.getBbWidth());
        float entityHeight = Math.max(0.25F, entity.getBbHeight());
        float aspect = entityWidth / entityHeight;
        float widthBuffer = aspect >= EXTRA_WIDE_ASPECT_THRESHOLD
                ? EXTRA_WIDE_WIDTH_BUFFER
                : aspect >= WIDE_ASPECT_THRESHOLD
                ? WIDE_WIDTH_BUFFER
                : STANDARD_WIDTH_BUFFER;
        float occupancy = entityWidth >= GIANT_ENTITY_THRESHOLD
                ? GIANT_OCCUPANCY
                : entityWidth >= BROAD_ENTITY_THRESHOLD
                ? BROAD_OCCUPANCY
                : STANDARD_OCCUPANCY;
        float silhouetteBuffer = Math.min(
                MAX_SILHOUETTE_BUFFER,
                1.0F + Math.max(0.0F, entityWidth - SILHOUETTE_BUFFER_START) * 0.11F
        );
        float maximum = Math.min(
                width * occupancy / (entityWidth * widthBuffer * silhouetteBuffer),
                height * occupancy / (entityHeight * HEIGHT_BUFFER * silhouetteBuffer)
        );
        return Math.max(MIN_SCALE, Math.min(MAX_SCALE, (int) maximum));
    }

    private static @Nullable LivingEntity entityFor(ItemStack stack) {
        Level level = Minecraft.getInstance().level;
        EntityType<?> type = SpawnEggItem.getType(stack);

        if (level == null || type == null) {
            clear();
            return null;
        }

        if (cachedEntity != null
                && cachedLevel == level
                && ItemStack.isSameItemSameComponents(cachedStack, stack)) {
            return cachedEntity;
        }

        cachedLevel = level;
        cachedStack = stack.copyWithCount(1);
        cachedEntity = createPreviewEntity(type, level);
        return cachedEntity;
    }

    private static @Nullable LivingEntity createPreviewEntity(EntityType<?> type, Level level) {
        try {
            Entity entity = type.create(level, EntitySpawnReason.SPAWN_ITEM_USE);
            if (entity instanceof LivingEntity living) {
                living.setYRot(0.0F);
                living.setYBodyRot(0.0F);
                living.setYHeadRot(0.0F);
                living.yBodyRotO = 0.0F;
                living.yHeadRotO = 0.0F;
                living.setXRot(0.0F);
                return living;
            }
        } catch (RuntimeException ignored) {
        }

        return null;
    }

    private static void clear() {
        cachedStack = ItemStack.EMPTY;
        cachedLevel = null;
        cachedEntity = null;
    }
}
