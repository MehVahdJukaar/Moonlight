package net.mehvahdjukaar.moonlight.api.util.fake_player;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.ApiStatus;

import java.util.UUID;
import java.util.function.Supplier;

public class FakePlayerManager {

    private static final GameProfile DEFAULT = new GameProfile(UUID.fromString(
            "61e22C44-14d5-1f22-ed27-13D2C95CA355"),
            "[ML_Fake_Player]");

    public static Player get(GameProfile id, Entity entity) {
        return get(id, entity.level);
    }

    public static Player get(GameProfile id, Level level) {
        Player fakePlayer;
        try {
            if (level instanceof ServerLevel sl) {
                fakePlayer = FakeServerPlayer.get(sl, id);
            } else {
                //class loading hacks
                fakePlayer = ClientAccess.get(level, id);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Level must be either ServerLevel or ClientLevel", e);
        }
        return fakePlayer;
    }


    public static Player get(GameProfile id, Entity copyPosFrom, Entity copyRotFrom) {
        Player p = get(id, copyPosFrom.level);
        p.setPos(copyPosFrom.getX(), copyPosFrom.getY(), copyPosFrom.getZ());
        p.setYHeadRot(copyRotFrom.getYHeadRot());
        p.setXRot(copyRotFrom.getXRot());
        p.setYRot(copyRotFrom.getYRot());
        p.setOldPosAndRot();
        return p;
    }

    @Deprecated(forRemoval = true)
    public static Player get(Entity copyPosFrom, Entity copyRotFrom) {
        return getDefault(copyPosFrom, copyRotFrom);
    }

    public static Player getDefault(Entity copyPosFrom, Entity copyRotFrom) {
        return get(DEFAULT, copyPosFrom, copyRotFrom);
    }

    public static Player getDefault(Level level) {
        return get(DEFAULT, level);
    }

    public static Player getDefault(Entity entity) {
        return get(DEFAULT, entity);
    }

    @ApiStatus.Internal
    public static void unloadLevel(LevelAccessor level) {
        try {
            if (level instanceof ServerLevel sl) {
                FakeServerPlayer.unloadLevel(sl);
            } else if (level.isClientSide()) {
                //got to be careful with classloading
                ClientAccess.unloadLevel(level);
            }
        } catch (Exception e) {
            //  Moonlight new IllegalArgumentException("Level must be either ServerLevel or ClientLevel", e);
        }
    }


}
