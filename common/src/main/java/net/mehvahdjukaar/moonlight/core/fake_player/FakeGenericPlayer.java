package net.mehvahdjukaar.moonlight.core.fake_player;

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

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

public class FakeGenericPlayer extends Player {

    private static final boolean HAS_CACHE = PlatHelper.getPlatform().isForge(); //fabric doesnt have world unload event

    // Map of all active fake player usernames to their entities
    // automatically gets cleaned when level is unloaded as key won't be in use anymore
    private static final WeakHashMap<Level, Map<GameProfile, FakeGenericPlayer>> FAKE_PLAYERS = new WeakHashMap<>();

    /**
     * Get a fake player with a given username,
     * Mods should either hold weak references to the return value, or listen for a
     * WorldEvent.Unload and kill all references to prevent worlds staying in memory.
     */
    public static FakeGenericPlayer get(Level level, GameProfile username) {
        if (!HAS_CACHE) return new FakeGenericPlayer(level, username);
        return FAKE_PLAYERS.computeIfAbsent(level, l -> new HashMap<>())
                .computeIfAbsent(username, u -> new FakeGenericPlayer(level, username));
    }

    public static void unloadLevel(LevelAccessor level) {
        FAKE_PLAYERS.entrySet().removeIf(e -> e.getKey() == level);
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
