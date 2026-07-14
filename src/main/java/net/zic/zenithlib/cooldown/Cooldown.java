package net.zic.zenithlib.cooldown;

/**
 * we are using a wrapper class to prevent constant Integer object creation
 */
public class Cooldown {
    private int ticksRemaining;
    public Cooldown(int initialTicks){
        this.ticksRemaining = initialTicks;;
    }

    public boolean tick(){
        return --ticksRemaining <= 0;
    }
    public void setTicksRemaining(int newValue){
        ticksRemaining = newValue;
    }
    public int getTicksRemaining(){return ticksRemaining;}

}
