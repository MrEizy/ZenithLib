package net.zic.zenithlib.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ZenithInspectCommand {
    private static final int MAX_VALUE_LENGTH = 240;
    private static final double PICK_DISTANCE = 24.0D;

    private static final List<ItemInspector> ITEM_INSPECTORS = new CopyOnWriteArrayList<>();
    private static final List<EntityInspector> ENTITY_INSPECTORS = new CopyOnWriteArrayList<>();
    private static final List<BlockInspector> BLOCK_INSPECTORS = new CopyOnWriteArrayList<>();

    private ZenithInspectCommand() {}

    public static void registerItemInspector(ItemInspector inspector) {
        ITEM_INSPECTORS.add(inspector);
    }

    public static void registerEntityInspector(EntityInspector inspector) {
        ENTITY_INSPECTORS.add(inspector);
    }

    public static void registerBlockInspector(BlockInspector inspector) {
        BLOCK_INSPECTORS.add(inspector);
    }

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        registerCommands(event.getDispatcher());
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("zinspect")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))

                        .then(Commands.literal("item")
                                .executes(ctx -> inspectHeldItem(ctx.getSource())))

                        .then(Commands.literal("entity")
                                .executes(ctx -> inspectLookedAtEntity(ctx.getSource()))
                                .then(Commands.argument("target", EntityArgument.entity())
                                        .executes(ctx -> inspectEntity(
                                                ctx.getSource(),
                                                EntityArgument.getEntity(ctx, "target")
                                        ))))

                        .then(Commands.literal("block")
                                .executes(ctx -> inspectLookedAtBlock(ctx.getSource()))
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(ctx -> inspectBlock(
                                                ctx.getSource(),
                                                BlockPosArgument.getLoadedBlockPos(ctx, "pos")
                                        ))))
        );
    }

    private static int inspectHeldItem(CommandSourceStack source) throws CommandSyntaxException {
        ItemStack stack = source.getPlayerOrException().getItemInHand(InteractionHand.MAIN_HAND);

        if (stack.isEmpty()) {
            fail(source, "Main hand is empty.");
            return 0;
        }

        List<Component> lines = new ArrayList<>();
        addItemStack(source, lines, stack, "Held Item");
        send(source, lines);
        return lines.size();
    }

    private static int inspectLookedAtEntity(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        Entity entity = findLookedAtEntity(player, PICK_DISTANCE);
        if (entity == null) {
            fail(source, "No entity found in sight.");
            return 0;
        }

        return inspectEntity(source, entity);
    }

    private static int inspectEntity(CommandSourceStack source, Entity entity) {
        List<Component> lines = new ArrayList<>();

        Identifier entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        header(lines, "Entity");
        pair(lines, "Type", idString(entityId));
        pair(lines, "Name", entity.getName().getString());
        pair(lines, "Entity ID", String.valueOf(entity.getId()));
        pair(lines, "UUID", entity.getStringUUID());
        pair(lines, "Position", formatVec(entity.position()));
        pair(lines, "Block Position", formatBlockPos(entity.blockPosition()));
        pair(lines, "Delta Movement", formatVec(entity.getDeltaMovement()));
        pair(lines, "On Ground", String.valueOf(entity.onGround()));
        pair(lines, "Fire Ticks", String.valueOf(entity.getRemainingFireTicks()));

        blank(lines);
        section(lines, "Tags");
        addEntityTags(lines, entity);

        if (entity instanceof ItemEntity itemEntity) {
            blank(lines);
            addItemStack(source, lines, itemEntity.getItem(), "Contained ItemStack");
        }

        addEntityInspectorOutput(source, lines, entity);

        send(source, lines);
        return lines.size();
    }

    private static int inspectLookedAtBlock(CommandSourceStack source) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();

        HitResult hit = player.pick(PICK_DISTANCE, 0.0F, false);
        if (!(hit instanceof BlockHitResult blockHit) || hit.getType() != HitResult.Type.BLOCK) {
            fail(source, "No block found in sight.");
            return 0;
        }

        return inspectBlock(source, blockHit.getBlockPos());
    }

    private static int inspectBlock(CommandSourceStack source, BlockPos pos) {
        BlockState state = source.getLevel().getBlockState(pos);
        BlockEntity blockEntity = source.getLevel().getBlockEntity(pos);

        List<Component> lines = new ArrayList<>();

        Identifier blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        header(lines, "Block");
        pair(lines, "Block", idString(blockId));
        pair(lines, "Position", formatBlockPos(pos));
        pair(lines, "Air", String.valueOf(state.isAir()));
        pair(lines, "Fluid", state.getFluidState().isEmpty() ? "none" : state.getFluidState().toString());

        blank(lines);
        section(lines, "Properties");
        addBlockProperties(lines, state);

        blank(lines);
        section(lines, "Tags");
        addBlockTags(lines, state);

        blank(lines);
        section(lines, "Block Entity");

        if (blockEntity == null) {
            muted(lines, "  none");
        } else {
            Identifier blockEntityId = BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType());

            pair(lines, "Type", idString(blockEntityId));
            pair(lines, "Persistent Data", blockEntity.getPersistentData().isEmpty()
                    ? "none"
                    : blockEntity.getPersistentData().toString());

            if (blockEntity instanceof Container container) {
                blank(lines);
                section(lines, "Inventory");
                addContainerContents(lines, container);
            }
        }

        addBlockInspectorOutput(source, lines, pos, state, blockEntity);

        send(source, lines);
        return lines.size();
    }

    private static void addItemStack(CommandSourceStack source, List<Component> lines, ItemStack stack, String title) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        header(lines, title);
        pair(lines, "Item", idString(itemId));
        pair(lines, "Name", stack.getDisplayName().getString());
        pair(lines, "Count", String.valueOf(stack.getCount()));
        pair(lines, "Max Stack Size", String.valueOf(stack.getMaxStackSize()));

        if (stack.isDamageableItem()) {
            int maxDamage = stack.getMaxDamage();
            int damage = stack.getDamageValue();
            int remaining = Math.max(0, maxDamage - damage);

            pair(lines, "Damage", damage + " / " + maxDamage);
            pair(lines, "Durability", remaining + " / " + maxDamage);
        }

        blank(lines);
        section(lines, "Components");
        addEffectiveComponents(lines, stack);

        blank(lines);
        section(lines, "Stack Patch");
        addComponentPatch(lines, stack);

        blank(lines);
        section(lines, "Tags");
        addItemTags(lines, stack);

        addItemInspectorOutput(source, lines, stack);
    }

    private static void addEffectiveComponents(List<Component> lines, ItemStack stack) {
        List<TypedDataComponent<?>> components = new ArrayList<>();

        for (TypedDataComponent<?> component : stack.getComponents()) {
            components.add(component);
        }

        components.sort(Comparator.comparing(component -> componentId(component.type())));

        if (components.isEmpty()) {
            muted(lines, "  none");
            return;
        }

        for (TypedDataComponent<?> component : components) {
            pair(lines, componentId(component.type()), prettyValue(component.value()));
        }
    }

    private static void addComponentPatch(List<Component> lines, ItemStack stack) {
        List<Map.Entry<DataComponentType<?>, Optional<?>>> entries = stack.getComponentsPatch()
                .entrySet()
                .stream()
                .sorted(Comparator.comparing(entry -> componentId(entry.getKey())))
                .toList();

        if (entries.isEmpty()) {
            muted(lines, "  none");
            return;
        }

        for (Map.Entry<DataComponentType<?>, Optional<?>> entry : entries) {
            String value = entry.getValue()
                    .map(ZenithInspectCommand::prettyValue)
                    .orElse("<removed>");

            pair(lines, componentId(entry.getKey()), value);
        }
    }

    private static void addItemTags(List<Component> lines, ItemStack stack) {
        List<String> tags = stack.typeHolder()
                .tags()
                .map(tag -> "#" + tag.location())
                .sorted()
                .toList();

        if (tags.isEmpty()) {
            muted(lines, "  none");
            return;
        }

        for (String tag : tags) {
            valueLine(lines, tag);
        }
    }

    private static void addEntityTags(List<Component> lines, Entity entity) {
        List<String> tags = entity.entityTags()
                .stream()
                .sorted()
                .toList();

        if (tags.isEmpty()) {
            muted(lines, "  none");
            return;
        }

        for (String tag : tags) {
            valueLine(lines, "#" + tag);
        }
    }

    private static void addBlockProperties(List<Component> lines, BlockState state) {
        List<Property.Value<?>> values = state.getValues()
                .sorted(Comparator.comparing(value -> value.property().getName()))
                .toList();

        if (values.isEmpty()) {
            muted(lines, "  none");
            return;
        }

        for (Property.Value<?> value : values) {
            pair(lines, value.property().getName(), value.valueName());
        }
    }

    private static void addBlockTags(List<Component> lines, BlockState state) {
        List<String> tags = state.tags()
                .map(tag -> "#" + tag.location())
                .sorted()
                .toList();

        if (tags.isEmpty()) {
            muted(lines, "  none");
            return;
        }

        for (String tag : tags) {
            valueLine(lines, tag);
        }
    }

    private static void addContainerContents(List<Component> lines, Container container) {
        boolean foundAny = false;

        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);

            if (stack.isEmpty()) {
                continue;
            }

            foundAny = true;

            Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
            pair(lines, "Slot " + slot, stack.getCount() + "x " + idString(itemId));
        }

        if (!foundAny) {
            muted(lines, "  empty");
        }
    }

    private static void addItemInspectorOutput(CommandSourceStack source, List<Component> lines, ItemStack stack) {
        if (ITEM_INSPECTORS.isEmpty()) {
            return;
        }

        List<Component> extra = new ArrayList<>();
        InspectionSink sink = new InspectionSink(extra);

        for (ItemInspector inspector : ITEM_INSPECTORS) {
            try {
                inspector.inspect(source, stack, sink);
            } catch (Exception exception) {
                sink.pair("Inspector Error", exception.getClass().getSimpleName() + ": " + exception.getMessage());
            }
        }

        if (!extra.isEmpty()) {
            blank(lines);
            section(lines, "Mod Inspectors");
            lines.addAll(extra);
        }
    }

    private static void addEntityInspectorOutput(CommandSourceStack source, List<Component> lines, Entity entity) {
        if (ENTITY_INSPECTORS.isEmpty()) {
            return;
        }

        List<Component> extra = new ArrayList<>();
        InspectionSink sink = new InspectionSink(extra);

        for (EntityInspector inspector : ENTITY_INSPECTORS) {
            try {
                inspector.inspect(source, entity, sink);
            } catch (Exception exception) {
                sink.pair("Inspector Error", exception.getClass().getSimpleName() + ": " + exception.getMessage());
            }
        }

        if (!extra.isEmpty()) {
            blank(lines);
            section(lines, "Mod Inspectors");
            lines.addAll(extra);
        }
    }

    private static void addBlockInspectorOutput(
            CommandSourceStack source,
            List<Component> lines,
            BlockPos pos,
            BlockState state,
            BlockEntity blockEntity
    ) {
        if (BLOCK_INSPECTORS.isEmpty()) {
            return;
        }

        List<Component> extra = new ArrayList<>();
        InspectionSink sink = new InspectionSink(extra);

        for (BlockInspector inspector : BLOCK_INSPECTORS) {
            try {
                inspector.inspect(source, pos, state, blockEntity, sink);
            } catch (Exception exception) {
                sink.pair("Inspector Error", exception.getClass().getSimpleName() + ": " + exception.getMessage());
            }
        }

        if (!extra.isEmpty()) {
            blank(lines);
            section(lines, "Mod Inspectors");
            lines.addAll(extra);
        }
    }

    private static Entity findLookedAtEntity(ServerPlayer player, double distance) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 end = eye.add(look.scale(distance));

        AABB searchBox = player.getBoundingBox()
                .expandTowards(look.scale(distance))
                .inflate(1.0D);

        EntityHitResult result = ProjectileUtil.getEntityHitResult(
                player,
                eye,
                end,
                searchBox,
                entity -> !entity.isSpectator() && entity.isPickable(),
                distance * distance
        );

        return result == null ? null : result.getEntity();
    }

    private static String componentId(DataComponentType<?> type) {
        Identifier id = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(type);
        return idString(id);
    }

    private static String idString(Identifier id) {
        return id == null ? "<unregistered>" : id.toString();
    }

    private static String prettyValue(Object value) {
        if (value == null) {
            return "null";
        }

        if (value instanceof Component component) {
            return component.getString();
        }

        return shorten(String.valueOf(value));
    }

    private static String shorten(String value) {
        if (value.length() <= MAX_VALUE_LENGTH) {
            return value;
        }

        return value.substring(0, MAX_VALUE_LENGTH) + "...";
    }

    private static String formatVec(Vec3 vec) {
        return "%.3f, %.3f, %.3f".formatted(vec.x, vec.y, vec.z);
    }

    private static String formatBlockPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static void send(CommandSourceStack source, List<Component> lines) {
        for (Component line : lines) {
            source.sendSuccess(() -> line, false);
        }
    }

    private static void fail(CommandSourceStack source, String message) {
        source.sendFailure(Component.literal(message).withStyle(ChatFormatting.RED));
    }

    private static void header(List<Component> lines, String text) {
        lines.add(Component.literal("[zinspect] " + text)
                .withStyle(ChatFormatting.AQUA));
    }

    private static void section(List<Component> lines, String text) {
        lines.add(Component.literal(text)
                .withStyle(ChatFormatting.AQUA));
    }

    private static void pair(List<Component> lines, String key, String value) {
        lines.add(
                Component.literal("  " + key + ": ")
                        .withStyle(ChatFormatting.DARK_AQUA)
                        .append(Component.literal(shorten(value))
                                .withStyle(ChatFormatting.WHITE))
        );
    }

    private static void valueLine(List<Component> lines, String value) {
        lines.add(Component.literal("  " + value)
                .withStyle(ChatFormatting.WHITE));
    }

    private static void muted(List<Component> lines, String text) {
        lines.add(Component.literal(text)
                .withStyle(ChatFormatting.WHITE));
    }

    private static void blank(List<Component> lines) {
        lines.add(Component.literal(""));
    }

    @FunctionalInterface
    public interface ItemInspector {
        void inspect(CommandSourceStack source, ItemStack stack, InspectionSink sink);
    }

    @FunctionalInterface
    public interface EntityInspector {
        void inspect(CommandSourceStack source, Entity entity, InspectionSink sink);
    }

    @FunctionalInterface
    public interface BlockInspector {
        void inspect(
                CommandSourceStack source,
                BlockPos pos,
                BlockState state,
                BlockEntity blockEntity,
                InspectionSink sink
        );
    }

    public static final class InspectionSink {
        private final List<Component> lines;

        private InspectionSink(List<Component> lines) {
            this.lines = lines;
        }

        public void section(String text) {
            ZenithInspectCommand.section(lines, text);
        }

        public void pair(String key, String value) {
            ZenithInspectCommand.pair(lines, key, value);
        }

        public void line(String value) {
            ZenithInspectCommand.valueLine(lines, value);
        }

        public void line(Component component) {
            lines.add(component);
        }

        public void empty(String value) {
            ZenithInspectCommand.muted(lines, value);
        }

        public void blank() {
            ZenithInspectCommand.blank(lines);
        }
    }
}