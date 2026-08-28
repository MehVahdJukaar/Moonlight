package net.mehvahdjukaar.moonlight.api.integration;

public class SableVec3     {

    /*
    private final Position position;

    public SableVec3(Position pos) {
        this.position = pos;
    }

    public Vec3 position(Level level) {
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, position);
    }

    public Quaterniondc orientation(Level level) {
        if (level.isClientSide()) {
            var sub = SableCompanion.INSTANCE.getContainingClient(this.position);
            if (sub != null) {
                return sub.renderPose().orientation();
            }
        } else {
            var sub = SableCompanion.INSTANCE.getContaining(level, this.position);
            if (sub != null) {
                return sub.logicalPose().orientation();
            }
        }
        return new Quaterniond();
    }



    public Matrix4d matrix(Level level) {
        if (level.isClientSide()) {
            var sub = SableCompanion.INSTANCE.getContainingClient(this.position);
            if (sub != null) {
                return sub.renderPose().bakeIntoMatrix(new Matrix4d());
            }
        } else {
            var sub = SableCompanion.INSTANCE.getContaining(level, this.position);
            if (sub != null) {
                return sub.logicalPose().bakeIntoMatrix(new Matrix4d());
            }
        }
        return new Matrix4d();

    }*/
}
