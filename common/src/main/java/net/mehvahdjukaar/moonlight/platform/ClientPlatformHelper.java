package net.mehvahdjukaar.moonlight.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;

import java.nio.file.Path;
import java.util.function.Supplier;

public class ClientPlatformHelper {

    @ExpectPlatform
    public static void registerRenderType(Block block, RenderType type){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getModIcon(String modId) {
        throw new AssertionError();
    }


}
