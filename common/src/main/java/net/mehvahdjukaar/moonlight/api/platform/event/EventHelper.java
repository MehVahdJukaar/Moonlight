package net.mehvahdjukaar.moonlight.api.platform.event;

import dev.architectury.injectables.annotations.ExpectPlatform;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;

import java.util.function.Consumer;

public class EventHelper {

    @ExpectPlatform
    public static <T extends SimpleEvent> void addListener(Consumer<T> listener, Class<T> eventClass){
        throw new AssertionError();
    }

    @ExpectPlatform
    public static <T extends SimpleEvent> void postEvent(T event, Class<T> eventClass){
        throw new AssertionError();
    }


    @ExpectPlatform
    public static boolean onProjectileImpact(Projectile improvedProjectileEntity, HitResult blockHitResult) {
        throw new AssertionError();
    }
}
