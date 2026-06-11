package net.zic.zenithlib.tooltip.client;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.zic.zenithlib.tooltip.api.animation.ScrambleRevealTextEffect;
import net.zic.zenithlib.tooltip.api.animation.ZenithTooltipTextEffect;

import java.util.ArrayList;
import java.util.List;

/** Applies render-only text effects to already wrapped tooltip lines. */
final class ZenithTooltipTextAnimator {
    private ZenithTooltipTextAnimator() {}

    static List<FormattedCharSequence> apply(
            Font font,
            List<FormattedCharSequence> lines,
            ZenithTooltipTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            long elementSeed
    ) {
        if (effect instanceof ScrambleRevealTextEffect scramble) {
            return scramble(font, lines, scramble, frame, elementSeed);
        }

        return lines;
    }

    private static List<FormattedCharSequence> scramble(
            Font font,
            List<FormattedCharSequence> lines,
            ScrambleRevealTextEffect effect,
            ZenithTooltipAnimationState.Frame frame,
            long elementSeed
    ) {
        List<String> sourceLines = new ArrayList<>(lines.size());
        int mutableCharacterCount = 0;

        for (FormattedCharSequence line : lines) {
            String text = plainText(line);
            sourceLines.add(text);
            mutableCharacterCount += mutableCharacterCount(text);
        }

        if (mutableCharacterCount == 0 || effect.reveal() >= 1.0F) {
            return lines;
        }

        int prefixRevealCount = Math.round(mutableCharacterCount * effect.reveal());
        int mutableIndex = 0;
        long animationStep = frame.step(effect.speed());
        long seed = mix64(frame.seed() ^ elementSeed);
        int[] replacementGlyphs = effect.glyphs().codePoints().toArray();
        List<FormattedCharSequence> animated = new ArrayList<>(sourceLines.size());

        for (String sourceLine : sourceLines) {
            int[] codePoints = sourceLine.codePoints().toArray();
            StringBuilder rendered = new StringBuilder(sourceLine.length());

            for (int codePoint : codePoints) {
                if (!isMutable(codePoint)) {
                    rendered.appendCodePoint(codePoint);
                    continue;
                }

                boolean revealed = switch (effect.mode()) {
                    case PREFIX -> mutableIndex < prefixRevealCount;
                    case SCATTERED -> unitHash(seed ^ mutableIndex * 0x9E3779B97F4A7C15L) < effect.reveal();
                };

                if (revealed) {
                    rendered.appendCodePoint(codePoint);
                } else {
                    long glyphHash = mix64(
                            seed
                                    ^ mutableIndex * 0xD1B54A32D192ED03L
                                    ^ animationStep * 0x94D049BB133111EBL
                    );
                    rendered.appendCodePoint(replacementGlyph(
                            font,
                            codePoint,
                            replacementGlyphs,
                            glyphHash
                    ));
                }

                mutableIndex++;
            }

            animated.add(Component.literal(rendered.toString()).getVisualOrderText());
        }

        return List.copyOf(animated);
    }


    private static int replacementGlyph(
            Font font,
            int originalCodePoint,
            int[] glyphs,
            long hash
    ) {
        int originalWidth = font.width(new String(Character.toChars(originalCodePoint)));
        int startIndex = Math.floorMod(hash, glyphs.length);
        int bestGlyph = glyphs[startIndex];
        int bestDifference = Integer.MAX_VALUE;

        for (int offset = 0; offset < glyphs.length; offset++) {
            int glyph = glyphs[(startIndex + offset) % glyphs.length];
            int glyphWidth = font.width(new String(Character.toChars(glyph)));
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

    private static String plainText(FormattedCharSequence sequence) {
        StringBuilder text = new StringBuilder();
        sequence.accept((index, style, codePoint) -> {
            text.appendCodePoint(codePoint);
            return true;
        });
        return text.toString();
    }

    private static int mutableCharacterCount(String text) {
        return (int) text.codePoints().filter(ZenithTooltipTextAnimator::isMutable).count();
    }

    private static boolean isMutable(int codePoint) {
        return Character.isLetterOrDigit(codePoint);
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
}
