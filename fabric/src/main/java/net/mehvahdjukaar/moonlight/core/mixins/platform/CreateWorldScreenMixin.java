package net.mehvahdjukaar.moonlight.core.mixins.platform;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.mehvahdjukaar.moonlight.platform.MoonlightFabric;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.HashSet;


@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

    @ModifyExpressionValue(method = "openFresh",
            at = @At(value = "NEW", target = "([Lnet/minecraft/server/packs/repository/RepositorySource;)Lnet/minecraft/server/packs/repository/PackRepository;"))
    private static PackRepository ml$addExtraDatapacks(PackRepository original) {

        if (original instanceof PackRepositoryAccessor pa) {
            HashSet<RepositorySource> sources = new HashSet<>(pa.getSources());
            sources.addAll(MoonlightFabric.EXTRA_DATA_PACK_SOURCES);
            pa.setSources(sources);
        }
        return original;
    }
}
