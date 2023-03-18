package net.mehvahdjukaar.moonlight.core.fake_player;

import com.mojang.authlib.GameProfile;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

public class FPClientAccess {
    
    @Environment(EnvType.CLIENT)
    public static Player get(Level level, GameProfile id) {
        return FakeLocalPlayer.get(level, id);
    }

    @Environment(EnvType.CLIENT)
    public static void unloadLevel(LevelAccessor level) {
        FakeLocalPlayer.unloadLevel(level);
    }
}
