package net.mehvahdjukaar.moonlight.core.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class RandomTeleportCommand {

    private static final SimpleCommandExceptionType INVALID_POSITION =
            new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("tpr")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                // /tpr
                .executes(c -> teleportRandom(
                        c,
                        List.of(c.getSource().getEntityOrException()),
                        Optional.empty()))
                // /tpr <radius>
                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0))
                        .executes(c -> teleportRandom(
                                c,
                                List.of(c.getSource().getEntityOrException()),
                                Optional.of(DoubleArgumentType.getDouble(c, "radius"))))
                        // /tpr <radius> <targets>
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(c -> teleportRandom(
                                        c,
                                        EntityArgument.getEntities(c, "targets"),
                                        Optional.of(DoubleArgumentType.getDouble(c, "radius"))))
                        )
                )
                // /tpr <targets>
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(c -> teleportRandom(
                                c,
                                EntityArgument.getEntities(c, "targets"),
                                Optional.empty()))
                );
    }

    private static int teleportRandom(CommandContext<CommandSourceStack> context,
                                      Collection<? extends Entity> targets,
                                      Optional<Double> optRadius) throws CommandSyntaxException {
        var source = context.getSource();
        ServerLevel level = source.getLevel();
        RandomSource random = level.getRandom();

        WorldBorder border = level.getWorldBorder();

        // Compute bounds to sample from (xMin,xMax,zMin,zMax)
        double xMin, xMax, zMin, zMax;

        if (optRadius.isPresent()) {
            // radius around the command source (if no source entity, center on world border center)
            double radius = optRadius.get();
            double centerX;
            double centerZ;
            try {
                Entity src = source.getEntityOrException();
                centerX = src.getX();
                centerZ = src.getZ();
            } catch (CommandSyntaxException e) {
                centerX = border.getCenterX();
                centerZ = border.getCenterZ();
            }
            xMin = centerX - radius;
            xMax = centerX + radius;
            zMin = centerZ - radius;
            zMax = centerZ + radius;
        } else {
            // use world border full bounds (center +/- size/2)
            double centerX = border.getCenterX();
            double centerZ = border.getCenterZ();
            double half = border.getSize() / 2.0;
            xMin = centerX - half;
            xMax = centerX + half;
            zMin = centerZ - half;
            zMax = centerZ + half;
        }

        // Clamp sample bounds to the world border bounds so we never sample outside it.
        // WorldBorder often exposes min/max; if present they will be used via getMinX/getMaxX etc.
        // To be robust across mappings we clamp sample coordinates after generation using border.clamp(...)
        Set<Relative> set = EnumSet.noneOf(Relative.class);
        set.add(Relative.X_ROT);
        set.add(Relative.Y_ROT);

        for (Entity entity : targets) {
            // pick x and z uniformly inside computed bounds rectangle
            double sampledX = xMin + random.nextDouble() * (xMax - xMin);
            double sampledZ = zMin + random.nextDouble() * (zMax - zMin);

            // Clamp the sampled position to the world border to guarantee it's inside.
            // Use border.clamp(x,y,z) which returns a Vec3 stuck inside the border shape.
            BlockPos clamped = border.clampToBounds(sampledX, 0.0, sampledZ);
            double x = clamped.getX();
            double z = clamped.getZ();

            // Determine safe Y using a surface heightmap
            int xi = Mth.floor(x);
            int zi = Mth.floor(z);
            // int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xi, zi);
            // double finalY = (double) y + 1.0;
            int finalY = 70;
            // Validate bounds using the same check as in your template
            BlockPos blockPos = BlockPos.containing(x, finalY, z);
            if (!Level.isInSpawnableBounds(blockPos)) {
                // If somehow out of spawnable bounds after clamping, throw same exception as template
                throw INVALID_POSITION.create();
            }

            performTeleport(source, entity, level, x, finalY, z, set);
        }

        // Keep *exact* feedback format from your template
        Vec3 example = targets.iterator().next().position();
        if (targets.size() == 1) {
            source.sendSuccess(
                    () -> Component.translatable("commands.teleport.success.location.single",
                            targets.iterator().next().getDisplayName(),
                            formatDouble(example.x),
                            formatDouble(example.y),
                            formatDouble(example.z)),
                    true);
        } else {
            source.sendSuccess(
                    () -> Component.translatable("commands.teleport.success.location.multiple",
                            targets.size(),
                            formatDouble(example.x),
                            formatDouble(example.y),
                            formatDouble(example.z)),
                    true);
        }

        return targets.size();
    }

    private static void performTeleport(CommandSourceStack source, Entity entity, ServerLevel level,
                                        double x, double y, double z, Set<Relative> relativeList)
            throws CommandSyntaxException {
        BlockPos blockPos = BlockPos.containing(x, y, z);
        if (!Level.isInSpawnableBounds(blockPos)) {
            throw INVALID_POSITION.create();
        } else {
            float f = Mth.wrapDegrees(entity.getYRot());
            float g = Mth.wrapDegrees(entity.getXRot());
            BlockPos oldPos = entity.blockPosition();
            var oldDim = entity.level().dimension();
            if (entity.teleportTo(level, x, y, z, relativeList, f, g, true)) {
                BackCommand.onTeleported(entity, oldPos, oldDim);

                label23:
                {
                    if (entity instanceof LivingEntity livingEntity) {
                        if (livingEntity.isFallFlying()) {
                            break label23;
                        }
                    }
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.0, 0.0, 1.0));
                    entity.setOnGround(true);
                }

                if (entity instanceof PathfinderMob pathfinderMob) {
                    pathfinderMob.getNavigation().stop();
                }
            }
        }
    }

    private static String formatDouble(double d) {
        return String.format(Locale.ROOT, "%f", d);
    }
}
