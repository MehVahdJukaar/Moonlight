package net.mehvahdjukaar.moonlight.client.texture_renderer;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class DummyCamera extends Camera {


    @Override
    public void setPosition(double pX, double pY, double pZ) {
        super.setPosition(pX, pY, pZ);
    }

    @Override
    public void setPosition(Vec3 pPos) {
        super.setPosition(pPos);
    }
    public void setPosition(BlockPos pPos) {
        super.setPosition(Vec3.atCenterOf(pPos));
    }
}
