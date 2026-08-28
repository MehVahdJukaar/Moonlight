package net.mehvahdjukaar.moonlight.core;

import com.mojang.blaze3d.platform.Lighting;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.client.model.ExtraModelData;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.fluids.client.SoftFluidColors;
import net.mehvahdjukaar.moonlight.api.misc.EventCalled;
import net.mehvahdjukaar.moonlight.api.platform.ClientHelper;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.api.resources.ResType;
import net.mehvahdjukaar.moonlight.api.resources.pack.*;
import net.mehvahdjukaar.moonlight.core.client.MLRenderTypes;
import net.mehvahdjukaar.moonlight.core.client.MoonlightHubInfo;
import net.mehvahdjukaar.moonlight.core.client.OurModsList;
import net.mehvahdjukaar.moonlight.core.client.SpawnBoxBlockEntityRenderer;
import net.mehvahdjukaar.moonlight.core.pack.DynamicResourcesInternals;
import net.mehvahdjukaar.moonlight.core.pack.MergedDynamicClientResourcesProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
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
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.mehvahdjukaar.moonlight.api.client.gui.AnimatedGuiItem;
import net.mehvahdjukaar.moonlight.api.client.gui.ModCatalogAPI;

@ApiStatus.Internal
public class MoonlightClient {

    private static final MergedDynamicClientResourcesProvider INSTANCE = new MergedDynamicClientResourcesProvider(
            new PackLocationInfo("moonlight:merged_pack",
                    Component.translatable("message.moonlight.merged_pack.title"),
                    PackSource.BUILT_IN, Optional.empty())
    );

    public static void initClient() {
        ClientConfigs.init();
        MoonlightHubInfo.fetchFromServer();
        ModCatalogAPI.register(OurModsList.INSTANCE);
        ClientHelper.addRenderPipelineRegistration(MoonlightClient::registerRenderPipelines);
        AnimatedGuiItem.register();
        ClientHelper.addClientReloadListener(SoftFluidColors::new, Moonlight.res("soft_fluid"));
        ClientHelper.addBlockEntityRenderersRegistration(event -> {
            event.register(MoonlightRegistry.SPAWN_BOX_BLOCK_ENTITY.get(), SpawnBoxBlockEntityRenderer::new);
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

    public static boolean isClientThread() {
        return Minecraft.getInstance().isSameThread();
    }

    public static void setupClient() {
        var e = ExtraModelData.EMPTY;
        //class-loaded on main thread to prevent possible race condition BS
    }

    @EventCalled
    public static void onItemTooltip(ItemStack stack, Item.TooltipContext tooltipContext, TooltipFlag tooltipFlag, List<Component> list) {
        if (ClientConfigs.TAGS_TOOLTIP.get().isOn(tooltipFlag) && tooltipFlag.isAdvanced()) {
            Item item = stack.getItem();
            // BLOCK TAGS
            if (item instanceof BlockItem bi) {
                Block b = bi.getBlock();
                BlockState state = b.defaultBlockState();
                var tags = state.tags().toList();
                if (!tags.isEmpty()) {
                    list.add(Component.translatable("tooltip.moonlight.block_tags").withStyle(ChatFormatting.GREEN));
                    tags.forEach((k) -> list.add(Component.literal("-" + k.location())
                            .withStyle(Style.EMPTY.withColor(0xc8ffc8))));
                }
            }

            // ITEM TAGS
            var tags = stack.tags().toList();
            if (!tags.isEmpty()) {
                list.add(Component.translatable("tooltip.moonlight.item_tags").withStyle(ChatFormatting.LIGHT_PURPLE));
                tags.forEach((k) -> list.add(Component.literal("-" + k.location())
                        .withStyle(Style.EMPTY.withColor(0xffc8ff))));
            }
        }
    }

    @EventCalled
    public static void registerRenderPipelines(ClientHelper.RenderPipelineEvent event) {
        event.register(MLRenderTypes.TEXT_ALPHA_COLOR);
        event.register(MLRenderTypes.ADDITIVE_PARTICLE);
    }

    @EventCalled
    public static void afterTextureReload() {
        DynamicResourcesInternals.clearAfterReload(PackType.CLIENT_RESOURCES);
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
            Minecraft.getInstance().schedule(() -> applyShadeFix(fixShade));
            if (fixShade != ClientConfigs.ShadeFix.FALSE) {
                executor.accept((manager, sink) -> {
                    // vanilla light.glsl with the power and ambient term swapped when it sees our light directions.
                    // dot(NEW_L_0, NEW_L_1) is 0.2039, no vanilla pair hits that
                    sink.addBytes(Identifier.parse("shaders/include/light.glsl"),
                            ("""
                                    #version 330

                                    #define MINECRAFT_LIGHT_POWER   (0.6)
                                    #define MINECRAFT_LIGHT_POWER_FIXED   (0.5)
                                    #define MINECRAFT_AMBIENT_LIGHT (0.4)
                                    #define MINECRAFT_AMBIENT_LIGHT_FIXED (0.5)

                                    layout(std140) uniform Lighting {
                                        vec3 Light0_Direction;
                                        vec3 Light1_Direction;
                                    };

                                    vec2 minecraft_compute_light(vec3 lightDir0, vec3 lightDir1, vec3 normal) {
                                        return vec2(dot(lightDir0, normal), dot(lightDir1, normal));
                                    }

                                    vec4 minecraft_mix_light_separate(vec2 light, vec4 color) {
                                        float dotP = dot(normalize(Light0_Direction), normalize(Light1_Direction));
                                        bool isFixed = dotP > 0.20 && dotP < 0.205;
                                        float lightPow = isFixed ? MINECRAFT_LIGHT_POWER_FIXED : MINECRAFT_LIGHT_POWER;
                                        float ambientLight = isFixed ? MINECRAFT_AMBIENT_LIGHT_FIXED : MINECRAFT_AMBIENT_LIGHT;

                                        vec2 lightValue = max(vec2(0.0), light);
                                        float lightAccum = min(1.0, (lightValue.x + lightValue.y) * lightPow + ambientLight);
                                        return vec4(color.rgb * lightAccum, color.a);
                                    }

                                    vec4 minecraft_mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {
                                        vec2 light = minecraft_compute_light(lightDir0, lightDir1, normal);
                                        return minecraft_mix_light_separate(light, color);
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

    private static Vector3f @Nullable [] vanillaDiffuseLights = null;

    // the level entries are plain statics but the gui item ones are baked into the lighting ubo, so those get pushed again
    public static void applyShadeFix(ClientConfigs.ShadeFix fix) {
        Lighting lighting = Minecraft.getInstance().gameRenderer.getLighting();
        if (vanillaDiffuseLights == null) {
            vanillaDiffuseLights = new Vector3f[]{Lighting.DIFFUSE_LIGHT_0, Lighting.DIFFUSE_LIGHT_1,
                    Lighting.NETHER_DIFFUSE_LIGHT_0, Lighting.NETHER_DIFFUSE_LIGHT_1};
        }

        boolean fixLevel = fix != ClientConfigs.ShadeFix.FALSE;
        Lighting.DIFFUSE_LIGHT_0 = fixLevel ? NEW_L_0 : vanillaDiffuseLights[0];
        Lighting.DIFFUSE_LIGHT_1 = fixLevel ? NEW_L_1 : vanillaDiffuseLights[1];
        Lighting.NETHER_DIFFUSE_LIGHT_0 = fixLevel ? NEW_L_0 : vanillaDiffuseLights[2];
        Lighting.NETHER_DIFFUSE_LIGHT_1 = fixLevel ? NEW_L_1 : vanillaDiffuseLights[3];

        ClientLevel level = Minecraft.getInstance().level;
        if (level != null) {
            lighting.updateLevel(level.dimensionType().cardinalLightType());
        }

        Vector3f gui0 = fix == ClientConfigs.ShadeFix.TRUE ? NEW_L_0 : vanillaDiffuseLights[0];
        Vector3f gui1 = fix == ClientConfigs.ShadeFix.TRUE ? NEW_L_1 : vanillaDiffuseLights[1];
        // same poses Lighting's constructor uses
        Matrix4f flatPose = new Matrix4f().rotationY(-(float) Math.PI / 8F).rotateX(2.3561945F);
        lighting.updateBuffer(Lighting.Entry.ITEMS_FLAT,
                flatPose.transformDirection(gui0, new Vector3f()),
                flatPose.transformDirection(gui1, new Vector3f()));
        Matrix4f item3DPose = new Matrix4f().scaling(1.0F, -1.0F, 1.0F)
                .rotateYXZ(1.0821041F, 3.2375858F, 0.0F)
                .rotateYXZ(-(float) Math.PI / 8F, 2.3561945F, 0.0F);
        lighting.updateBuffer(Lighting.Entry.ITEMS_3D,
                item3DPose.transformDirection(gui0, new Vector3f()),
                item3DPose.transformDirection(gui1, new Vector3f()));
    }

}
