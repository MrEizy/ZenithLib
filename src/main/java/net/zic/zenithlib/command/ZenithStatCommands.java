package net.zic.zenithlib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.zic.zenithlib.common.ZenithRegistries;
import net.zic.zenithlib.custom_attributes.SuppressedAttributeHelper;
import net.zic.zenithlib.custom_attributes.ZenithAttribute;
import net.zic.zenithlib.stats.Stat;
import net.zic.zenithlib.stats.ZenithStatHelper;
import net.zic.zenithlib.value_containers.typed.Modifier;

import java.util.List;

public class ZenithStatCommands {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("stat")
                        .then(Commands.literal("get")
                                .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                                .then(Commands.argument("stat",IdentifierArgument.id())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggestResource(
                                                        ZenithRegistries.STAT_REGISTRY.keySet(),
                                                        builder))
                                        .then(Commands.literal("base").executes(ZenithStatCommands::getBaseStat))
                                        .executes(ZenithStatCommands::getStat)
                                )
                        )
                        .then(Commands.literal("set")
                                .requires(Commands.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("stat",IdentifierArgument.id())
                                        .suggests((context, builder) ->
                                                SharedSuggestionProvider.suggestResource(
                                                        ZenithRegistries.STAT_REGISTRY.keySet(),
                                                        builder))
                                        .then(Commands.literal("flat")
                                                .then(Commands.argument("modifier_id",IdentifierArgument.id())
                                                        .then(Commands.argument("value",DoubleArgumentType.doubleArg())
                                                                .then(Commands.argument("op_group", IntegerArgumentType.integer())
                                                                        .executes(ZenithStatCommands::addFlatModifier)
                                                                )
                                                        )
                                                )
                                        )
                                        .then(Commands.literal("multiplier")
                                                .then(Commands.argument("modifier_id",IdentifierArgument.id())
                                                        .then(Commands.argument("group",IdentifierArgument.id())
                                                                .then(Commands.argument("value",DoubleArgumentType.doubleArg())
                                                                        .then(Commands.argument("op_group", IntegerArgumentType.integer())
                                                                                .executes(ZenithStatCommands::addMultiplierModifier)
                                                                        )
                                                                )
                                                        )
                                                )
                                        )

                                )
                        )
                        .then(Commands.literal("attribute_bonus")
                                .requires(Commands.hasPermission(Commands.LEVEL_ALL))
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

    private static int getStat(CommandContext<CommandSourceStack> source)throws CommandSyntaxException{
        ServerPlayer player = source.getSource().getPlayerOrException();

        Identifier statId = IdentifierArgument.getId(source,"stat");
        if(ZenithStatHelper.stat(statId) == null){
            fail(source.getSource(),statId +" is not a valid stat");
        }
        Component statName = ZenithStatHelper.stat(statId).getName();
        double stat = player.getData(ZenithAttachments.STAT_HOLDER).getStat(ZenithStatHelper.stat(statId));
        source.getSource().sendSuccess(()->
                        Component.empty().append(statName).append(" : "+stat),
                false);

        return 1;
    }
    private static int getBaseStat(CommandContext<CommandSourceStack> source)throws CommandSyntaxException{
        ServerPlayer player = source.getSource().getPlayerOrException();

        Identifier statId = IdentifierArgument.getId(source,"stat");
        if(ZenithStatHelper.stat(statId) == null){
            fail(source.getSource(),statId +" is not a valid stat");
        }
        Component statName = ZenithStatHelper.stat(statId).getName();
        double stat = player.getData(ZenithAttachments.STAT_HOLDER).getBaseStat(ZenithStatHelper.stat(statId));
        source.getSource().sendSuccess(()->
                        Component.empty().append(statName).append(" : "+stat),
                false);

        return 1;
    }
    private static int addFlatModifier(CommandContext<CommandSourceStack> source) throws CommandSyntaxException{
        ServerPlayer player = source.getSource().getPlayerOrException();

        Identifier statId = IdentifierArgument.getId(source,"stat");
        if(ZenithStatHelper.stat(statId) == null){
            fail(source.getSource(),statId +" is not a valid stat");
            return -1;
        }
        Identifier id = IdentifierArgument.getId(source,"modifier_id");
        double value = DoubleArgumentType.getDouble(source,"value");
        int opGroup = IntegerArgumentType.getInteger(source,"op_group");
        Modifier<Double> modifier = Modifier.flat(id,opGroup,value);
        player.getData(ZenithAttachments.STAT_HOLDER).addFlatModifier(ZenithStatHelper.stat(statId),modifier);
        source.getSource().sendSuccess(()->
                        Component.literal("stat updated"),
                false);

        return 1;
    }
    private static int addMultiplierModifier(CommandContext<CommandSourceStack> source) throws CommandSyntaxException{
        ServerPlayer player = source.getSource().getPlayerOrException();

        Identifier statId = IdentifierArgument.getId(source,"stat");
        if(ZenithStatHelper.stat(statId) == null){
            fail(source.getSource(),statId +" is not a valid stat");
        }
        Identifier id = IdentifierArgument.getId(source,"modifier_id");
        Identifier group = IdentifierArgument.getId(source,"group");
        double value = DoubleArgumentType.getDouble(source,"value");
        int opGroup = IntegerArgumentType.getInteger(source,"op_group");
        Modifier<Double> modifier = Modifier.multiplier(id,group,opGroup,value);
        player.getData(ZenithAttachments.STAT_HOLDER).addMultiplierModifier(ZenithStatHelper.stat(statId),modifier);
        source.getSource().sendSuccess(()->
                        Component.literal("stat updated"),
                false);

        return 1;
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
