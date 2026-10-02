package net.zic.zenithlib.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.zic.zenithlib.common.ZenithAttachments;
import net.zic.zenithlib.custom_attributes.SuppressedAttributeHelper;
import net.zic.zenithlib.custom_attributes.ZenithAttributeHolder;

import java.util.Formatter;

public class ZenithAttributeCommands {
    public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("suppression")
                        .requires(Commands.hasPermission(Commands.LEVEL_ALL))
                        .then(Commands.argument("attribute", IdentifierArgument.id())
                            .suggests((context, builder) ->
                                    SharedSuggestionProvider.suggestResource(
                                            SuppressedAttributeHelper.getSuppressedAttributes(),
                                            builder))

                            .then(Commands.literal("set")
                                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(0,1))
                                            .executes(ZenithAttributeCommands::setSuppression)
                                    )

                            )
                            .then(Commands.literal("get")
                                    .executes(ZenithAttributeCommands::getSuppression)
                            )
                        )
        );
    }

    private static int setSuppression(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer player = source.getSource().getPlayerOrException();
        Identifier attribute = IdentifierArgument.getId(source,"attribute");
        double suppression = DoubleArgumentType.getDouble(source,"value");

        if(!player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).suppress(attribute,suppression)){
            fail(source.getSource(),"unable to suppress "+attribute);
            return 0;
        }else source.getSource().sendSuccess(()->Component.literal("suppressed to " + String.format("%.2f%%", suppression * 100)),false);
        return 1;
    }
    private static int getSuppression(CommandContext<CommandSourceStack> source) throws CommandSyntaxException {
        ServerPlayer player = source.getSource().getPlayerOrException();
        Identifier attribute = IdentifierArgument.getId(source,"attribute");
        if(!SuppressedAttributeHelper.isSuppressible(attribute)){
            fail(source.getSource(),"attribute "+attribute +" is cannot be suppressed");
            return 0;
        }
        double suppression = player.getData(ZenithAttachments.ATTRIBUTE_HOLDER).getSuppression(SuppressedAttributeHelper.getAttribute(attribute));
        source.getSource().sendSuccess(()->Component.literal("suppression : "+String.format("%.2f%%", suppression * 100)),false);
        return 1;
    }

    private static void fail(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message).withStyle(ChatFormatting.RED));
    }
}
