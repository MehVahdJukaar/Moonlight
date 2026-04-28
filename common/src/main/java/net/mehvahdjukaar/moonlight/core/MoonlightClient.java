package net.mehvahdjukaar.moonlight.core;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.fluids.SoftFluidColors;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.*;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.mehvahdjukaar.moonlight.core.client.SimpleSpecialModelsLoader;
import net.mehvahdjukaar.moonlight.core.client.SpawnBoxBlockEntityRenderer;
import net.mehvahdjukaar.moonlight.core.pack.DynamicResourcesInternals;
import net.mehvahdjukaar.moonlight.core.pack.MergedDynamicClientResourcesProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@ApiStatus.Internal
public class MoonlightClient {

    private static final ThreadLocal<Boolean> MAP_MIPMAP = ThreadLocal.withInitial(() -> false);

    private static final MergedDynamicClientResourcesProvider INSTANCE = new MergedDynamicClientResourcesProvider(
            new PackLocationInfo("moonlight:merged_pack",
                    Component.translatable("message.moonlight.merged_pack.title"),
                    PackSource.BUILT_IN, Optional.empty())
    );

    public static void initClient() {
        ClientConfigs.init();
        ClientHelper.addShaderRegistration(MoonlightClient::registerShaders);
        ClientHelper.addClientReloadListener(SoftFluidColors::new, Moonlight.res("soft_fluid"));
        ClientHelper.addBlockEntityRenderersRegistration(event -> {
            event.register(MoonlightRegistry.SPAWN_BOX_BLOCK_ENTITY.get(), SpawnBoxBlockEntityRenderer::new);
        });

        var specialModels = new SimpleSpecialModelsLoader();
        ClientHelper.addClientReloadListener(() -> specialModels, Moonlight.res("special_models_loader"));
        ClientHelper.addSpecialModelRegistration(specialModelEvent -> {
            specialModels.getSpecialModels().forEach(specialModelEvent::register);
        });

        RegHelper.registerDynamicResourceProvider(new MLDynamicClientResources());
    }

    //null when merge happened. not null when it should add normally
    @Nullable
    public static SimplePackProvider mergePackSupplier(DynamicResourcesProvider provider) {
        if (!ClientConfigs.MERGE_PACKS.get()) return provider;
        INSTANCE.add(provider);
        if (INSTANCE.size() == 1) return INSTANCE;
        return null; //dont register if we already have stuff here. it means its already registered
    }

    @Deprecated(forRemoval = true)
    public static boolean maybeMergeLegacyPack(DynamicResourcePack pack) {
        if (!ClientConfigs.MERGE_PACKS.get()) return false; //no op
        INSTANCE.addLegacy(pack);
        if (INSTANCE.size() == 1) {
            RegHelper.registerResourcePack(PackType.CLIENT_RESOURCES,
                    INSTANCE::createPack);
        }
        return true;
    }


    public static boolean isClientThread() {
        return Minecraft.getInstance().isSameThread();
    }

    public static void setupClient() {
        var e = ExtraModelData.EMPTY;
        //class-loaded on main thread to prevent possible race condition BS
    }

    @EventCalled
    public static void onItemTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> list) {
        if (ClientConfigs.TAGS_TOOLTIP.get().isOn(tooltipFlag)) {
            Item item = stack.getItem();
            // BLOCK TAGS
            if (item instanceof BlockItem bi) {
                Block b = bi.getBlock();
                BlockState state = b.defaultBlockState();
                var tags = state.getTags().toList();
                if (!tags.isEmpty()) {
                    list.add(Component.translatable("tooltip.moonlight.block_tags").withStyle(ChatFormatting.GREEN));
                    tags.forEach((k) -> list.add(Component.literal("-" + k.location())
                            .withStyle(Style.EMPTY.withColor(0xc8ffc8))));
                }
            }

            // ITEM TAGS
            var tags = stack.getTags().toList();
            if (!tags.isEmpty()) {
                list.add(Component.translatable("tooltip.moonlight.item_tags").withStyle(ChatFormatting.LIGHT_PURPLE));
                tags.forEach((k) -> list.add(Component.literal("-" + k.location())
                        .withStyle(Style.EMPTY.withColor(0xffc8ff))));
            }
        }
    }

    @EventCalled
    public static void registerShaders(ClientHelper.ShaderEvent event) {
        event.register(Moonlight.res("particle_translucent"), DefaultVertexFormat.POSITION_TEX,
                MLRenderTypes.PARTICLE_TRANSLUCENT_SHADER::assign);
        event.register(Moonlight.res("text_alpha_color"), DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                MLRenderTypes.TEXT_COLOR_SHADER::assign);
    }

    @EventCalled
    public static void afterTextureReload() {
        DynamicResourcesInternals.clearAfterReload(PackType.CLIENT_RESOURCES);
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


    private static class MLDynamicClientResources extends DynamicClientResourceProvider {

        protected MLDynamicClientResources() {
            super(Moonlight.res("dynamic_resources"), PackGenerationStrategy.REGEN_ON_EVERY_RELOAD);
        }

        @Override
        protected void addDynamicTranslations(AfterLanguageLoadEvent afterLanguageLoadEvent) {
        }

        @Override
        protected Collection<String> gatherSupportedNamespaces() {
            return List.of("minecraft");
        }

        @Override
        protected void regenerateDynamicAssets(Consumer<ResourceGenTask> executor) {
            fixShade = ClientConfigs.FIX_SHADE.get();
            if (fixShade != ClientConfigs.ShadeFix.FALSE) {
                // applyFixedShade();

                executor.accept((manager, sink) -> {
                    sink.addBytes(ResourceLocation.parse("shaders/include/light.glsl"),
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
                });
            }
        }


    }

    public static ClientConfigs.ShadeFix fixShade = ClientConfigs.ShadeFix.FALSE;


    // such neat numbers. These give exactly the same shade that block use (1, 0.8, 0.6, 0.5)
    public static final Vector3f NEW_L_0 = new Vector3f(0.2f, 7 / 9f, -0.6f).normalize();
    public static final Vector3f NEW_L_1 = new Vector3f(-0.2f, 7 / 9f, 0.6f).normalize();


}
