package net.mehvahdjukaar.moonlight.api.integration;

import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.function.Consumer;

// Somewhat bare bones, based on my best guess of functions that compat will need
public class ValkyrienSkiesCompat {

    /**
     * Makes sure a position is always in the world.
     * If the position is already in the world, nothing happens.
     * If the position is on a ship, it will get transformed out into the world.
     */
    public static Vec3 toWorldCoordinates(Level level, Vec3 pos) {
        return VSGameUtilsKt.toWorldCoordinates(level, pos);
    }

    public static boolean isOnShip(Level level, Vec3 pos) {
        return VSGameUtilsKt.getShipManagingPos(level, pos) != null;
    }

    /**
     * If there is a ship at the position, the direction will be
     * transformed to world
     * @param level The level to look for the ship in
     * @param position The position to look for the ship at
     * @param direction The direction to transform
     * @return The transformed or unchanged direction vector
     */
    public static Vec3 transformDirectionToWorld(Level level, Vec3 position, Vec3 direction) {
        Ship ship = VSGameUtilsKt.getShipManagingPos(level, position);
        if (ship == null) return direction;

        Vector3d directionJoml = VectorConversionsMCKt.toJOML(direction);
        directionJoml = ship.getTransform().getShipToWorld().transformDirection(directionJoml);
        return VectorConversionsMCKt.toMinecraft(directionJoml);
    }

    /**
     * Calculates the distanceToCenterSqrt from the posA to posB,
     * but if one or both positions are on ships, they will be transformed
     * to the world for the distance calculation.
     */
    public static double distanceToCenterSqrInWorld(Level level, Vec3i posA, Position posB) {
        return ValkyrienSkies.distanceToCenterSquared(level, posA, posB);
    }

}
