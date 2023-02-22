package net.mehvahdjukaar.moonlight.api.entity;

public interface IControllableVehicle {

    void onInputUpdate(boolean left, boolean right, boolean up, boolean down, boolean sprint, boolean jumping);
}
