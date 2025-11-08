package net.mehvahdjukaar.moonlight.core.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DebugRenderersCommand {

    private static final ResourceKeyArgument<Structure> STRUCTURE_ARG = ResourceKeyArgument.key(Registries.STRUCTURE);

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("debug_renderers")
                .requires(cs -> cs.hasPermission(2))

                .then(Commands.literal("neighbors_update")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(DebugRenderersCommand::neighbors)))
                .then(Commands.literal("navigation")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(DebugRenderersCommand::navigation)
                                .then(Commands.argument("entity", ResourceKeyArgument.key(Registries.ENTITY_TYPE))
                                        .then(Commands.argument("active", BoolArgumentType.bool())
                                                .executes(DebugRenderersCommand::navigation)
                                        )
                                )
                        )
                )
                .then(Commands.literal("goals_selector")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(DebugRenderersCommand::goals)
                                .then(Commands.argument("entity", ResourceKeyArgument.key(Registries.ENTITY_TYPE))
                                        .then(Commands.argument("active", BoolArgumentType.bool())
                                                .executes(DebugRenderersCommand::goals)
                                        )
                                )

                        )
                )
                .then(Commands.literal("structures")
                        .then(Commands.argument("active", BoolArgumentType.bool())
                                .executes(DebugRenderersCommand::structures)
                                .then(Commands.argument("structure", STRUCTURE_ARG)
                                        .then(Commands.argument("active", BoolArgumentType.bool())
                                                .executes(DebugRenderersCommand::structures)
                                        )
                                )
                        )
                );
    }

    private static int navigation(CommandContext<CommandSourceStack> context) {
        toggle(context, "entity", Registries.ENTITY_TYPE, DEBUG_PATHFINDING, "navigation");
        return 0;
    }

    private static int neighbors(CommandContext<CommandSourceStack> context) {
        DEBUG_NEIGHBOR_UPDATES = BoolArgumentType.getBool(context, "active");
        context.getSource().sendSuccess(() ->
                Component.translatable("commands.moonlight.neighbor_updates", DEBUG_NEIGHBOR_UPDATES), false);
        return 0;
    }


    private static int goals(CommandContext<CommandSourceStack> context) {
        toggle(context, "entity", Registries.ENTITY_TYPE, DEBUG_GOAL_SELECTOR, "goal_selector");
        return 0;
    }

    private static int structures(CommandContext<CommandSourceStack> context) {
        toggle(context, "structure", Registries.STRUCTURE, DEBUG_STRUCTURES_BB, "structures");
        return 0;
    }


    private static <T> void toggle(CommandContext<CommandSourceStack> context,
                                   String keyKey, ResourceKey<Registry<T>> registry,
                                   DebugConfig config, String translation) {
        boolean active = BoolArgumentType.getBool(context, "active");
        ResourceKey<T> key = getResourceKey(context, keyKey, registry).orElse(null);
        Component comp;
        if (key != null) {
            ResourceLocation location = key.location();
            if (active) {
                comp = Component.translatable("commands.moonlight." + translation + ".add", location);
            } else {
                comp = Component.translatable("commands.moonlight." + translation + ".remove", location);
            }
            config.toggleKey(location, active);
        } else {
            if (active) {
                comp = Component.translatable("commands.moonlight." + translation + ".on");

            } else {
                comp = Component.translatable("commands.moonlight." + translation + ".off");
            }
            config.setActive(active);
        }
        context.getSource().sendSuccess(() -> comp, false);
    }

    private static <T> Optional<ResourceKey<T>> getResourceKey(CommandContext<CommandSourceStack> ctx, String name,
                                                               ResourceKey<Registry<T>> registryKey) {
        ResourceKey<?> key = ctx.getArgument(name, ResourceKey.class);
        return key.cast(registryKey);
    }

    public static boolean DEBUG_NEIGHBOR_UPDATES = false;
    public static final DebugConfig<EntityType<?>> DEBUG_PATHFINDING = new DebugConfig<>(Registries.ENTITY_TYPE);
    public static final DebugConfig<EntityType<?>> DEBUG_GOAL_SELECTOR = new DebugConfig<>(Registries.ENTITY_TYPE);
    public static final DebugConfig<Structure> DEBUG_STRUCTURES_BB = new DebugConfig<>(Registries.STRUCTURE);


    public static class DebugConfig<T> {
        private final ResourceKey<Registry<T>> registryKey;
        private final Set<ResourceLocation> keys = new HashSet<>();
        private boolean allActive = false;

        public DebugConfig(ResourceKey<Registry<T>> registryKey) {
            this.registryKey = registryKey;
        }

        public boolean isActive(ResourceLocation key) {
            if (allActive) return true;
            if (keys.isEmpty()) return false;
            return keys.contains(key);
        }

        public boolean isActive(T element, Level level) {
            return isActive(element, level.registryAccess());
        }

        public boolean isActive(T element, RegistryAccess access) {
            if (allActive) return true;
            if (keys.isEmpty()) return false;
            return isActive(access.registryOrThrow(registryKey).getKey(element));
        }

        public boolean isActive(Holder<T> holder) {
            return isActive(holder.unwrapKey().get().location());
        }

        private void setActive(boolean active) {
            this.allActive = active;
        }

        private void toggleKey(ResourceLocation key, boolean active) {
            if (active) {
                keys.add(key);
            } else {
                keys.remove(key);
            }
        }

    }
}