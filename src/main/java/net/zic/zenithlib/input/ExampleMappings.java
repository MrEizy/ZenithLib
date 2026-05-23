package net.zic.zenithlib.input;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;


public class ExampleMappings {
    public static void init(){}
    public static final KeyMapping CAST_SKILL_KEY = new KeyMapping(
            "key.examplemod.example1", // Will be localized using this translation key
            InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
            GLFW.GLFW_KEY_P, // Default key is P
            KeyMapping.Category.MISC // Mapping will be in the misc category
    );


    public static final MappingHandler CAST_HANDLER = InputHandler.registerMapping(
                new KeyMapping(
                        "key.examplemod.example1", // Will be localized using this translation key
                        InputConstants.Type.KEYSYM, // Default mapping is on the keyboard
                        GLFW.GLFW_KEY_P, // Default key is P
                        KeyMapping.Category.MISC // Mapping will be in the misc category
                )
            )
            .setOnDown(()->System.out.println("cast down"))
            .setOnRepeat((ticks)->System.out.println("repeat ("+ticks+")"))
            .setOnUp((ticks)->System.out.println("cast released after held for "+ticks +" ticks"));

}
