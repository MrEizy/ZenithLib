package net.zic.zenithlib.input;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.IntConsumer;

/**
 * and abstraction layer to keyMapping. wrap a key wrapper in this, assign your event methods and register on InputHandler
 *
 * the input handler will then auto listen to the keyMapping for you
 */

public class MappingHandler {
    private static final Runnable EMPTY = ()->{};
    private static final IntConsumer EMPTY_INT_CONSUMER = (val)->{};
    private final KeyMapping mapping;

    private Runnable onDown = EMPTY;
    private IntConsumer onRepeat = EMPTY_INT_CONSUMER;
    private IntConsumer onUp = EMPTY_INT_CONSUMER;

    private int onDownTicks;

    public MappingHandler(KeyMapping mapping) {
        this.mapping = mapping;
    }

    public KeyMapping getMapping(){return mapping;}

    public MappingHandler setOnDown(Runnable runnable){
        this.onDown = runnable;
        return this;
    }
    public MappingHandler setOnRepeat(IntConsumer runnable){
        this.onRepeat = runnable;
        return this;
    }
    public MappingHandler setOnUp(IntConsumer runnable){
        this.onUp = runnable;
        return this;
    }

    public void onDown(int ticks){
        this.onDownTicks = ticks;
        onDown.run();
    }
    public void onRepeat(int ticks){
        int ticksElapsed = ticks-onDownTicks;
        onRepeat.accept(ticksElapsed);
    }
    public void onUp(int ticks){
        int ticksElapsed = ticks-onDownTicks;
        onDownTicks = 0;
        onUp.accept(ticksElapsed);
    }


}
