package net.mehvahdjukaar.moonlight.api.integration;

import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.Pose3dc;
import net.mehvahdjukaar.moonlight.api.util.math.MthUtils;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SableCompat {

    public static BlockHitResult sweepIncludingSubLevels(Entity entity, Vec3 movement, double maxStep,
                                                         BlockHitResult ownSpaceHit) {
        double length = movement.length();
        if (length < 1.0E-7) return ownSpaceHit;

        SableCompanion sable = SableCompanion.INSTANCE;
        Level level = entity.level();
        Vec3 start = entity.position();
        Vec3 end = start.add(movement);

        SubLevelAccess origin = sable.getContaining(entity);
        Pose3dc originPose = origin == null ? null : origin.logicalPose();
        Vec3 worldStart = originPose == null ? start : originPose.transformPosition(start);
        Vec3 worldEnd = originPose == null ? end : originPose.transformPosition(end);

        AABB box = entity.getBoundingBox();
        double margin = Math.max(box.getXsize(), Math.max(box.getYsize(), box.getZsize()));
        BoundingBox3d sweptArea = new BoundingBox3d(worldStart, (Position) worldEnd).expand(margin);

        List<Pose3dc> frames = new ArrayList<>();
        if (originPose != null) frames.add(null);
        for (SubLevelAccess subLevel : sable.getAllIntersecting(level, sweptArea)) {
            if (!isSameSubLevel(subLevel, origin)) frames.add(subLevel.logicalPose());
        }

        BlockHitResult best = ownSpaceHit;
        double bestFraction = hitFraction(start, length, ownSpaceHit);

        for (Pose3dc pose : frames) {
            Vec3 frameStart = pose == null ? worldStart : pose.transformPositionInverse(worldStart);
            Vec3 frameEnd = pose == null ? worldEnd : pose.transformPositionInverse(worldEnd);
            Vec3 frameMovement = frameEnd.subtract(frameStart);
            AABB frameBox = box.move(frameStart.subtract(start));

            BlockHitResult hit = MthUtils.collideWithSweptAABB(frameStart, frameBox, frameMovement, level, maxStep);
            if (hit.getType() == HitResult.Type.MISS) continue;

            double fraction = hitFraction(frameStart, frameMovement.length(), hit);
            if (fraction < bestFraction) {
                bestFraction = fraction;
                best = new BlockHitResult(start.add(movement.scale(fraction)), hit.getDirection(),
                        hit.getBlockPos(), hit.isInside());
            }
        }
        return best;
    }

    private static double hitFraction(Vec3 start, double length, BlockHitResult hit) {
        if (hit.getType() == HitResult.Type.MISS || length < 1.0E-7) return 1;
        return Math.min(1, hit.getLocation().distanceTo(start) / length);
    }

    private static boolean isSameSubLevel(SubLevelAccess a, @Nullable SubLevelAccess b) {
        return b != null && (a == b || a.getUniqueId().equals(b.getUniqueId()));
    }
}
