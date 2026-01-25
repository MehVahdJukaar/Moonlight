package net.mehvahdjukaar.moonlight.core.mixins;

import net.mehvahdjukaar.moonlight.core.commands.DebugRenderersCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.common.custom.*;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.breeze.Breeze;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.Target;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(DebugPackets.class)
public abstract class DebugPacketsMixin {

    @Shadow
    private static void sendPacketToAllPlayers(ServerLevel level, CustomPacketPayload payload) {
    }

    @Inject(method = "sendPathFindingPacket", at = @At("HEAD"))
    private static void ml$sendPathfindingDebug(Level level, Mob mob, @Nullable Path path, float maxDistanceToWaypoint, CallbackInfo ci) {
        if (path != null && level instanceof ServerLevel sl) {
            if (!DebugRenderersCommand.DEBUG_PATHFINDING.isActive(mob.getType(), sl)) return;

            Path.DebugData debugData = path.debugData();
            if (debugData == null) {
                List<Node> close = new ArrayList<>();
                List<Node> open = new ArrayList<>();
                Set<Target> targets = new HashSet<>();
                for (int i = 0; i < path.getNodeCount(); i++) {
                    Node node = path.getNode(i);
                    if (node.closed) {
                        close.add(node);
                    } else open.add(node);

                }
                BlockPos pos = path.getTarget();

                Target t = mob.getNavigation().getNodeEvaluator().getTarget(pos.getX(), pos.getY(), pos.getZ());
                targets.add(t);
                path.setDebug(open.toArray(new Node[0]), close.toArray(new Node[0]), targets);
                debugData = path.debugData();
            }
            if (debugData != null && !debugData.targetNodes().isEmpty()) {
                sendPacketToAllPlayers(sl, new PathfindingDebugPayload(mob.getId(), path, maxDistanceToWaypoint));
            }
        }
    }

    @Inject(method = "sendNeighborsUpdatePacket", at = @At("HEAD"))
    private static void ml$neighborUpdatesDebug(Level level, BlockPos pos, CallbackInfo ci) {
        if (DebugRenderersCommand.DEBUG_NEIGHBOR_UPDATES && level instanceof ServerLevel sl) {
            sendPacketToAllPlayers(sl, new NeighborUpdatesDebugPayload(level.getGameTime(), pos));
        }
    }

    @Inject(method = "sendGameEventInfo", at = @At("HEAD"))
    private static void ml$gameEvent(Level level, Holder<GameEvent> gameEvent, Vec3 pos, CallbackInfo ci) {
        if (DebugRenderersCommand.DEBUG_GAME_EVENTS && level instanceof ServerLevel sl) {
            sendPacketToAllPlayers(sl, new GameEventDebugPayload(gameEvent.unwrapKey().get(),
                    pos));
        }
    }

    @Inject(method = "sendGameEventListenerInfo", at = @At("HEAD"))
    private static void ml$gameEventListener(Level level, GameEventListener gameEventListener, CallbackInfo ci) {
        if (DebugRenderersCommand.DEBUG_GAME_EVENTS && level instanceof ServerLevel sl) {
            sendPacketToAllPlayers(sl, new GameEventListenerDebugPayload(gameEventListener.getListenerSource(),
                    gameEventListener.getListenerRadius()));
        }
    }

    @Inject(method = "sendBreezeInfo", at = @At("HEAD"))
    private static void ml$breeze(Breeze breeze, CallbackInfo ci) {
        if (DebugRenderersCommand.DEBUG_BREEZE && breeze.level() instanceof ServerLevel sl) {
            LivingEntity target = breeze.getTarget();
            sendPacketToAllPlayers(sl, new BreezeDebugPayload(new BreezeDebugPayload.BreezeInfo(
                    breeze.getUUID(), breeze.getId(), target == null ? null : target.getId(),
                    null //idk
            )));
        }
    }

    @Inject(method = "sendGoalSelector", at = @At("HEAD"))
    private static void ml$goalDebug(Level level, Mob mob, GoalSelector goalSelector, CallbackInfo ci) {
        if (level instanceof ServerLevel sl) {
            if (!DebugRenderersCommand.DEBUG_GOAL_SELECTOR.isActive(mob.getType(), sl)) return;

            List<GoalDebugPayload.DebugGoal> goals = new ArrayList<>();
            for (var g : goalSelector.getAvailableGoals()) {
                goals.add(new GoalDebugPayload.DebugGoal(g.getPriority(), g.isRunning(), g.getGoal().toString()));
            }
            sendPacketToAllPlayers(sl, new GoalDebugPayload(mob.getId(), mob.blockPosition(), goals));
        }
    }

    @Inject(method = "sendStructurePacket", at = @At("HEAD"))
    private static void ml$StructureDebug(WorldGenLevel level, StructureStart structureStart, CallbackInfo ci) {
        ServerLevel sl = level.getLevel();
        if (!DebugRenderersCommand.DEBUG_STRUCTURES_BB.isActive(structureStart.getStructure(), sl)) return;

        List<StructuresDebugPayload.PieceInfo> infos = new ArrayList<>();
        for (var s : structureStart.getPieces()) {
            infos.add(new StructuresDebugPayload.PieceInfo(s.getBoundingBox(), s.getGenDepth() <= 0));
        }
        sendPacketToAllPlayers(sl, new StructuresDebugPayload(sl.dimension(),
                structureStart.getBoundingBox(), infos));
    }

}
