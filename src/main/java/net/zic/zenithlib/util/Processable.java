package net.zic.zenithlib.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Processable {
    public static final Runnable EMPTY_RUNNABLE = ()->{};
    private String process;
    private Runnable onResolved = EMPTY_RUNNABLE;

    public Processable(){

    }
    public Processable(Runnable onResolved){
        setOnResolved(onResolved);
    }
    public void setOnResolved(Runnable onResolved){
        this.onResolved = onResolved;
    }
    public void startProcess(String process){
        if(this.process == null && process != null) this.process = process;
    }
    public boolean resolveProcess(String process){
        if(this.process == null || process == null) return false;
        process = null;
        onResolved.run();
        return true;
    }

    public boolean startAndResolveProcess(){
        String id = UUID.randomUUID().toString();
        startProcess(id);
        return resolveProcess(id);
    }

}
