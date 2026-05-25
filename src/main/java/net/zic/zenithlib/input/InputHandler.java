package net.zic.zenithlib.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.input.action.ActionHandler;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * the "registry" for KeyMappings. here you register your keyMappings to get a MappingHandler
 *
 * EXAMPLE NORMAL
 *     public static final MappingHandler CAST_HANDLER = InputHandler.registerMapping(
 *                 new KeyMapping(
 *                         "key.examplemod.example1",
 *                         InputConstants.Type.KEYSYM,
 *                         GLFW.GLFW_KEY_P,
 *                         KeyMapping.Category.MISC
 *                 )
 *             )
 *             .setOnDown(()->System.out.println("cast down"))
 *             .setOnRepeat((ticks)->System.out.println("repeat ("+ticks+")"))
 *             .setOnUp((ticks)->System.out.println("cast released after held for "+ticks +" ticks"));
 *
 * EXAMPLE ACTION (SEE ActionHandler)
 *
 *            public static final MappingHandler CAST_HANDLER = InputHandler.registerAction(
 *                    Identifier.fromNamespaceAndLocation(EXAMPLEMOD.MOD_ID,"skill_cast"),
 *                    new KeyMapping(
 *                            "key.examplemod.example1",
 *                            InputConstants.Type.KEYSYM,
 *                            GLFW.GLFW_KEY_P,
 *                            KeyMapping.Category.MISC
 *                    )
 *                )
 *                .setOnDown(()->System.out.println("cast down"))
 *                .setOnRepeat((ticks)->System.out.println("repeat ("+ticks+")"))
 *                .setOnUp((ticks)->System.out.println("cast released after held for "+ticks +" ticks"));
 */
@EventBusSubscriber(modid = ZenithLib.MOD_ID,value = Dist.CLIENT)
public class InputHandler {

    private static final ArrayList<MappingHandler> handlers = new ArrayList<>();
    private static final HashSet<KeyMapping> activeMapping = new HashSet<>();
    private static int ticksElapsed = 0;

    /* GUI-only mappings used by multipage Zenith tooltips. */
    public static final KeyMapping TOOLTIP_PREVIOUS_PAGE = new KeyMapping(
            "key.zenithlib.tooltip.previous_page",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT_BRACKET,
            KeyMapping.Category.MISC
    );

    public static final KeyMapping TOOLTIP_NEXT_PAGE = new KeyMapping(
            "key.zenithlib.tooltip.next_page",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_BRACKET,
            KeyMapping.Category.MISC
    );



    public static MappingHandler registerMapping(KeyMapping mapping){
        MappingHandler handler = new MappingHandler(mapping);
        handlers.add(handler);
        return handler;
    }
    public static MappingHandler registerAction(Identifier identifier, KeyMapping mapping){
        ActionHandler handler = new ActionHandler(identifier,mapping);
        handlers.add(handler);
        return handler;
    }
    @SubscribeEvent // on the mod event bus only on the physical client
    protected static void registerBindings(RegisterKeyMappingsEvent event) {
        for(MappingHandler handler : handlers){
            event.register(handler.getMapping());
        }

        event.register(TOOLTIP_PREVIOUS_PAGE);
        event.register(TOOLTIP_NEXT_PAGE);
    }
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event){

        ticksElapsed++;
        for(MappingHandler handler : handlers){
            if(handler.getMapping().isDown()){
                if(activeMapping.contains(handler.getMapping())) handler.onRepeat(ticksElapsed);
                else{
                    activeMapping.add(handler.getMapping());
                    handler.onDown(ticksElapsed);
                }
            }else if(activeMapping.contains(handler.getMapping())){
                activeMapping.remove(handler.getMapping());
                handler.onUp(ticksElapsed);
            }
        }

    }
}
