package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.mehvahdjukaar.moonlight.fabric.MoonlightFabric;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraft.server.packs.repository.ServerPacksSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;


@Mixin(ServerPacksSource.class)
public abstract class ServerPackSourceMixin {

    @ModifyReturnValue(method = "createVanillaTrustedRepository",
            at = @At(value = "RETURN"))
    private static PackRepository ml$addExtraDatapacks(PackRepository original) {

        if (original instanceof PackRepositoryAccessor pa) {
            HashSet<RepositorySource> sources = new HashSet<>(pa.getSources());
            sources.addAll(MoonlightFabric.EXTRA_DATA_PACK_SOURCES);
            pa.setSources(sources);
        }
        return original;
    }

    @ModifyReturnValue(method = "createPackRepository(Ljava/nio/file/Path;Lnet/minecraft/world/level/validation/DirectoryValidator;)Lnet/minecraft/server/packs/repository/PackRepository;",
            at = @At(value = "RETURN"))
    private static PackRepository ml$addExtraDatapacks2(PackRepository original) {

        if (original instanceof PackRepositoryAccessor pa) {
            HashSet<RepositorySource> sources = new HashSet<>(pa.getSources());
            sources.addAll(MoonlightFabric.EXTRA_DATA_PACK_SOURCES);
            pa.setSources(sources);
        }
        return original;
    }
}
