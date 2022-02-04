package net.mehvahdjukaar.selene;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

//mods whose id should come last in mod list so it can run after everyting else. thanks forge
@Mod("zzz_woodloader")
public class WoodLoaderMod {
    public WoodLoaderMod() {
        //tell display test to ignore us
        ModLoadingContext modLoader = ModLoadingContext.get();
        modLoader.registerExtensionPoint(IExtensionPoint.DisplayTest.class,
                () -> new IExtensionPoint.DisplayTest(() -> "ANY", (remote, isServer) -> true));
    }
}
