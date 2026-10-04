package net.zic.zenithlib.custom_attributes;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.zenithlib.Config;
import net.zic.zenithlib.ZenithLib;
import net.zic.zenithlib.value_containers.typed.Modifier;

import java.util.ArrayList;
import java.util.List;

public class SuppressedAttributeHelper {

    public static final Identifier SUPPRESSION_ID = Identifier.fromNamespaceAndPath(ZenithLib.MOD_ID,"suppression");

    public static Identifier getId(Holder<Attribute> attribute){
        return attribute.getKey() == null ?null : attribute.getKey().identifier();
    }
    public static Holder<Attribute> getAttribute(Identifier identifier){
        return BuiltInRegistries.ATTRIBUTE.containsKey(identifier) ?
                BuiltInRegistries.ATTRIBUTE.get(identifier).get() : null;
    }
    public static boolean isSuppressible(Holder<Attribute> attribute){
        Identifier id = getId(attribute);
        if(id == null) return false;
        return isSuppressible(id);
    }
    public static boolean isSuppressible(Identifier attribute){
        return getSuppressedAttributes().contains(attribute);
    }
    public static List<Identifier> getSuppressedAttributes(){
        List<Identifier> identifiers = new ArrayList<>();
        Config.SUPPRESSABLE_ATTRIBUTES.get().forEach(val->identifiers.add(Identifier.parse(val)));
        return identifiers;
    }

    public static int getOperationGroup(){
        return Config.SUPPRESSION_OPERATION_GROUP.get();
    }
    public static double getSuppression(ZenithAttribute attribute){
        if(!attribute.hasModifier(SUPPRESSION_ID)) return 1;
        Modifier<Double> modifier = attribute.getMultiplierModifier(SUPPRESSION_ID);
        return modifier == null ? 1 : 1+modifier.value();
    }

    /**
     *
     * @param suppression a value between 0 and 1, that represents how much of the original value should remain(e.g 0.7 means 30% suppression, or down to 70% of the origional)
     * @return
     */
    public static Modifier<Double> createSuppression(double suppression){
        return Modifier.multiplier(SUPPRESSION_ID,SUPPRESSION_ID,getOperationGroup(),Math.clamp(suppression,0,1)-1);
    }

    public static void applySuppression(ZenithAttribute attribute, double suppression){
        attribute.startProcess("applying_suppression");
        attribute.removeModifier(SUPPRESSION_ID);
        attribute.addMultiplierModifier(createSuppression(suppression));
        attribute.resolveProcess("applying_suppression");
    }

}

