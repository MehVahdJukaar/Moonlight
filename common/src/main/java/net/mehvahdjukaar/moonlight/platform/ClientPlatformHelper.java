package net.mehvahdjukaar.moonlight.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;

public class ClientPlatformHelper {

    @ExpectPlatform
    public static void registerRenderType(Block block, RenderType type){
        throw new AssertionError();
    }





}
