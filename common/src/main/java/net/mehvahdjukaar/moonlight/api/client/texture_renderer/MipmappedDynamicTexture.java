package net.mehvahdjukaar.moonlight.api.client.texture_renderer;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.MipmapStrategy;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

import java.util.function.Supplier;

/** DynamicTexture with real mip levels, generated on the cpu at each upload. Read it with a mipmapping sampler. */
public class MipmappedDynamicTexture extends DynamicTexture {

    private static final int USAGE = GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING;
    private static final Identifier DUMMY_NAME = Identifier.withDefaultNamespace("dynamic");

    private final int mipLevels;

    public MipmappedDynamicTexture(Supplier<String> label, int width, int height, boolean clear, int maxMipLevel) {
        super(label, width, height, clear);
        this.mipLevels = 1 + Mth.clamp(maxMipLevel, 0, Mth.log2(Math.min(width, height)));

        GpuDevice device = RenderSystem.getDevice();
        this.texture.close();
        this.textureView.close();
        this.texture = device.createTexture(label, USAGE, TextureFormat.RGBA8, width, height, 1, this.mipLevels);
        this.sampler = RenderSystem.getSamplerCache().getSampler(AddressMode.REPEAT, AddressMode.REPEAT,
                FilterMode.NEAREST, FilterMode.NEAREST, true);
        this.textureView = device.createTextureView(this.texture);
    }

    @Override
    public void upload() {
        NativeImage pixels = this.getPixels();
        NativeImage[] mips = MipmapGenerator.generateMipLevels(DUMMY_NAME, new NativeImage[]{pixels},
                this.mipLevels - 1, MipmapStrategy.MEAN, 0, Transparency.NONE);

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        for (int level = 0; level < mips.length; level++) {
            NativeImage mip = mips[level];
            encoder.writeToTexture(this.texture, mip, level, 0, 0, 0, mip.getWidth(), mip.getHeight(), 0, 0);
            //level 0 is our own image, the rest are throwaway
            if (level != 0) mip.close();
        }
    }
}
