package net.mehvahdjukaar.moonlight.api.client.util;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
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

    // ---- New class state ----
    private final Vec3 cameraPosition;
    private final Vec3 cameraDirection;
    private final Vec3 objCenter;
    private final double distSq; // computed once

    public static final LOD MAX = new LOD(0.0);
    private static float DEFAULT_RADIUS = 1.45f;

    // ---- New factories ----
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
        this.cameraPosition = camera.getPosition();
        this.cameraDirection = new Vec3(camera.getLookVector()).normalize();
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

    // ---- BACKWARD-COMP: public isScoping as before ----
    public static boolean isScoping() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        return p != null && mc.options.getCameraType().isFirstPerson() && p.isScoping();
    }

    // ---- BACKWARD-COMP: old constructors ----

    /**
     * Old: new LOD(Camera, BlockPos)
     */
    @Deprecated(forRemoval = true)
    public LOD(Camera camera, BlockPos pos) {
        this.cameraPosition = camera.getPosition();
        this.cameraDirection = new Vec3(camera.getLookVector()).normalize();
        this.objCenter = pos.getCenter();
        this.distSq = isScoping() ? 1 : this.cameraPosition.distanceToSqr(this.objCenter);
    }

    /**
     * Old: new LOD(Vec3 cameraPos, BlockPos pos)
     */
    @Deprecated(forRemoval = true)
    public LOD(Vec3 cameraPos, BlockPos pos) {
        this.cameraPosition = cameraPos;
        this.cameraDirection = Vec3.ZERO; // unknown without a Camera; unused by distance checks
        this.objCenter = pos.getCenter();
        this.distSq = isScoping() ? 1 : this.cameraPosition.distanceToSqr(this.objCenter);
    }

    // ---- distance helpers (use precomputed distSq) ----
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

    // ---- plane facing / culling ----

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
     * @param cosTolerance require normal·toCam > cosTolerance (0 = any front-facing)
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

    // ---- BACKWARD-COMP: old angle/focus helpers ----

    /**
     * Old API: quick helper
     */
    @Deprecated(forRemoval = true)
    public static boolean isOutOfFocus(Vec3 cameraPos, BlockPos pos, float blockYaw) {
        return isOutOfFocus(cameraPos, pos, blockYaw, 0, Direction.UP, 0);
    }

    /**
     * Old API: full helper using direction and offset
     */
    @Deprecated(forRemoval = true)
    public static boolean isOutOfFocus(Vec3 cameraPos, BlockPos pos, float blockYaw,
                                       float degMargin, Direction dir, float offset) {
        float relAngle = getRelativeAngle(cameraPos, pos, dir, offset);
        return isOutOfFocus(relAngle, blockYaw, degMargin);
    }

    /**
     * Old API: angle-only overload
     */
    @Deprecated(forRemoval = true)
    public static boolean isOutOfFocus(float relativeAngle, float blockYaw, float degMargin) {
        // Preserve old logic (note: this matched the legacy semantics)
        return (net.minecraft.util.Mth.degreesDifference(relativeAngle, blockYaw - 90) > -degMargin);
    }

    /**
     * Old API: relative angle camera->block center
     */
    @Deprecated(forRemoval = true)
    public static float getRelativeAngle(Vec3 cameraPos, BlockPos pos) {
        return getRelativeAngle(cameraPos, pos, Direction.UP, 0);
    }

    /**
     * Old API: relative angle with offset along a face direction
     */
    @Deprecated(forRemoval = true)
    public static float getRelativeAngle(Vec3 cameraPos, BlockPos pos, Direction dir, float offset) {
        // Matches legacy implementation
        return (float) (net.minecraft.util.Mth.atan2(
                offset * dir.getStepX() + cameraPos.x - (pos.getX() + 0.5f),
                offset * dir.getStepZ() + cameraPos.z - (pos.getZ() + 0.5f)) * 180 / Math.PI);
    }

    // ---- small util ----
    public static int sq(int v) {
        return v * v;
    }
}
