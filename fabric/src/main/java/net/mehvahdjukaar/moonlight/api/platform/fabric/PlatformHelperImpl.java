package net.mehvahdjukaar.moonlight.api.platform.fabric;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.client.itemgroup.FabricItemGroupBuilder;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.mehvahdjukaar.moonlight.core.mixins.fabric.PackRepositoryAccessor;
import net.mehvahdjukaar.moonlight.core.network.ClientBoundSpawnCustomEntityPacket;
import net.mehvahdjukaar.moonlight.core.network.ModMessages;
import net.mehvahdjukaar.moonlight.fabric.MoonlightFabric;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;

public class PlatformHelperImpl {

    public static PlatformHelper.Platform getPlatform() {
        return PlatformHelper.Platform.FABRIC;
    }

    public static boolean isData() {
        return false;
    }

    public static boolean isModLoaded(String name) {
        return FabricLoader.getInstance().isModLoaded(name);
    }

    @Nullable
    public static <T> Field findField(Class<? super T> clazz, String fieldName) {
        return null;
    }

    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return null;
    }


    public static boolean isMobGriefingOn(Level level, Entity entity) {
        return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
    }

    public static boolean isAreaLoaded(Level level, BlockPos pos, int maxRange) {
        //crappy version for fabric :(
        return level.isLoaded(pos);
    }

    public static int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return FlammableBlockRegistry.getDefaultInstance().get(state.getBlock()).getBurnChance();
    }

    public static void addFeatureToBiome(GenerationStep.Decoration step, TagKey<Biome> tagKey, ResourceKey<PlacedFeature> feature) {
        BiomeModifications.addFeature(BiomeSelectors.tag(tagKey), step, feature);
    }

    public static PlatformHelper.Env getEnv() {
        return FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT ? PlatformHelper.Env.CLIENT : PlatformHelper.Env.SERVER;
    }

    @Nullable
    public static FoodProperties getFoodProperties(Item food, ItemStack stack, Player player) {
        return food.getFoodProperties();
    }

    public static boolean isCurativeItem(ItemStack stack, MobEffectInstance effect) {
        return stack.getItem() == Items.MILK_BUCKET || stack.getItem() == Items.HONEY_BOTTLE;
    }

    public static boolean isDev() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }


    public static int getBurnTime(ItemStack stack) {
        return FuelRegistry.INSTANCE.get(stack.getItem());
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return MoonlightFabric.currentServer;
    }

    public static Packet<?> getEntitySpawnPacket(Entity entity) {
        var packet = new ClientBoundSpawnCustomEntityPacket(entity);
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        packet.writeToBuffer(buf);
        return ServerPlayNetworking.createS2CPacket(ModMessages.SPAWN_PACKET_ID, buf);
    }

    public static Path getGamePath() {
        return FabricLoader.getInstance().getGameDir();
    }

    public static CreativeModeTab createModTab(ResourceLocation name, Supplier<ItemStack> icon, boolean hasSearchBar) {
        return FabricItemGroupBuilder.build(name, icon);
    }

    private static final HashMap<PackType, List<Supplier<Pack>>> EXTRA_PACKS = new HashMap<>();

    @SuppressWarnings("ConstantConditions")
    public static void registerResourcePack(PackType packType, Supplier<Pack> packSupplier) {
        EXTRA_PACKS.computeIfAbsent(packType, p -> new ArrayList<>()).add(packSupplier);
        if(packType == PackType.CLIENT_RESOURCES && PlatformHelper.getEnv().isClient()){
           if(Minecraft.getInstance().getResourcePackRepository() instanceof PackRepositoryAccessor rep){
               var newSources = new HashSet<>(rep.getSources());
               getAdditionalPacks(packType).forEach(l -> {
                   newSources.add((infoConsumer, b) -> infoConsumer.accept(l.get()));
               });
               rep.setSources(newSources);
           }
        }
    }

    public static Collection<Supplier<Pack>> getAdditionalPacks(PackType packType) {
        List<Supplier<Pack>> list = new ArrayList<>();
        var suppliers = EXTRA_PACKS.get(packType);
        if (suppliers != null) {
            list.addAll(suppliers);
        }
        return list;
    }
}
