package net.mehvahdjukaar.moonlight.core;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidColors;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynClientResourcesGenerator;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicResourcePack;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynamicTexturePack;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@ApiStatus.Internal
public class MoonlightClient {

    private static final ThreadLocal<Boolean> MAP_MIPMAP = ThreadLocal.withInitial(() -> false);
    private static MergedDynamicTexturePack mergedDynamicPack;

    public static void initClient() {
        ClientHelper.addShaderRegistration(MoonlightClient::registerShaders);
        ClientHelper.addClientReloadListener(SoftFluidColors::new, Moonlight.res("soft_fluids"));
        ClientConfigs.init();
        var gen = new Gen();
        gen.register();
    }

    public static DynamicTexturePack maybeMergePack(DynamicTexturePack pack) {
        if (!ClientConfigs.MERGE_PACKS.get()) return pack;
        if (mergedDynamicPack == null) {
            mergedDynamicPack = new MergedDynamicTexturePack() {
            };
        }
        Set<String> nameSpaces = pack.getNamespaces(pack.getPackType());
        for (var n : nameSpaces) mergedDynamicPack.addNamespaces(n);
        mergedDynamicPack.mods++;
        return mergedDynamicPack;
    }

    private static class MergedDynamicTexturePack extends DynamicTexturePack {
        int mods = 0;

        public MergedDynamicTexturePack() {
            super(Moonlight.res("mods_dynamic_assets"));
        }

        @Override
        public Component makeDescription() {
            return Component.literal("Dynamic resources for " + mods + (mods == 1 ? " mod" : " mods"));
        }
    }

    @EventCalled
    public static void registerShaders(ClientHelper.ShaderEvent event) {
        event.register(Moonlight.res("particle_translucent"), DefaultVertexFormat.POSITION_TEX,
                MLRenderTypes.PARTICLE_TRANSLUCENT_SHADER::set);
        event.register(Moonlight.res("text_alpha_color"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                MLRenderTypes.TEXT_COLOR_SHADER::set);
    }

    @EventCalled
    public static void afterTextureReload() {
        DynamicResourcePack.clearAfterReload(PackType.CLIENT_RESOURCES);
    }

    public static void setMipMap(boolean b) {
        if (ClientConfigs.MAPS_MIPMAP.get() == 0) {
            b = false;
        }
        MAP_MIPMAP.set(b);
    }

    public static boolean isMapMipMap() {
        return MAP_MIPMAP.get();
    }


    private static class Gen extends DynClientResourcesGenerator {
        public Gen() {
            super(new DynamicTexturePack(Moonlight.res("generated_pack")));
            this.dynamicPack.addNamespaces("minecraft");
        }

        @Override
        public Logger getLogger() {
            return Moonlight.LOGGER;
        }

        @Override
        public boolean dependsOnLoadedPacks() {
            return true;
        }


        @Override
        public void regenerateDynamicAssets(ResourceManager manager) {
            fixShade = ClientConfigs.FIX_SHADE.get();
            if (fixShade != ClientConfigs.ShadeFix.FALSE) {

                dynamicPack.addBytes(ResourceLocation.parse("shaders/include/light.glsl"),
                        ("""
                                #version 150
                                
                                #define MINECRAFT_LIGHT_POWER   (0.6)
                                #define MINECRAFT_LIGHT_POWER_FIXED   (0.5)
                                #define MINECRAFT_AMBIENT_LIGHT (0.4)
                                #define MINECRAFT_AMBIENT_LIGHT_FIXED (0.5)
                                
                                vec4 minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {
                                    lightDir0 = normalize(lightDir0);
                                    lightDir1 = normalize(lightDir1);
                                    float light0 = max(0.0, dot(lightDir0, normal));
                                    float light1 = max(0.0, dot(lightDir1, normal));
                                
                                    float dotP = dot(lightDir0, lightDir1);
                                    bool isFixed = dotP > 0.20 && dotP < 0.205;
                                    float lightPow = isFixed ? MINECRAFT_LIGHT_POWER_FIXED : MINECRAFT_LIGHT_POWER;
                                    float ambientLight = isFixed ? MINECRAFT_AMBIENT_LIGHT_FIXED : MINECRAFT_AMBIENT_LIGHT;
                                
                                    float lightAccum = min(1.0, (light0 + light1) * lightPow + ambientLight);
                                    return vec4(color.rgb * lightAccum, color.a);
                                }
                                
                                vec4 minecraft_sample_lightmap(sampler2D lightMap, ivec2 uv) {
                                    return texture(lightMap, clamp(uv / 256.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
                                }""").getBytes()

                        , ResType.GENERIC);
            }
        }
    }

    public static ClientConfigs.ShadeFix fixShade = ClientConfigs.ShadeFix.FALSE;


    // such neat numbers. These give exactly the same shade that block use (1, 0.8, 0.6, 0.5)
    public static final Vector3f NEW_L_0 = new Vector3f(0.2f, 7 / 9f, -0.6f).normalize();
    public static final Vector3f NEW_L_1 = new Vector3f(-0.2f, 7 / 9f, 0.6f).normalize();


}
