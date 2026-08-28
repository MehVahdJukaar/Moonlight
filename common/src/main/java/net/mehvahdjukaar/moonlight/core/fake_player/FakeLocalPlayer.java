package net.mehvahdjukaar.moonlight.core.fake_player;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class FakeLocalPlayer extends AbstractClientPlayer {

    // weak values too, or the cached player keeps its level key alive
    private static final Map<ClientLevel, Map<GameProfile, FakeLocalPlayer>> FAKE_PLAYERS =
            new MapMaker().weakKeys().makeMap();

    /**
     * Only cached while the caller holds it. Don't store it in a static, it keeps its level alive.
     */
    static FakeLocalPlayer get(ClientLevel level, GameProfile username) {
        return FAKE_PLAYERS.computeIfAbsent(level, l -> new MapMaker().weakValues().makeMap())
                .computeIfAbsent(username, u -> new FakeLocalPlayer(level, username));
    }

    static void unloadLevel(LevelAccessor level) {
        FAKE_PLAYERS.keySet().removeIf(l -> l == level);
    }

    private final EntityDimensions dimensions = EntityDimensions.fixed(0, 0);

    public FakeLocalPlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile);
        this.noPhysics = true;
    }

    @Override
    public void playSound(SoundEvent pSound, float pVolume, float pPitch) {
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return dimensions;
    }

    @Override
    public void tick() {
    }

    @Override
    public Vec3 position() {
        return new Vec3(this.getX(), this.getY(), this.getZ());
    }

    @Override
    public BlockPos blockPosition() {
        return new BlockPos((int) this.getX(), (int) this.getY(), (int) this.getZ());
    }

    @Override
    public void setXRot(float pXRot) {
        super.setXRot(pXRot);
        this.xRotO = pXRot;
    }

    @Override
    public void setYRot(float pYRot) {
        super.setYRot(pYRot);
        this.yRotO = pYRot;
    }
}
