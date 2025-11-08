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
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.*;

public class RandomTeleportCommand {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        // /tpr [radius] [targets]
        return Commands.literal("tpr")
                .requires((p) -> p.hasPermission(2))
                // no radius, default to worldborder
                .executes(c -> teleportRandom(c,
                        List.of(c.getSource().getEntityOrException()),
                        Optional.empty()))
                .then(Commands.argument("radius", DoubleArgumentType.doubleArg(0.0))
                        .executes(c -> teleportRandom(c,
                                List.of(c.getSource().getEntityOrException()),
                                Optional.of(DoubleArgumentType.getDouble(c, "radius"))))
                        .then(Commands.argument("targets", EntityArgument.entities())
                                .executes(c -> teleportRandom(c,
                                        EntityArgument.getEntities(c, "targets"),
                                        Optional.of(DoubleArgumentType.getDouble(c, "radius"))))
                        )
                )
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(c -> teleportRandom(c,
                                EntityArgument.getEntities(c, "targets"),
                                Optional.empty()))
                );
    }

    private static int teleportRandom(CommandContext<CommandSourceStack> context,
                                      Collection<? extends Entity> targets,
                                      Optional<Double> optRadius) throws CommandSyntaxException {
        var source = context.getSource();
        ServerLevel level = source.getLevel();
        RandomSource random = source.getLevel().getRandom();

        // determine center and radius
        double centerX;
        double centerZ;
        double radius;

        if (optRadius.isPresent()) {
            radius = optRadius.get();
            // center around the command source entity if present, otherwise worldborder center
            try {
                Entity srcEntity = source.getEntityOrException();
                centerX = srcEntity.getX();
                centerZ = srcEntity.getZ();
            } catch (CommandSyntaxException e) {
                // no entity source -> use world border center
                WorldBorder border = level.getWorldBorder();
                centerX = border.getCenterX();
                centerZ = border.getCenterZ();
            }
        } else {
            // no radius provided -> choose within world border
            WorldBorder border = level.getWorldBorder();
            centerX = border.getCenterX();
            centerZ = border.getCenterZ();
            // radius is half the world border size (conservative)
            radius = border.getSize() / 2.0;
            // if something weird (infinite / zero), fallback to a large safe value
            if (!(radius > 0) || Double.isInfinite(radius)) {
                radius = 1000000.0; // fallback 1e6 — still big but within spawnable bounds checking later
            }
        }

        Set<RelativeMovement> set = EnumSet.noneOf(RelativeMovement.class);
        set.add(RelativeMovement.X_ROT);
        set.add(RelativeMovement.Y_ROT);

        int teleported = 0;
        // We'll attempt to find a spawnable position for each entity.
        for (Entity entity : targets) {

            // choose random x,z within radius (uniform in circle)
            double angle = random.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(random.nextDouble()) * radius; // sqrt for uniform distribution in circle
            double x = centerX + r * Math.cos(angle);
            double z = centerZ + r * Math.sin(angle);

            // clamp/convert to ints for height lookup
            int xi = Mth.floor(x);
            int zi = Mth.floor(z);

            // find highest non-air surface (safest ground) using heightmap
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xi, zi);

            // place one block above surface
            double finalY = (double) y + 1.0;

            // Validate bounds
            BlockPos blockPos = BlockPos.containing(x, finalY, z);
            if (!Level.isInSpawnableBounds(blockPos)) {
                // If invalid, try a few additional attempts at random (so command is useful)
                boolean succeeded = false;
                for (int i = 0; i < 5; i++) {
                    angle = random.nextDouble() * Math.PI * 2.0;
                    r = Math.sqrt(random.nextDouble()) * radius;
                    x = centerX + r * Math.cos(angle);
                    z = centerZ + r * Math.sin(angle);
                    xi = Mth.floor(x);
                    zi = Mth.floor(z);
                    y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, xi, zi);
                    finalY = (double) y + 1.0;
                    blockPos = BlockPos.containing(x, finalY, z);
                    if (Level.isInSpawnableBounds(blockPos)) {
                        succeeded = true;
                        break;
                    }
                }
                if (!succeeded) {
                    throw INVALID_POSITION.create();
                }
            }

            float f = Mth.wrapDegrees(entity.getYRot());
            float g = Mth.wrapDegrees(entity.getXRot());
            if (entity.teleportTo(level, x, finalY, z, set, f, g)) {

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
                teleported++;
            }
        }

        // feedback
        if (teleported == 1 && !targets.isEmpty()) {
            Entity single = targets.iterator().next();
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.location.single",
                    single.getDisplayName(),
                    formatDouble(single.getX()),
                    formatDouble(single.getY()),
                    formatDouble(single.getZ())), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.location.multiple",
                    teleported,
                    teleported > 0 ? formatDouble(((Entity) targets.iterator().next()).getX()) : "0",
                    teleported > 0 ? formatDouble(((Entity) targets.iterator().next()).getY()) : "0",
                    teleported > 0 ? formatDouble(((Entity) targets.iterator().next()).getZ()) : "0"), true);
        }

        if (targets.size() == 1) {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.location.single", targets.iterator().next().getDisplayName(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)), true);
        } else {
            source.sendSuccess(() -> Component.translatable("commands.teleport.success.location.multiple", targets.size(), formatDouble(vec3.x), formatDouble(vec3.y), formatDouble(vec3.z)), true);
        }

        return teleported;
    }

    private static String formatDouble(double d) {
        return String.format(Locale.ROOT, "%f", d);
    }

    private static final SimpleCommandExceptionType INVALID_POSITION = new SimpleCommandExceptionType(Component.translatable("commands.teleport.invalidPosition"));
}
