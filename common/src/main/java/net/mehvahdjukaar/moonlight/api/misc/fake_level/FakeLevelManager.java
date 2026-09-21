package net.mehvahdjukaar.moonlight.api.misc.fake_level;

import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

public class FakeLevelManager {

    private static final Map<String, FakeLevel> CLIENT_INSTANCES = new ConcurrentHashMap<>();
    private static final Map<String, FakeServerLevel> SERVER_INSTANCES = new ConcurrentHashMap<>();

    @ApiStatus.Internal
    @VisibleForTesting
    public static void invalidateAll() {
        invalidateAll(true);
        invalidateAll(false);
    }

    //client logout must not close the integrated server's levels from the render thread
    @ApiStatus.Internal
    public static void invalidateAll(boolean clientSide) {
        Map<String, ? extends Level> map = SERVER_INSTANCES;
        if (clientSide) map = CLIENT_INSTANCES;
        for (Level l : new ArrayList<>(map.values())) {
            invalidate(l);
        }
    }

    // Manually invalidate one
    public static boolean invalidate(Level level) {
        if (level == null) return false;
        boolean removed = CLIENT_INSTANCES.values().remove(level) || SERVER_INSTANCES.values().remove(level);

        PlatHelper.invokeLevelUnload(level);
        if (level instanceof FakeServerLevel sl) close(sl);
        return removed;
    }

    private static void close(FakeServerLevel level) {
        try {
            level.close();
        } catch (Exception e) {
            if (PlatHelper.isDev()) {
                throw new RuntimeException(e);
            } else {
                Moonlight.LOGGER.error("An error occurred while closing fake level", e);
            }
        }
    }

    public static FakeLevel getDefaultClient(Level original) {
        return getClient("dummy_world", original, (id, registries) -> new FakeLevel(true, id, registries));
    }

    public static <T extends FakeLevel> T getClient(String id, Level original, BiFunction<String, RegistryAccess, FakeLevel> constructor) {
        id = "client_" + id;
        FakeLevel existing = CLIENT_INSTANCES.get(id);
        if (existing != null) return (T) existing;
        FakeLevel created = constructor.apply(id, original.registryAccess());
        FakeLevel raced = CLIENT_INSTANCES.putIfAbsent(id, created);
        if (raced != null) return (T) raced;
        return (T) created;
    }


    public static FakeServerLevel getDefaultServer(ServerLevel original) {
        return getServer("dummy_world", original, FakeServerLevel::new);
    }

    public static <T extends FakeServerLevel> T getServer(String id, ServerLevel original, BiFunction<String, ServerLevel, FakeServerLevel> constructor) {
        id = "server_" + id;
        FakeServerLevel existing = SERVER_INSTANCES.get(id);
        if (existing != null) return (T) existing;
        FakeServerLevel created = constructor.apply(id, original);
        FakeServerLevel raced = SERVER_INSTANCES.putIfAbsent(id, created);
        if (raced != null) {
            close(created);
            return (T) raced;
        }
        return (T) created;
    }

    public static Level get(String id, Level original,
                            BiFunction<String, RegistryAccess, FakeLevel> clientConstr,
                            BiFunction<String, ServerLevel, FakeServerLevel> serverConstr) {
        if (original instanceof ServerLevel sl) {
            return getServer(id, sl, serverConstr);
        } else {
            return getClient(id, original, clientConstr);
        }
    }

    public static Level getDefault(Level original) {
        if (original instanceof ServerLevel sl) {
            return getDefaultServer(sl);
        } else {
            return getDefaultClient(original);
        }
    }

    public interface ILevelLike {
        Level cast();
    }
}
