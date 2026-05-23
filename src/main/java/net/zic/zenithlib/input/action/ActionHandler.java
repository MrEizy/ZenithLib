package net.zic.zenithlib.input.action;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zic.zenithlib.input.MappingHandler;

/**
 * an action handler is a type of Mapping handler that, instead of just reacting on the client
 * syncs the mapping to the server.
 * <p>
 * BUT, we do not sync the key pressed, but the action.
 * <p>
 * on the server we then send an event for actionStart and actionEnd
 */
public class ActionHandler extends MappingHandler {
    private final Identifier identifier;
    public ActionHandler(Identifier identifier,KeyMapping mapping) {
        super(mapping);
        this.identifier = identifier;
    }
    public Identifier getIdentifier(){return identifier;}

    @Override
    public void onDown(int ticks) {
        super.onDown(ticks);
        ClientPacketDistributor.sendToServer(new ActionChangedPacket(identifier,true));
    }

    @Override
    public void onUp(int ticks) {
        super.onUp(ticks);
        ClientPacketDistributor.sendToServer(new ActionChangedPacket(identifier,false));
    }
}
