package net.mehvahdjukaar.moonlight.api.util.fake_player;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class FakeLocalPlayer extends AbstractClientPlayer {

    // Map of all active fake player usernames to their entities
    // automatically gets cleaned when level is unloaded as key won't be in use anymore
    private static final WeakHashMap<ClientLevel, Map<GameProfile, FakeLocalPlayer>> FAKE_PLAYERS = new WeakHashMap<>();

    /**
     * Get a fake player with a given username,
     * Mods should either hold weak references to the return value, or listen for a
     * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
     */
    static FakeLocalPlayer get(Level level, GameProfile username) {
        return FAKE_PLAYERS.computeIfAbsent((ClientLevel) level, l -> new HashMap<>())
                .computeIfAbsent(username, u -> new FakeLocalPlayer((ClientLevel) level, username));
    }

    public FakeLocalPlayer(ClientLevel pClientLevel, GameProfile pGameProfile) {
        super(pClientLevel, pGameProfile, null);
        this.noPhysics = true;
    }

    @Override
    public void playSound(SoundEvent pSound, float pVolume, float pPitch) {
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
