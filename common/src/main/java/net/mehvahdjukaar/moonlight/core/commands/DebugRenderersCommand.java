package net.mehvahdjukaar.moonlight.core.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.Minecraft;
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
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DebugRenderersCommand {

    private static final ResourceKeyArgument<Structure> STRUCTURE_ARG = ResourceKeyArgument.key(Registries.STRUCTURE);

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("debug_renderers")
                .requires(cs -> cs.hasPermission(2))

                .then(Commands.literal("neighbors_update")
                        .executes(DebugRenderersCommand::neighbors))
                .then(Commands.literal("navigation")
                        .executes(DebugRenderersCommand::navigation)
                        .then(Commands.argument("entity", ResourceKeyArgument.key(Registries.ENTITY_TYPE))
                                .executes(DebugRenderersCommand::navigation)
                                .then(Commands.argument("active", BoolArgumentType.bool())
                                        .executes(DebugRenderersCommand::navigation)
                                )
                        )
                )
                .then(Commands.literal("goals_selector")
                        .executes(DebugRenderersCommand::goals)
                        .then(Commands.argument("entity", ResourceKeyArgument.key(Registries.ENTITY_TYPE))
                                .executes(DebugRenderersCommand::goals)
                                .then(Commands.argument("active", BoolArgumentType.bool())
                                        .executes(DebugRenderersCommand::goals)
                                )
                        )
                )
                .then(Commands.literal("structures")
                        .executes(DebugRenderersCommand::structures)
                        .then(Commands.argument("structure", STRUCTURE_ARG)
                                .executes(DebugRenderersCommand::structures)
                                .then(Commands.argument("active", BoolArgumentType.bool())
                                        .executes(DebugRenderersCommand::structures)
                                )
                        )
                )

                .then(Commands.literal("section_path")
                        .executes(DebugRenderersCommand::sectionPath))

                .then(Commands.literal("smart_cull")
                        .executes(DebugRenderersCommand::smartCull))

                .then(Commands.literal("frustum")
                        .then(Commands.literal("capture")
                                .executes(DebugRenderersCommand::captureFrustum))
                        .then(Commands.literal("kill")
                                .executes(DebugRenderersCommand::killFrustum))
                )

                .then(Commands.literal("section_visibility")
                        .executes(DebugRenderersCommand::sectionVisibility))

                .then(Commands.literal("wireframe")
                        .executes(DebugRenderersCommand::wireframe))

                .then(Commands.literal("water")
                        .executes(DebugRenderersCommand::water))
                .then(Commands.literal("heightmap")
                        .executes(DebugRenderersCommand::heightmap))
                .then(Commands.literal("collision")
                        .executes(DebugRenderersCommand::collision))
                .then(Commands.literal("support")
                        .executes(DebugRenderersCommand::support))
                .then(Commands.literal("light")
                        .executes(DebugRenderersCommand::light))
                .then(Commands.literal("world_gen_attempts")
                        .executes(DebugRenderersCommand::worldGen))
                .then(Commands.literal("solid_faces")
                        .executes(DebugRenderersCommand::solidFaces))
                .then(Commands.literal("game_events")
                        .executes(DebugRenderersCommand::gameEvents))
                .then(Commands.literal("sky_light_sections")
                        .executes(DebugRenderersCommand::skyLightSections))
                .then(Commands.literal("breeze")
                        .executes(DebugRenderersCommand::breeze));
    }

    private static void sendSuccess(CommandContext<CommandSourceStack> ctx, boolean value, String name) {
        ctx.getSource().sendSuccess(() -> Component.translatable(
                value ? "commands.moonlight." + name + ".on" : "commands.moonlight." + name + ".off"
        ), false);
    }

    private static int water(CommandContext<CommandSourceStack> ctx) {
        DEBUG_WATER = !DEBUG_WATER;
        sendSuccess(ctx, DEBUG_WATER, "debug_water");
        return 1;
    }

    private static int heightmap(CommandContext<CommandSourceStack> ctx) {
        DEBUG_HEIGHTMAP = !DEBUG_HEIGHTMAP;
        sendSuccess(ctx, DEBUG_HEIGHTMAP, "debug_heightmap");
        return 1;
    }

    private static int sectionPath(CommandContext<CommandSourceStack> ctx) {
        var mc = Minecraft.getInstance();
        mc.sectionPath = !mc.sectionPath;
        sendSuccess(ctx, mc.sectionPath, "section_path");
        return 1;
    }

    private static int collision(CommandContext<CommandSourceStack> ctx) {
        DEBUG_COLLISION = !DEBUG_COLLISION;
        sendSuccess(ctx, DEBUG_COLLISION, "collision");
        return 1;
    }

    private static int support(CommandContext<CommandSourceStack> ctx) {
        DEBUG_SUPPORT = !DEBUG_SUPPORT;
        sendSuccess(ctx, DEBUG_SUPPORT, "support");
        return 1;
    }

    private static int light(CommandContext<CommandSourceStack> ctx) {
        DEBUG_LIGHT = !DEBUG_LIGHT;
        sendSuccess(ctx, DEBUG_LIGHT, "light");
        return 1;
    }

    private static int worldGen(CommandContext<CommandSourceStack> ctx) {
        DEBUG_WORLD_GEN_ATTEMPTS = !DEBUG_WORLD_GEN_ATTEMPTS;
        sendSuccess(ctx, DEBUG_WORLD_GEN_ATTEMPTS, "world_gen_attempts");
        return 1;
    }

    private static int solidFaces(CommandContext<CommandSourceStack> ctx) {
        DEBUG_SOLID_FACES = !DEBUG_SOLID_FACES;
        sendSuccess(ctx, DEBUG_SOLID_FACES, "solid_faces");
        return 1;
    }

    private static int gameEvents(CommandContext<CommandSourceStack> ctx) {
        DEBUG_GAME_EVENTS = !DEBUG_GAME_EVENTS;
        sendSuccess(ctx, DEBUG_GAME_EVENTS, "game_events");
        return 1;
    }

    private static int skyLightSections(CommandContext<CommandSourceStack> ctx) {
        DEBUG_SKY_LIGHT_SECTIONS = !DEBUG_SKY_LIGHT_SECTIONS;
        sendSuccess(ctx, DEBUG_SKY_LIGHT_SECTIONS, "sky_light_sections");
        return 1;
    }

    private static int breeze(CommandContext<CommandSourceStack> ctx) {
        DEBUG_BREEZE = !DEBUG_BREEZE;
        sendSuccess(ctx, DEBUG_BREEZE, "breeze");
        return 1;
    }

    private static int smartCull(CommandContext<CommandSourceStack> ctx) {
        var mc = Minecraft.getInstance();
        mc.smartCull = !mc.smartCull;
        sendSuccess(ctx, mc.smartCull, "smart_cull");
        return 1;
    }

    private static int captureFrustum(CommandContext<CommandSourceStack> ctx) {
        var mc = Minecraft.getInstance();
        mc.levelRenderer.captureFrustum();
        ctx.getSource().sendSuccess(() ->
                Component.translatable("commands.moonlight.frustum.captured"), false);
        return 1;
    }

    private static int killFrustum(CommandContext<CommandSourceStack> ctx) {
        var mc = Minecraft.getInstance();
        mc.levelRenderer.killFrustum();
        ctx.getSource().sendSuccess(() ->
                Component.translatable("commands.moonlight.frustum.killed"), false);
        return 1;
    }

    private static int sectionVisibility(CommandContext<CommandSourceStack> ctx) {
        var mc = Minecraft.getInstance();
        mc.sectionVisibility = !mc.sectionVisibility;
        sendSuccess(ctx, mc.sectionVisibility, "section_visibility");
        return 1;
    }

    private static int wireframe(CommandContext<CommandSourceStack> ctx) {
        var mc = Minecraft.getInstance();
        mc.wireframe = !mc.wireframe;
        sendSuccess(ctx, mc.wireframe, "wireframe");
        return 1;
    }

    private static int neighbors(CommandContext<CommandSourceStack> context) {
        DEBUG_NEIGHBOR_UPDATES = !DEBUG_NEIGHBOR_UPDATES;
        sendSuccess(context, DEBUG_NEIGHBOR_UPDATES, "neighbor_updates");
        return 0;
    }

    private static int navigation(CommandContext<CommandSourceStack> context) {
        toggle(context, "entity", DEBUG_PATHFINDING, "navigation");
        return 0;
    }


    private static int goals(CommandContext<CommandSourceStack> context) {
        toggle(context, "entity", DEBUG_GOAL_SELECTOR, "goal_selector");
        return 0;
    }

    private static int structures(CommandContext<CommandSourceStack> context) {
        toggle(context, "structure", DEBUG_STRUCTURES_BB, "structures");
        return 0;
    }


    private static <T> void toggle(CommandContext<CommandSourceStack> ctx,
                                   String keyKey, DebugConfig<T> config, String translation) {
        Boolean active = getOptArg(ctx, "active", Boolean.class);

        Component comp;
        var key = getResourceKey(ctx, keyKey, config.registryKey);
        if (key.isPresent()) {
            ResourceLocation location = key.get().location();
            active = active != null ? active : !config.keys.contains(location);
            if (active) {
                comp = Component.translatable("commands.moonlight." + translation + ".add", location.toString());
            } else {
                comp = Component.translatable("commands.moonlight." + translation + ".remove", location.toString());
            }
            config.toggleKey(location, active);
        } else {
            active = !config.allActive;
            if (active) {
                comp = Component.translatable("commands.moonlight." + translation + ".on");
            } else {
                comp = Component.translatable("commands.moonlight." + translation + ".off");
            }
            config.setActive(active);
        }
        ctx.getSource().sendSuccess(() -> comp, false);
    }

    @Nullable
    private static Boolean getOptArg(CommandContext<CommandSourceStack> ctx, String active, Class<Boolean> booleanClass) {
        try {
            return ctx.getArgument(active, booleanClass);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static <T> Optional<ResourceKey<T>> getResourceKey(CommandContext<CommandSourceStack> ctx, String name,
                                                               ResourceKey<Registry<T>> registryKey) {
        try {
            ResourceKey<?> key = ctx.getArgument(name, ResourceKey.class);
            return key.cast(registryKey);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static final DebugConfig<EntityType<?>> DEBUG_PATHFINDING = new DebugConfig<>(Registries.ENTITY_TYPE);
    public static final DebugConfig<EntityType<?>> DEBUG_GOAL_SELECTOR = new DebugConfig<>(Registries.ENTITY_TYPE);
    public static final DebugConfig<Structure> DEBUG_STRUCTURES_BB = new DebugConfig<>(Registries.STRUCTURE);
    public static boolean DEBUG_NEIGHBOR_UPDATES = false;

    //client only. won't work on servers
    public static boolean DEBUG_WATER = false;
    public static boolean DEBUG_HEIGHTMAP = false;
    public static boolean DEBUG_COLLISION = false;
    public static boolean DEBUG_SUPPORT = false;
    public static boolean DEBUG_LIGHT = false;
    public static boolean DEBUG_WORLD_GEN_ATTEMPTS = false;
    public static boolean DEBUG_SOLID_FACES = false;
    public static boolean DEBUG_GAME_EVENTS = false;
    public static boolean DEBUG_SKY_LIGHT_SECTIONS = false;
    public static boolean DEBUG_BREEZE = false;


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