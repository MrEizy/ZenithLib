package net.zic.zenithlib.input;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/**
 * and abstraction layer to keyMapping. wrap a key wrapper in this, assign your event methods and register on InputHandler
 *
 * the input handler will then auto listen to the keyMapping for you
 */

public class MappingHandler {

    private final KeyMapping mapping;

    private Runnable onDown;
    private MappingConsumer onRepeat;
    private MappingConsumer onUp;

    private int onDownTicks;

    public MappingHandler(KeyMapping mapping) {
        this.mapping = mapping;
    }

    public KeyMapping getMapping(){return mapping;}

    public MappingHandler setOnDown(Runnable runnable){
        this.onDown = runnable;
        return this;
    }
    public MappingHandler setOnRepeat(MappingConsumer runnable){
        this.onRepeat = runnable;
        return this;
    }
    public MappingHandler setOnUp(MappingConsumer runnable){
        this.onUp = runnable;
        return this;
    }

    public void onDown(int ticks){
        this.onDownTicks = ticks;
        onDown.run();
    }
    public void onRepeat(int ticks){
        int ticksElapsed = ticks-onDownTicks;
        onRepeat.run(ticksElapsed);
    }
    public void onUp(int ticks){
        int ticksElapsed = ticks-onDownTicks;
        onDownTicks = 0;
        onUp.run(ticksElapsed);
    }


}
