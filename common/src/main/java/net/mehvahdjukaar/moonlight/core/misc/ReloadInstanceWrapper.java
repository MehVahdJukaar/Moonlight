package net.mehvahdjukaar.moonlight.core.misc;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.pack.DynamicResourcesInternals;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ReloadInstanceWrapper implements ReloadInstance {

    public static ReloadInstance wrap(Supplier<ReloadInstance> factory, PackType type, ResourceManager manager, Executor backgroundExecutor) {
        return new ReloadInstanceWrapper(factory, type, manager, backgroundExecutor);
    }

    public static void executeEarlyReloadBlocking(PackType type, ResourceManager manager, IProgressTracker progressTracker) {
        DynamicResourcesInternals.clearBeforeReload(type);
        MoonlightEventsHelper.postEvent(new EarlyPackReloadEvent(List.of(), manager, type, progressTracker), EarlyPackReloadEvent.class);
    }

    private final Supplier<ReloadInstance> lazyInstance;
    private final CompletableFuture<Unit> beforeTask;
    private final IProgressTracker.Tree progressTracker;

    public ReloadInstanceWrapper(Supplier<ReloadInstance> factory,
                                 PackType type, ResourceManager manager, Executor executor) {
        this.progressTracker = IProgressTracker.createTree(1);
        this.lazyInstance = Suppliers.memoize(factory::get);
        this.beforeTask = CompletableFuture.supplyAsync(() -> {
            executeEarlyReloadBlocking(type, manager, progressTracker);
            return Unit.INSTANCE;
        }, executor);


    }

    @Nullable
    private ReloadInstance allErrorsInPackReloadWillHaveThisLineOnTheirStackTrace_DoesntMeanItsTheCause() {
        if (beforeTask.isDone() && !beforeTask.isCompletedExceptionally()) {
            return lazyInstance.get();
        }
        return null;
    }

    @Override
    public CompletableFuture<?> done() {
        return beforeTask.thenCompose(unused -> {
            ReloadInstance actual = allErrorsInPackReloadWillHaveThisLineOnTheirStackTrace_DoesntMeanItsTheCause();
            return actual.done();
        });
    }

    @Override
    public float getActualProgress() {
        float maxAmount = Mth.clamp(0.2f, 0, 0.5f);
        float progress = progressTracker.getProgress() ;
        if (!beforeTask.isDone()) {
            return progress;
        }
        ReloadInstance actual = allErrorsInPackReloadWillHaveThisLineOnTheirStackTrace_DoesntMeanItsTheCause();
        if (actual != null) {
            return actual.getActualProgress() * (1 );
        }
        return 1;
    }


    @Override
    public void checkExceptions() {
        if (!beforeTask.isDone()) {
            return;
        }
        if (beforeTask.isCompletedExceptionally()) {
            beforeTask.join(); // This will throw the exception
        }
        ReloadInstance actual = allErrorsInPackReloadWillHaveThisLineOnTheirStackTrace_DoesntMeanItsTheCause();
        if (actual != null) {
            actual.checkExceptions();
        }
    }

}
