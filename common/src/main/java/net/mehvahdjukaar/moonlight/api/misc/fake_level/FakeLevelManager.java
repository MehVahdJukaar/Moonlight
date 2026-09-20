package net.mehvahdjukaar.moonlight.api.misc.fake_level;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class FakeLevelManager {

    //shared by client and integrated server threads
    protected static final Map<String, Level> INSTANCES = new Object2ObjectArrayMap<>();

    @ApiStatus.Internal
    @VisibleForTesting
    public static void invalidateAll() {
        invalidateAll(true);
        invalidateAll(false);
    }

    //client logout must not close the integrated server's levels from the render thread
    @ApiStatus.Internal
    public static synchronized void invalidateAll(boolean clientSide) {
        List<Level> toRemove = new ArrayList<>();
        for (var l : INSTANCES.values()) {
            if (l instanceof FakeServerLevel != clientSide) toRemove.add(l);
        }
        toRemove.forEach(FakeLevelManager::invalidate);
    }

    // Manually invalidate one
    public static synchronized boolean invalidate(Level level) {
        boolean removed = INSTANCES.entrySet().removeIf(e -> e.getValue() == level);

        if (level != null) PlatHelper.invokeLevelUnload(level);
        try {
            if (level instanceof FakeServerLevel) {
                level.close();
            }
        } catch (Exception e) {
            if (PlatHelper.isDev()) {
                throw new RuntimeException(e);
            } else {
                Moonlight.LOGGER.error("An error occurred while closing fake level", e);
            }
        }
        return removed;
    }

    public static FakeLevel getDefaultClient(Level original) {
        return getClient("dummy_world", original, (id, registries) -> new FakeLevel(true, id, registries));
    }

    public static synchronized <T extends FakeLevel> T getClient(String id, Level original, BiFunction<String, RegistryAccess, FakeLevel> constructor) {
        id = "client_" + id;
        String finalId = id;
        return (T) INSTANCES.computeIfAbsent(id, k -> constructor.apply(finalId, original.registryAccess()));
    }


    public static FakeServerLevel getDefaultServer(ServerLevel original) {
        return getServer("dummy_world", original, FakeServerLevel::new);
    }

    public static synchronized <T extends FakeServerLevel> T getServer(String id, ServerLevel original, BiFunction<String, ServerLevel, FakeServerLevel> constructor) {
        id = "server_" + id;
        String finalId = id;
        return (T) INSTANCES.computeIfAbsent(id, k -> constructor.apply(finalId, original));
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
