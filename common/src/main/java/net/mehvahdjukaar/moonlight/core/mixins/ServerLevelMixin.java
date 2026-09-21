package net.mehvahdjukaar.moonlight.core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.datafixers.DataFixer;
import net.mehvahdjukaar.moonlight.api.misc.fake_level.FakeServerLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.ChunkStatusUpdateListener;
import net.minecraft.world.level.entity.EntityPersistentStorage;
import net.minecraft.world.level.entity.LevelCallback;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.SavedDataStorage;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {

    protected ServerLevelMixin(WritableLevelData levelData, ResourceKey<Level> dimension, RegistryAccess registryAccess, Holder<DimensionType> dimensionTypeRegistration, boolean isClientSide, boolean isDebug, long biomeZoomSeed, int maxChainedNeighborUpdates) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;Lcom/mojang/datafixers/DataFixer;Lnet/minecraft/world/level/levelgen/structure/templatesystem/StructureTemplateManager;Ljava/util/concurrent/Executor;Lnet/minecraft/world/level/chunk/ChunkGenerator;IIZLnet/minecraft/world/level/entity/ChunkStatusUpdateListener;Ljava/util/function/Supplier;)Lnet/minecraft/server/level/ServerChunkCache;"))
    private ServerChunkCache ml$assignFakeChunkCache(ServerLevel level, LevelStorageSource.LevelStorageAccess levelStorageAccess,
                                                DataFixer fixerUpper, StructureTemplateManager structureManager,
                                                Executor dispatcher, ChunkGenerator generator, int viewDistance,
                                                int simulationDistance, boolean sync,
                                                ChunkStatusUpdateListener chunkStatusListener,
                                                Supplier<SavedDataStorage> dataStorage,
                                                Operation<ServerChunkCache> operation) {
        if ((Object) this instanceof FakeServerLevel) {
            return FakeServerLevel.createDummyChunkCache(level, levelStorageAccess, fixerUpper, structureManager, dispatcher, generator, viewDistance, simulationDistance, sync, chunkStatusListener, dataStorage);
        }
        return operation.call(level, levelStorageAccess, fixerUpper, structureManager, dispatcher, generator, viewDistance, simulationDistance, sync, chunkStatusListener, dataStorage);
    }

    @WrapOperation(method = "<init>", at = @At(value = "NEW", target = "(Ljava/lang/Class;Lnet/minecraft/world/level/entity/LevelCallback;Lnet/minecraft/world/level/entity/EntityPersistentStorage;)Lnet/minecraft/world/level/entity/PersistentEntitySectionManager;"))
    private PersistentEntitySectionManager<?> ml$assignFakePersistentEntitySectionManager(
            Class entityClass, LevelCallback callbacks, EntityPersistentStorage permanentStorage,
            Operation<PersistentEntitySectionManager> original) {
        if ((Object) this instanceof FakeServerLevel) {
            return FakeServerLevel.createDummyEntityManager(entityClass, callbacks, permanentStorage);
        }
        return original.call(entityClass, callbacks, permanentStorage);
    }
}
