package net.mehvahdjukaar.moonlight.core.fake_player;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FakeGenericPlayer extends Player {

    // Map of all active fake player usernames to their entities.
    // A fake player holds its level, which is this map's key, so weak keys alone would never let an entry go:
    // the value resurrects it. Hence weak values too, which breaks the loop and makes the whole thing
    // self healing even if nobody calls unloadLevel.
    private static final Map<Level, Map<GameProfile, FakeGenericPlayer>> FAKE_PLAYERS =
            new MapMaker().weakKeys().makeMap();

    /**
     * Get a fake player with a given username. The returned player is only cached for as long as the caller
     * keeps a reference to it, so holding onto it also keeps its level in memory: don't store it in a static.
     */
    public static FakeGenericPlayer get(Level level, GameProfile username) {
        return FAKE_PLAYERS.computeIfAbsent(level, l -> new MapMaker().weakValues().makeMap())
                .computeIfAbsent(username, u -> new FakeGenericPlayer(level, username));
    }

    public static void unloadLevel(LevelAccessor level) {
        FAKE_PLAYERS.keySet().removeIf(l -> l == level);
    }

    public FakeGenericPlayer(Level level, GameProfile gameProfile) {
        super(level, BlockPos.ZERO, 0, gameProfile);
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }

    @Override
    public void displayClientMessage(Component chatComponent, boolean actionBar) {
    }

    @Override
    public void awardStat(Stat stat, int increment) {
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return true;
    }

    @Override
    public boolean canHarmPlayer(Player other) {
        return false;
    }

    @Override
    public void die(DamageSource damageSource) {
    }

    @Override
    public void tick() {
    }

    public @Nullable MinecraftServer getServer() {
        return PlatHelper.getCurrentServer();
    }
}
