package net.zic.zenithlib.tooltip.client;

import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Shared timing state for the tooltip currently being hovered.
 */
public final class ZenithTooltipAnimationState {
    private static final long SESSION_TIMEOUT_MS = 500L;

    private static @Nullable Identifier currentItemId;
    private static ItemStack currentStack = ItemStack.EMPTY;
    private static long sessionStartedAtMs;
    private static long lastPreparedAtMs;
    private static long sessionSeed;
    private static long sessionCounter;

    private ZenithTooltipAnimationState() {}

    public static Update update(Identifier itemId, ItemStack stack) {
        long now = Util.getMillis();
        boolean expired = lastPreparedAtMs == 0L || now - lastPreparedAtMs > SESSION_TIMEOUT_MS;
        boolean identityChanged = currentItemId == null
                || !currentItemId.equals(itemId)
                || !ItemStack.isSameItemSameComponents(currentStack, stack);
        boolean changed = expired || identityChanged;

        if (changed) {
            currentItemId = itemId;
            currentStack = stack.copyWithCount(1);
            sessionStartedAtMs = now;
            sessionSeed = mix64(
                    now
                            ^ (++sessionCounter * 0x9E3779B97F4A7C15L)
                            ^ itemId.hashCode()
                            ^ currentStack.getComponents().hashCode()
            );
        }

        lastPreparedAtMs = now;
        return new Update(changed, new Frame(Math.max(0L, now - sessionStartedAtMs), sessionSeed));
    }

    public static void reset() {
        currentItemId = null;
        currentStack = ItemStack.EMPTY;
        sessionStartedAtMs = 0L;
        lastPreparedAtMs = 0L;
        sessionSeed = 0L;
    }

    private static long mix64(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    public record Update(boolean changed, Frame frame) {}

    public record Frame(long elapsedMillis, long seed) {
        public long step(int intervalMillis) {
            return elapsedMillis / Math.max(1, intervalMillis);
        }
    }
}
