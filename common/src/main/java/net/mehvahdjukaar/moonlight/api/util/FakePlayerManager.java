package net.mehvahdjukaar.moonlight.api.util;

import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.fake_player.FPClientAccess;
import net.mehvahdjukaar.moonlight.core.fake_player.FakeGenericPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class FakePlayerManager {

    private static final GameProfile DEFAULT = new GameProfile(UUID.fromString(
            "61e22C44-14d5-1f22-ed27-13D2C95CA355"),
            "[ML_Fake_Player]");

    public static Player get(GameProfile id, Entity entity) {
        return get(id, entity.level());
    }

    public static Player get(GameProfile id, Level level) {
        Player fakePlayer;
        if (level instanceof ServerLevel sl) {
            fakePlayer = PlatHelper.getFakeServerPlayer(id, sl);
        }
        else if (PlatHelper.getPhysicalSide().isServer()) { //on server side but level not serve sided or not instance of server level
            fakePlayer = FakeGenericPlayer.get(level, id);
        } else {
            //class loading hacks
            fakePlayer = FPClientAccess.get(level, id);
        }
        return fakePlayer;
    }

    public static Player get(GameProfile id, Entity copyPosFrom, Entity copyRotFrom) {
        Player p = get(id, copyPosFrom.level());
        p.setPos(copyPosFrom.getX(), copyPosFrom.getY(), copyPosFrom.getZ());
        p.setYHeadRot(copyRotFrom.getYHeadRot());
        p.setXRot(copyRotFrom.getXRot());
        p.setYRot(copyRotFrom.getYRot());
        p.setOldPosAndRot();
        return p;
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
}
