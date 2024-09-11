package net.mehvahdjukaar.moonlight.core.fake_player;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class FPClientAccess {
    
    @Environment(EnvType.CLIENT)
    public static Player get(Level level, GameProfile id) {
        if(level instanceof ClientLevel cl){
            return FakeLocalPlayer.get(cl, id);
        }else {
            return FakeGenericPlayer.get(level, id);
        }
    }

    @Environment(EnvType.CLIENT)
    public static void unloadLevel(LevelAccessor level) {
        FakeLocalPlayer.unloadLevel(level);
    }
}
