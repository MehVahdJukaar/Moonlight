package net.mehvahdjukaar.moonlight.core.misc;

import com.mojang.datafixers.util.Unit;
import net.mehvahdjukaar.moonlight.api.events.EarlyPackReloadEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.misc.IProgressTracker;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ReloadInstance;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class ReloadInstanceWrapper implements ReloadInstance {

    public static ReloadInstance wrap(
            Supplier<ReloadInstance> factory,
            PackType type,
            ResourceManager manager,
            Executor backgroundExecutor,
            Executor mainExecutor,                     // <- add this
            Collection<PackResources> selectedPacks) {
        return new ReloadInstanceWrapper(factory, type, manager, backgroundExecutor, mainExecutor, selectedPacks);
    }

    public static void executeEarlyReloadBlocking(PackType type, ResourceManager manager,
                                                  IProgressTracker progressTracker, Collection<PackResources> selectedPacks) {
        MoonlightEventsHelper.postEvent(new EarlyPackReloadEvent(selectedPacks, manager, type, progressTracker), EarlyPackReloadEvent.class);
    }

    private final CompletableFuture<Unit> beforeTask;
    private final CompletableFuture<ReloadInstance> instanceFuture;
    private final IProgressTracker.Tree progressTracker;

    public ReloadInstanceWrapper(Supplier<ReloadInstance> factory,
                                 PackType type, ResourceManager manager,
                                 Executor backgroundExecutor,
                                 Executor mainExecutor,               // <- pass main executor in
                                 Collection<PackResources> selectedPacks) {

        this.progressTracker = IProgressTracker.createTree(1);

        this.beforeTask = CompletableFuture.supplyAsync(() -> {
            executeEarlyReloadBlocking(type, manager, progressTracker, selectedPacks);
            return Unit.INSTANCE;
        }, backgroundExecutor);

        // Ensure the actual instance is created on the main thread.
        this.instanceFuture = beforeTask.thenApplyAsync(u -> factory.get(), mainExecutor);
    }

    @Override
    public CompletableFuture<?> done() {
        return instanceFuture.thenCompose(ReloadInstance::done);
    }

    @Override
    public float getActualProgress() {
        if (!beforeTask.isDone()) return progressTracker.getProgress();
        ReloadInstance actual = instanceFuture.getNow(null);
        return actual != null ? actual.getActualProgress() : 1f;
    }

    @Override
    public void checkExceptions() {
        if (!beforeTask.isDone()) return;
        if (beforeTask.isCompletedExceptionally()) beforeTask.join();
        ReloadInstance actual = instanceFuture.getNow(null);
        if (actual != null) actual.checkExceptions();
    }
}
