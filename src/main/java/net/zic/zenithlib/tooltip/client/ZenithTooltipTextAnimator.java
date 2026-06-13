package net.zic.zenithlib.tooltip.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.FormattedCharSequence;
import net.zic.zenithlib.Config;
import net.zic.zenithlib.tooltip.api.animation.RainbowTextEffect;
import net.zic.zenithlib.tooltip.api.animation.RuneDecipherTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ScrambleRevealTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ShimmerTextEffect;
import net.zic.zenithlib.tooltip.api.animation.TextEffectStack;
import net.zic.zenithlib.tooltip.api.animation.TypewriterTextEffect;
import net.zic.zenithlib.tooltip.api.animation.WaveTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;

import java.util.ArrayList;
import java.util.List;

/** Applies render-only text effects to already wrapped tooltip lines. */
final class ZenithTooltipTextAnimator {
    private static final double TWO_PI = Math.PI * 2.0D;

    private ZenithTooltipTextAnimator() {}

    static int verticalPadding(ZenithTooltipTextEffect effect) {
        if (effect instanceof WaveTextEffect wave && animationsEnabled()) {
            return wave.amplitude();
        }
        if (effect instanceof TextEffectStack stack) {
            int padding = 0;
            for (ZenithTooltipTextEffect child : stack.effects()) {
                padding += verticalPadding(child);
            }
            return padding;
        }
        return 0;
    }

    static void render(
            Font font,
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            List<FormattedCharSequence> lines,
            int baseColor,
            int lineGap,
            ZenithTooltipTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            long elementSeed
    ) {
        if (!animationsEnabled()) {
            renderPlain(font, graphics, x, y, lines, baseColor, lineGap);
            return;
        }

        List<ZenithTooltipTextEffect> effects = flatten(effect);
        int glyphCount = glyphCount(lines);
        int mutableCharacterCount = mutableCharacterCount(lines);
        int glyphIndex = 0;
        int mutableIndex = 0;

        for (FormattedCharSequence line : lines) {
            List<Glyph> glyphs = glyphs(font, line);
            int cursorX = x;

            for (Glyph glyph : glyphs) {
                int renderedCodePoint = glyph.codePoint();
                Style renderedStyle = glyph.style();
                int yOffset = 0;
                boolean visible = true;
                boolean drawCursorAfterGlyph = false;

                for (ZenithTooltipTextEffect current : effects) {
                    if (current instanceof TypewriterTextEffect typewriter) {
                        TypewriterResult result = typewriterVisibility(typewriter, frame, glyphIndex, glyphCount, glyph.codePoint());
                        visible &= result.visible();
                        drawCursorAfterGlyph |= result.cursorAfter();
                    } else if (current instanceof RuneDecipherTextEffect rune) {
                        renderedCodePoint = runeCodePoint(
                                font,
                                glyph,
                                renderedCodePoint,
                                rune,
                                frame,
                                elementSeed,
                                mutableIndex,
                                mutableCharacterCount
                        );
                    } else if (current instanceof ScrambleRevealTextEffect scramble) {
                        renderedCodePoint = scrambleCodePoint(
                                font,
                                glyph,
                                renderedCodePoint,
                                scramble,
                                frame,
                                elementSeed,
                                mutableIndex,
                                mutableCharacterCount
                        );
                    } else if (current instanceof RainbowTextEffect rainbow) {
                        int rgb = rainbowColor(rainbow, frame, elementSeed, glyphIndex);
                        renderedStyle = renderedStyle.withColor(TextColor.fromRgb(rgb));
                    } else if (current instanceof ShimmerTextEffect shimmer) {
                        int rgb = shimmerColor(shimmer, frame, glyphIndex, glyphCount, effectiveRgb(renderedStyle, baseColor));
                        renderedStyle = renderedStyle.withColor(TextColor.fromRgb(rgb));
                    } else if (current instanceof WaveTextEffect wave && !reduceMotion()) {
                        yOffset += waveOffset(wave, frame, glyphIndex);
                    }
                }

                if (visible && !Character.isWhitespace(renderedCodePoint)) {
                    graphics.text(
                            font,
                            singleGlyph(renderedStyle, renderedCodePoint),
                            cursorX,
                            y + yOffset,
                            baseColor,
                            false
                    );
                }

                if (drawCursorAfterGlyph) {
                    int cursorColor = effectiveRgb(renderedStyle, baseColor) | 0xFF000000;
                    graphics.fill(cursorX + glyph.width(), y + 1, cursorX + glyph.width() + 1, y + font.lineHeight - 1, cursorColor);
                }

                cursorX += glyph.width();
                glyphIndex++;
                if (isMutable(glyph.codePoint())) {
                    mutableIndex++;
                }
            }

            y += font.lineHeight + lineGap;
        }
    }

    private static void renderPlain(
            Font font,
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            List<FormattedCharSequence> lines,
            int color,
            int gap
    ) {
        for (FormattedCharSequence line : lines) {
            graphics.text(font, line, x, y, color, false);
            y += font.lineHeight + gap;
        }
    }

    private static TypewriterResult typewriterVisibility(
            TypewriterTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            int glyphIndex,
            int glyphCount,
            int codePoint
    ) {
        if (glyphCount == 0) {
            return new TypewriterResult(true, false);
        }

        long elapsed = Math.max(0L, frame.pageElapsedMillis() - effect.delay());
        float progress = Math.min(1.0F, elapsed / (float) effect.duration());
        int visibleGlyphs = Math.round(glyphCount * progress);
        boolean whitespace = Character.isWhitespace(codePoint);
        boolean visible = glyphIndex < visibleGlyphs || (whitespace && !effect.revealWhitespace());
        boolean cursorAfter = effect.cursor()
                && progress < 1.0F
                && glyphIndex == Math.max(0, visibleGlyphs - 1);
        return new TypewriterResult(visible, cursorAfter);
    }

    private static int runeCodePoint(
            Font font,
            Glyph glyph,
            int currentCodePoint,
            RuneDecipherTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            long elementSeed,
            int mutableIndex,
            int mutableCharacterCount
    ) {
        if (!isMutable(glyph.codePoint()) || mutableCharacterCount == 0) {
            return currentCodePoint;
        }

        long elapsed = Math.max(0L, frame.pageElapsedMillis() - effect.delay());
        float reveal = Math.min(1.0F, elapsed / (float) effect.duration());
        ScrambleRevealTextEffect proxy = new ScrambleRevealTextEffect(
                reveal,
                effect.speed(),
                effect.mode(),
                effect.glyphs()
        );
        return scrambleCodePoint(font, glyph, currentCodePoint, proxy, frame, elementSeed, mutableIndex, mutableCharacterCount);
    }

    private static int scrambleCodePoint(
            Font font,
            Glyph glyph,
            int currentCodePoint,
            ScrambleRevealTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            long elementSeed,
            int mutableIndex,
            int mutableCharacterCount
    ) {
        if (!isMutable(glyph.codePoint()) || mutableCharacterCount == 0 || effect.reveal() >= 1.0F) {
            return currentCodePoint;
        }

        int prefixRevealCount = Math.round(mutableCharacterCount * effect.reveal());
        long seed = mix64(frame.seed() ^ elementSeed);
        boolean revealed = switch (effect.mode()) {
            case PREFIX -> mutableIndex < prefixRevealCount;
            case SCATTERED -> unitHash(seed ^ mutableIndex * 0x9E3779B97F4A7C15L) < effect.reveal();
        };

        if (revealed) {
            return currentCodePoint;
        }

        long animationStep = frame.step(effect.speed());
        long glyphHash = mix64(
                seed
                        ^ mutableIndex * 0xD1B54A32D192ED03L
                        ^ animationStep * 0x94D049BB133111EBL
        );
        return replacementGlyph(font, glyph, effect.glyphs().codePoints().toArray(), glyphHash);
    }

    private static int rainbowColor(
            RainbowTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            long elementSeed,
            int glyphIndex
    ) {
        float direction = effect.reverse() ? -1.0F : 1.0F;
        float seededPhase = (float) unitHash(elementSeed * 0x9E3779B97F4A7C15L);
        float travellingPhase = direction * (float) frame.elapsedMillis() / (float) effect.period();
        float rawHue = seededPhase + travellingPhase + glyphIndex * effect.spread();
        float hue = switch (effect.mode()) {
            case SPECTRUM -> wrap01(rawHue);
            case PING_PONG -> pingPong(rawHue, effect.minHue(), effect.maxHue());
        };
        return hsvToRgb(hue, effect.saturation(), effect.brightness());
    }

    private static int shimmerColor(
            ShimmerTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            int glyphIndex,
            int glyphCount,
            int baseRgb
    ) {
        if (glyphCount <= 0) {
            return baseRgb;
        }

        float direction = effect.reverse() ? -1.0F : 1.0F;
        float phase = wrap01(direction * frame.elapsedMillis() / (float) effect.period());
        float position = glyphIndex / (float) Math.max(1, glyphCount - 1);
        float distance = Math.abs(position - phase);
        distance = Math.min(distance, 1.0F - distance);
        float shine = Math.max(0.0F, 1.0F - distance / effect.width()) * effect.brightness();
        return blendTowardsWhite(baseRgb, shine);
    }

    private static int waveOffset(
            WaveTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            int glyphIndex
    ) {
        if (effect.amplitude() == 0) {
            return 0;
        }

        double direction = effect.reverse() ? -1.0D : 1.0D;
        double timePhase = direction * frame.elapsedMillis() * TWO_PI / effect.period();
        double glyphPhase = glyphIndex * TWO_PI / effect.wavelength();
        double sine = Math.sin(timePhase - glyphPhase);

        return switch (effect.mode()) {
            case SINE -> (int) Math.round(sine * effect.amplitude());
            case BOUNCE -> -(int) Math.round(Math.max(0.0D, sine) * effect.amplitude());
        };
    }

    private static List<ZenithTooltipTextEffect> flatten(ZenithTooltipTextEffect effect) {
        List<ZenithTooltipTextEffect> flattened = new ArrayList<>();
        flattenInto(effect, flattened);
        return List.copyOf(flattened);
    }

    private static void flattenInto(ZenithTooltipTextEffect effect, List<ZenithTooltipTextEffect> destination) {
        if (effect instanceof TextEffectStack stack) {
            for (ZenithTooltipTextEffect child : stack.effects()) {
                flattenInto(child, destination);
            }
            return;
        }
        destination.add(effect);
    }

    private static List<Glyph> glyphs(Font font, FormattedCharSequence line) {
        List<Glyph> glyphs = new ArrayList<>();
        line.accept((index, style, codePoint) -> {
            glyphs.add(new Glyph(style, codePoint, font.width(singleGlyph(style, codePoint))));
            return true;
        });
        return glyphs;
    }

    private static int glyphCount(List<FormattedCharSequence> lines) {
        int[] count = {0};
        for (FormattedCharSequence line : lines) {
            line.accept((index, style, codePoint) -> {
                count[0]++;
                return true;
            });
        }
        return count[0];
    }

    private static int mutableCharacterCount(List<FormattedCharSequence> lines) {
        int[] count = {0};
        for (FormattedCharSequence line : lines) {
            line.accept((index, style, codePoint) -> {
                if (isMutable(codePoint)) {
                    count[0]++;
                }
                return true;
            });
        }
        return count[0];
    }

    private static int replacementGlyph(Font font, Glyph original, int[] glyphs, long hash) {
        if (glyphs.length == 0) {
            return original.codePoint();
        }

        int originalWidth = original.width() > 0
                ? original.width()
                : font.width(singleGlyph(original.style(), original.codePoint()));
        int startIndex = Math.floorMod(hash, glyphs.length);
        int bestGlyph = glyphs[startIndex];
        int bestDifference = Integer.MAX_VALUE;

        for (int offset = 0; offset < glyphs.length; offset++) {
            int glyph = glyphs[(startIndex + offset) % glyphs.length];
            int glyphWidth = font.width(singleGlyph(original.style(), glyph));
            int difference = Math.abs(glyphWidth - originalWidth);

            if (difference < bestDifference) {
                bestGlyph = glyph;
                bestDifference = difference;
                if (difference == 0) {
                    break;
                }
            }
        }

        return bestGlyph;
    }

    private static FormattedCharSequence singleGlyph(Style style, int codePoint) {
        return sink -> sink.accept(0, style, codePoint);
    }

    private static boolean isMutable(int codePoint) {
        return Character.isLetterOrDigit(codePoint);
    }

    private static int effectiveRgb(Style style, int baseColor) {
        TextColor styleColor = style.getColor();
        return styleColor == null ? baseColor & 0xFFFFFF : styleColor.getValue() & 0xFFFFFF;
    }

    private static int blendTowardsWhite(int rgb, float amount) {
        float t = Math.max(0.0F, Math.min(1.0F, amount));
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        r += Math.round((255 - r) * t);
        g += Math.round((255 - g) * t);
        b += Math.round((255 - b) * t);
        return r << 16 | g << 8 | b;
    }

    private static float pingPong(float value, float min, float max) {
        float triangle = Math.abs(2.0F * wrap01(value) - 1.0F);
        return min + triangle * (max - min);
    }

    private static float wrap01(float value) {
        return value - (float) Math.floor(value);
    }

    private static int hsvToRgb(float hue, float saturation, float brightness) {
        float scaledHue = wrap01(hue) * 6.0F;
        int sector = (int) Math.floor(scaledHue);
        float fraction = scaledHue - sector;
        float p = brightness * (1.0F - saturation);
        float q = brightness * (1.0F - fraction * saturation);
        float t = brightness * (1.0F - (1.0F - fraction) * saturation);

        float red;
        float green;
        float blue;
        switch (sector % 6) {
            case 0 -> { red = brightness; green = t; blue = p; }
            case 1 -> { red = q; green = brightness; blue = p; }
            case 2 -> { red = p; green = brightness; blue = t; }
            case 3 -> { red = p; green = q; blue = brightness; }
            case 4 -> { red = t; green = p; blue = brightness; }
            default -> { red = brightness; green = p; blue = q; }
        }

        int r = Math.round(red * 255.0F);
        int g = Math.round(green * 255.0F);
        int b = Math.round(blue * 255.0F);
        return r << 16 | g << 8 | b;
    }

    private static boolean animationsEnabled() {
        return Config.TOOLTIP_ANIMATIONS_ENABLED.get() && Config.TOOLTIP_ANIMATION_INTENSITY.get() > 0;
    }

    private static boolean reduceMotion() {
        return Config.TOOLTIP_REDUCE_MOTION.get();
    }

    private static double unitHash(long value) {
        return (mix64(value) >>> 11) * 0x1.0p-53;
    }

    private static long mix64(long value) {
        value ^= value >>> 30;
        value *= 0xBF58476D1CE4E5B9L;
        value ^= value >>> 27;
        value *= 0x94D049BB133111EBL;
        return value ^ value >>> 31;
    }

    private record Glyph(Style style, int codePoint, int width) {}

    private record TypewriterResult(boolean visible, boolean cursorAfter) {}
}
