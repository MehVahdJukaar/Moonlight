package net.mehvahdjukaar.moonlight.api.entity;

/**
 * Implement this into either a vehicle entity to receive input updates
 * Only called on client side of course
 */
public interface IControllableVehicle {

    // you should probably send a packet to the server here
    void onInputUpdate(boolean left, boolean right, boolean up, boolean down, boolean sprint, boolean jumping);
}
