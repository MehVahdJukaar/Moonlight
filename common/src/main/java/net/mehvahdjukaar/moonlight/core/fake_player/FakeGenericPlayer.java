package net.mehvahdjukaar.moonlight.core.fake_player;

import com.google.common.collect.MapMaker;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;

import java.util.Map;

public class FakeGenericPlayer extends Player {

    // Map of all active fake player usernames to their entities.
    // weak values too: the player holds its level (the key), so weak keys alone would never let an entry go
    private static final Map<Level, Map<GameProfile, FakeGenericPlayer>> FAKE_PLAYERS =
            new MapMaker().weakKeys().makeMap();

    /**
     * Get a fake player with a given username. Don't store it in a static, it keeps its level alive.
     */
    public static FakeGenericPlayer get(Level level, GameProfile username) {
        return FAKE_PLAYERS.computeIfAbsent(level, l -> new MapMaker().weakValues().makeMap())
                .computeIfAbsent(username, u -> new FakeGenericPlayer(level, username));
    }

    public static void unloadLevel(LevelAccessor level) {
        FAKE_PLAYERS.keySet().removeIf(l -> l == level);
    }

    public FakeGenericPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public GameType gameMode() {
        return GameType.SURVIVAL;
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
    public void sendSystemMessage(Component message) {
    }

    @Override
    public void sendOverlayMessage(Component message) {
    }

    @Override
    public void awardStat(Stat stat, int increment) {
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
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
}
