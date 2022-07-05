package net.mehvahdjukaar.moonlight.platform;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.mehvahdjukaar.moonlight.platform.configs.ConfigBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Supplier;

public class PlatformHelper {

    @ExpectPlatform
    public static boolean isDev() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isData() {
        throw new AssertionError();
    }


    public enum Platform {
        FORGE, FABRIC;

        public boolean isForge() {
            return this == FORGE;
        }

        public boolean isFabric() {
            return this == FABRIC;
        }

        public void ifForge(Runnable runnable) {
            if (isForge()) runnable.run();
        }

        public void ifFabric(Runnable runnable) {
            if (isFabric()) runnable.run();
        }
    }

    @ExpectPlatform
    public static Platform getPlatform() {
        throw new AssertionError();
    }

    public enum Env {
        CLIENT, SERVER;

        public boolean isClient() {
            return this == CLIENT;
        }

        public boolean isServer() {
            return this == SERVER;
        }

        public void ifClient(Runnable runnable) {
            if (isClient()) runnable.run();
        }

        public void ifServer(Runnable runnable) {
            if (isServer()) runnable.run();
        }
    }

    @ExpectPlatform
    public static Env getEnv() {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static <T> Field findField(Class<? super T> clazz, String fieldName) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        throw new AssertionError();
    }

    @Nullable
    @ExpectPlatform
    public static MinecraftServer getCurrentServer(){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isModLoaded(String name) {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static void registerResourcePack(PackType packType, Supplier<Pack> packSupplier) {
        throw new AssertionError();
    }


    @ExpectPlatform
    public static boolean isMobGriefingOn(Level level, Entity entity) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static boolean isAreaLoaded(Level level, BlockPos pos, int maxRange) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        throw new AssertionError();
    }

    @ExpectPlatform
    @Nullable
    public static FoodProperties getFoodProperties(Item food, ItemStack stack, Player player) {
        throw  new AssertionError();
    }

    @ExpectPlatform
    public static boolean isCurativeItem(ItemStack stack, MobEffectInstance effect) {
       throw new AssertionError();
    }

    @ExpectPlatform
    public static int getBurnTime(ItemStack stack) {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Packet<?> getEntitySpawnPacket(Entity entity){
        throw new AssertionError();
    }
}
