package net.zic.zenithlib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.SuppressedAttributeHelper;
import net.zic.zenithlib.custom_attributes.ZenithAttribute;
import net.zic.zenithlib.stats.Stat;

import java.util.List;

public class ZenithStatCommands {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("stat")
                        .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                        .then(Commands.literal("attribute_bonus")
                                .then(Commands.argument("attribute",IdentifierArgument.id())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggestResource(
                                                        BuiltInRegistries.ATTRIBUTE.keySet(),
                                                        builder))
                                        .executes(ZenithStatCommands::getScalingBonus)
                                )
                        )
        );
    }

    private static int getScalingBonus(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer player = source.getSource().getPlayerOrException();
        Identifier attribute = IdentifierArgument.getId(source,"attribute");

        Holder<Attribute> attributeHolder = SuppressedAttributeHelper.getAttribute(attribute);
        if(attributeHolder == null){
            fail(source.getSource(),"attribute does not exist");
            return 0;
        }

        ZenithAttribute zenithAttribute = player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).getAttribute(attributeHolder);
        if(zenithAttribute == null){
            fail(source.getSource(),"you do not have that attribute");
            return 0;
        }


        source.getSource().sendSuccess(()->
                Component.literal("===="+attribute+"====\n")
                        .append(Component.literal("total : "+zenithAttribute.getStatBonus()+"\n"))
                        .append(displayBonuses(zenithAttribute.getStatBonuses())),

                false);


        return 1;
    }

    private static Component displayBonuses(List<Pair<Stat,Double>> stats){
        MutableComponent component = Component.empty();
        for(Pair<Stat,Double> stat : stats) component.append(stat.getFirst().getName() +" : "+String.format("%.2f%%", stat.getSecond())+"\n");
        return component;
    }
    private static void fail(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message).withStyle(ChatFormatting.RED));
    }

}
