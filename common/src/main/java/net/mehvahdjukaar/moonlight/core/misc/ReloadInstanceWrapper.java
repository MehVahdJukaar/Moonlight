package net.mehvahdjukaar.moonlight.core.misc;

import com.google.common.base.Suppliers;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.mehvahdjukaar.moonlight.api.resources.pack.DynResourceGenerator;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ReloadInstanceWrapper implements ReloadInstance {

    public static ReloadInstance wrap(Supplier<ReloadInstance> factory, PackType type, ResourceManager manager) {
        return new ReloadInstanceWrapper(factory, type, manager);
    }

    private static CompletableFuture<Unit> earlyReloadTask(PackType type, ResourceManager manager, IProgressTracker progressTracker) {
        return CompletableFuture.supplyAsync(() -> {
            DynResourceGenerator.clearBeforeReload(type);

            MoonlightEventsHelper.postEvent(new EarlyPackReloadEvent(List.of(), manager, type, progressTracker), EarlyPackReloadEvent.class);
            return Unit.INSTANCE;
        });
    }


    private final Supplier<ReloadInstance> lazyInstance;
    private final CompletableFuture<Unit> beforeTask;
    private final IProgressTracker.Tree progressTracker;
    private final int leaves;

    public ReloadInstanceWrapper(Supplier<ReloadInstance> factory,
                           PackType type, ResourceManager manager) {
        progressTracker = IProgressTracker.createTree(1);
        leaves = progressTracker.countLeaves();
        this.beforeTask = earlyReloadTask(type, manager, progressTracker);
        this.lazyInstance = Suppliers.memoize(factory::get);
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
        float maxAmount = Mth.clamp(leaves / 100f, 0, 0.5f);
        float progress = progressTracker.getProgress() * maxAmount;
        if (!beforeTask.isDone()) {
            return progress;
        }
        ReloadInstance actual = allErrorsInPackReloadWillHaveThisLineOnTheirStackTrace_DoesntMeanItsTheCause();
        if (actual != null) {
            return progress + actual.getActualProgress() * (1 - maxAmount);
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
