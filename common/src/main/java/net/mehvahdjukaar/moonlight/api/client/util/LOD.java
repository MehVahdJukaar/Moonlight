package net.mehvahdjukaar.moonlight.api.client.util;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class LOD {

    public static final int BUFFER = sq(2 * 2);
    public static final int VERY_NEAR_DIST = sq(16);
    public static final int NEAR_DIST = sq(32);
    public static final int NEAR_MED_DIST = sq(48);
    public static final int MEDIUM_DIST = sq(64);
    public static final int FAR_DIST = sq(96);

    private final Vec3 cameraPosition;
    private final Vec3 cameraDirection;
    private final Vec3 objCenter;
    private final double distSq; // computed once

    public static final LOD MAX = new LOD(0.0);
    private static final float DEFAULT_RADIUS = 1.45f;

    public static LOD at(BlockEntity be) {
        return LOD.at(be.getBlockPos());
    }

    public static LOD at(BlockPos objPos) {
        Minecraft mc = Minecraft.getInstance();
        return LOD.at(mc.gameRenderer.getMainCamera(), objPos.getCenter());
    }

    public static LOD at(Camera camera, BlockPos objPos) {
        return LOD.at(camera, objPos.getCenter());
    }

    public static LOD at(Camera camera, Vec3 objCenter) {
        return new LOD(camera, objCenter);
    }

    private LOD(Camera camera, Vec3 objCenter) {
        this.cameraPosition = camera.position();
        this.cameraDirection = new Vec3(camera.forwardVector()).normalize();
        this.objCenter = objCenter;
        this.distSq = isScoping() ? 1 : cameraPosition.distanceToSqr(objCenter);
    }

    // Private ctor for MAX and legacy paths that only care about distance
    private LOD(double distSq) {
        this.cameraPosition = Vec3.ZERO;
        this.cameraDirection = Vec3.ZERO;
        this.objCenter = Vec3.ZERO;
        this.distSq = distSq;
    }

    public static boolean isScoping() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        return p != null && mc.options.getCameraType().isFirstPerson() && p.isScoping();
    }

    public boolean isVeryNear() {
        return distSq <= VERY_NEAR_DIST;
    }

    public boolean isNear() {
        return distSq <= NEAR_DIST;
    }

    public boolean isNearMed() {
        return distSq <= NEAR_MED_DIST;
    }

    public boolean isMedium() {
        return distSq <= MEDIUM_DIST;
    }

    public boolean isFar() {
        return distSq <= FAR_DIST;
    }

    /**
     * Generic max range check (if you want custom per-object limits).
     */
    public boolean within(double maxDist) {
        return distSq <= (maxDist * maxDist);
    }

    /**
     * Returns true if the plane should be culled (i.e., not rendered).
     */
    public boolean isPlaneCulled(Vec3 normalVec) {
        return isPlaneCulled(normalVec, null, 0.0f);
    }

    public boolean isPlaneCulled(Vec3 normalVec, Vec3 offset) {
        return isPlaneCulled(normalVec, offset, 0.0f);
    }


    public boolean isPlaneCulled(Direction facing, float offset, float radius, float cosTolerance) {
        Vector3f normal = facing.step();
        return this.isPlaneCulled(new Vec3(normal), offset, radius, cosTolerance);
    }

    public boolean isPlaneCulled(Direction facing, float offset, float cosTolerance) {
        return isPlaneCulled(facing, offset, DEFAULT_RADIUS, cosTolerance);
    }

    public boolean isPlaneCulled(Vec3 planeNormal, float offset, float cosTolerance) {
        return isPlaneCulled(planeNormal, offset, DEFAULT_RADIUS, cosTolerance);
    }

    public boolean isPlaneCulled(Vec3 planeNormal, float offset, float radius, float cosTolerance) {
        return isPlaneCulled(planeNormal, planeNormal.scale(offset), radius, cosTolerance);
    }

    public boolean isPlaneCulled(Direction facing, float offset) {
        return isPlaneCulled(facing, offset, 0.0f);
    }

    public boolean isPlaneCulled(Vec3 planeNormal, @Nullable Vec3 offset, float cosTolerance) {
        //assumes a plane of length 1
        return isPlaneCulled(planeNormal, offset, DEFAULT_RADIUS, cosTolerance);
    }

    /**
     * @param planeNormal  unit-length normal (outward from the visible face)
     * @param offset       optional offset from object center (null for none)
     * @param cosTolerance require normal.toCam > cosTolerance (0 = any front-facing)
     * @param discRadius  radius of the plane (for disk-aware culling; 0 = point)
     * @return true if culled (behind camera or backfacing beyond tolerance)
     */
    public boolean isPlaneCulled(
            Vec3 planeNormal,
            @Nullable Vec3 offset,
            float discRadius,
            float cosTolerance
    ) {
        Vec3 planePoint = (offset == null) ? this.objCenter : this.objCenter.add(offset);
        Vec3 camToPlane = planePoint.subtract(this.cameraPosition);
        double forwardDist = camToPlane.dot(this.cameraDirection);

        if (forwardDist <= -discRadius) {
            return true;
        }

        Vec3 toCam = this.cameraPosition.subtract(planePoint);
        double len2 = toCam.lengthSqr();

        if (len2 <= 1e-12) {
            return false;
        }

        // Normalize vector without sqrt if only dot product is needed
        double cosFacing = planeNormal.dot(toCam) / Math.sqrt(len2);
        if (cosFacing <= cosTolerance) {
            return true;
        }

        // Avoid sqrt for angular disk test using squared values
        double discRadius2 = discRadius * discRadius;
        if (discRadius2 >= len2) {
            return false;
        }

        double cosTheta = Math.sqrt(1.0 - discRadius2 / len2);
        return cosFacing <= cosTolerance - cosTheta;
    }

    public static int sq(int v) {
        return v * v;
    }
}
