package net.zic.zenithlib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.zic.zenithlib.api.SpatialRuptureAPI;

import java.util.concurrent.CompletableFuture;

public final class RandomTeleportCommand {

    private static final SimpleCommandExceptionType NO_PLAYER_EXCEPTION =
            new SimpleCommandExceptionType(Component.literal("This command can only be used by a player."));
    private static final SimpleCommandExceptionType INVALID_TIER_EXCEPTION =
            new SimpleCommandExceptionType(Component.literal("Tier must be between 1 and 4."));

    private RandomTeleportCommand() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rtp")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

                        .executes(ctx -> executeRTP(ctx, 1))

                        .then(Commands.argument("tier", IntegerArgumentType.integer(1, 4))
                                .executes(ctx -> executeRTP(
                                        ctx,
                                        IntegerArgumentType.getInteger(ctx, "tier")
                                ))
                        )
        );
    }

    private static int executeRTP(CommandContext<CommandSourceStack> ctx, int tier) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            throw NO_PLAYER_EXCEPTION.create();
        }

        ServerLevel world = source.getLevel();
        TierConfig config = TierConfig.byTier(tier);

        source.sendSuccess(() -> Component.literal(
                "Rupturing space... searching within " + config.radius + " blocks (Tier " + tier + ")."
        ).withStyle(ChatFormatting.AQUA), false);

        CompletableFuture<Boolean> future = SpatialRuptureAPI.randomTeleport(player, world, config.radius);

        future.thenAccept(success -> source.getServer().execute(() -> {
            if (success) {
                source.sendSuccess(() -> Component.literal(
                        "Spatial rupture successful! You have been displaced to a new location."
                ).withStyle(ChatFormatting.GREEN), false);
            } else {
                source.sendFailure(Component.literal(
                        "Spatial rupture failed — no safe location found within the specified radius."
                ).withStyle(ChatFormatting.RED));
            }
        }));

        return 1;
    }

    private enum TierConfig {
        TIER_1(1, 1_000,   "Local",      ChatFormatting.GREEN),
        TIER_2(2, 5_000,   "Regional",   ChatFormatting.YELLOW),
        TIER_3(3, 15_000,  "Continental",ChatFormatting.GOLD),
        TIER_4(4, 50_000,  "Planetary",  ChatFormatting.RED);

        final int tier;
        final int radius;
        final String label;
        final ChatFormatting color;

        TierConfig(int tier, int radius, String label, ChatFormatting color) {
            this.tier = tier;
            this.radius = radius;
            this.label = label;
            this.color = color;
        }

        static TierConfig byTier(int tier) {
            for (TierConfig t : values()) {
                if (t.tier == tier) return t;
            }
            return TIER_1;
        }
    }
}