package net.mehvahdjukaar.moonlight.core.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.mehvahdjukaar.moonlight.api.misc.CircularList;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;

public class BackCommand {

    private static final int MAX_HISTORY = 10;
    private static final WeakHashMap<ServerPlayer, CircularList<GlobalPos>> HISTORY = new WeakHashMap<>();

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandBuildContext context) {
        return Commands.literal("back")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(BackCommand::teleportBack);
    }

    // Called when a player is teleported elsewhere, to save their previous location
    public static void onTeleported(Entity entity, BlockPos oldPos, ResourceKey<Level> oldDim) {
        if (!(entity instanceof ServerPlayer player)) return;
        record(player, GlobalPos.of(oldDim, oldPos));
    }

    // Dying or leaving the End makes a whole new player object, so the history has to be moved onto it. Where they
    // died goes in as well, so /back brings you back to your corpse
    public static void onPlayerCloned(Player oldPlayer, Player newPlayer) {
        if (!(oldPlayer instanceof ServerPlayer old) || !(newPlayer instanceof ServerPlayer fresh)) return;

        CircularList<GlobalPos> history = HISTORY.remove(old);
        if (history != null) HISTORY.put(fresh, history);
        record(fresh, GlobalPos.of(old.level().dimension(), old.blockPosition()));
    }

    private static void record(ServerPlayer player, GlobalPos pos) {
        CircularList<GlobalPos> list = HISTORY.computeIfAbsent(player, p -> new CircularList<>(MAX_HISTORY));
        // a cross dimension teleport goes through more than one hook, so the same spot can come in twice
        if (list.isEmpty() || !list.getLast().equals(pos)) {
            list.addLast(pos); // plain add() skips the size cap
        }
    }

    private static int teleportBack(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        Entity entity = source.getEntity();
        if (!(entity instanceof ServerPlayer player)) {
            source.sendFailure(Component.translatable("commands.moonlight.back.only_players"));
            return 0;
        }

        CircularList<GlobalPos> list = HISTORY.get(player);
        if (list == null || list.isEmpty()) {
            source.sendFailure(Component.translatable("commands.moonlight.back.empty"));
            return 0;
        }

        // Get last known position (and remove it from history so multiple /back calls step through history)
        GlobalPos last = list.removeLast();
        ServerLevel targetLevel = source.getServer().getLevel(last.dimension());
        if (targetLevel == null) {
            source.sendFailure(Component.translatable("commands.moonlight.back.invalid_dimension"));
            return 0;
        }

        BlockPos pos = last.pos();
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;

        Set<Relative> set = EnumSet.of(Relative.X_ROT, Relative.Y_ROT);
        performTeleport(source, player, targetLevel, x, y, z, set);

        source.sendSuccess(() ->
                Component.translatable("commands.teleport.success.location.single",
                        player.getDisplayName(),
                        formatDouble(x),
                        formatDouble(y),
                        formatDouble(z)), true);

        return 1;
    }

    private static void performTeleport(CommandSourceStack source, Entity entity, ServerLevel level,
                                        double x, double y, double z, Set<Relative> relativeList)
            throws CommandSyntaxException {
        BlockPos blockPos = BlockPos.containing(x, y, z);
        if (!Level.isInSpawnableBounds(blockPos)) {
            throw new CommandSyntaxException(null, Component.translatable("commands.teleport.invalidPosition"));
        } else {
            float f = Mth.wrapDegrees(entity.getYRot());
            float g = Mth.wrapDegrees(entity.getXRot());
            if (entity.teleportTo(level, x, y, z, relativeList, f, g, true)) {
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
