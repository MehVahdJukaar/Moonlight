package net.mehvahdjukaar.moonlight.core.mixins.fabric;

import net.mehvahdjukaar.moonlight.api.platform.fabric.PlatformHelperImpl;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;


@Mixin(PackRepository.class)
public abstract class PackRepositoryMixin {

    @Shadow
    @Final
    private Set<RepositorySource> sources;

    @Inject(method = "<init>(Lnet/minecraft/server/packs/PackType;[Lnet/minecraft/server/packs/repository/RepositorySource;)V",
            at = @At("TAIL"))
    private void init(PackType packType, RepositorySource[] repositorySources, CallbackInfo ci) {
        var list = PlatformHelperImpl.getAdditionalPacks(packType);
        var newSources = new HashSet<>(((PackRepositoryAccessor) this).getSources());
        list.forEach(l -> {
            newSources.add((infoConsumer, b) -> infoConsumer.accept(l.get()));
        });
        ((PackRepositoryAccessor) this).setSources(newSources);
    }
}
