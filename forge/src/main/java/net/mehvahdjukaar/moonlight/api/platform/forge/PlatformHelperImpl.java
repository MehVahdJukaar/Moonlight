package net.mehvahdjukaar.moonlight.api.platform.forge;

import net.mehvahdjukaar.moonlight.api.platform.PlatformHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PlatformHelperImpl {

    public static boolean isDev() {
        return !FMLLoader.isProduction();
    }

    public static boolean isData() {
        return FMLLoader.getLaunchHandler().isData();
    }


    public static PlatformHelper.Platform getPlatform() {
        return PlatformHelper.Platform.FORGE;
    }

    public static boolean isModLoaded(String name) {
        return ModList.get().isLoaded(name);
    }

    @Nullable
    public static <T> Field findField(Class<? super T> clazz, String fieldName) {
        try {
            return ObfuscationReflectionHelper.findField(clazz, fieldName);
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            return ObfuscationReflectionHelper.findMethod(clazz, methodName, parameterTypes);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isMobGriefingOn(Level level, Entity entity) {
        return ForgeEventFactory.getMobGriefingEvent(level, entity);
    }

    public static boolean isAreaLoaded(Level level, BlockPos pos, int maxRange) {
        return level.isAreaLoaded(pos, maxRange);
    }

    public static int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return state.getFlammability(level, pos, face);
    }

    public static PlatformHelper.Env getEnv() {
        return FMLEnvironment.dist == Dist.CLIENT ? PlatformHelper.Env.CLIENT : PlatformHelper.Env.SERVER;
    }

    @Nullable
    public static FoodProperties getFoodProperties(Item food, ItemStack stack, Player player) {
        return food.getFoodProperties(stack, player);
    }

    public static boolean isCurativeItem(ItemStack stack, MobEffectInstance effect) {
        return effect.isCurativeItem(stack);
    }


    public static void registerResourcePack(PackType packType, Supplier<Pack> packSupplier) {
        var bus = FMLJavaModLoadingContext.get().getModEventBus();
        Consumer<AddPackFindersEvent> consumer = event->{
            if (event.getPackType() == packType) {
                event.addRepositorySource((infoConsumer, packFactory) ->
                        infoConsumer.accept(packSupplier.get()));
            }
        };
        bus.addListener(consumer);
    }

    public static int getBurnTime(ItemStack stack) {
        return stack.getBurnTime(null);
    }

    @Nullable
    public static MinecraftServer getCurrentServer() {
        return ServerLifecycleHooks.getCurrentServer();
    }

    public static Packet<?> getEntitySpawnPacket(Entity entity) {
        return NetworkHooks.getEntitySpawningPacket(entity);
    }

    public static Path getGamePath() {
        return FMLPaths.GAMEDIR.get();
    }

    public static CreativeModeTab createModTab(ResourceLocation name, Supplier<ItemStack> icon, boolean hasSearchBar) {
        return new CreativeModeTab(name.getPath()) {
            @Override
            public ItemStack makeIcon() {
                return icon.get();
            }

            @Override
            public boolean hasSearchBar() {
                return hasSearchBar;
            }
        };
    }

    public static SpawnEggItem newSpawnEgg(Supplier<? extends EntityType<? extends Mob>> entityType, int color, int outerColor, Item.Properties properties) {
        return new ForgeSpawnEggItem(entityType, color, outerColor, properties);
    }


}
