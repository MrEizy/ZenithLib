package net.zic.zenithlib.cooldown;

import net.minecraft.resources.Identifier;

//holds an instance of a cooldown
public class Cooldown {
    private final Identifier identifier;
    private final CooldownListener listener;
    private int ticksRemaining;

    public Cooldown(Identifier identifier, CooldownListener listener,int initialTicks) {
        this.identifier = identifier;
        this.listener = listener;
        ticksRemaining = initialTicks;
    }
    public Identifier getIdentifier(){return identifier;}
    public CooldownListener getListener(){return listener;}
    public int getTicksRemaining() {return ticksRemaining;}

    /**
     * @return true if it has finished cooldown, false otherwise
     */
    public boolean tick(){
        this.ticksRemaining--;
        return ticksRemaining <= 0;
    }
    public void setTicksRemaining(int value){ticksRemaining = value;}
}
