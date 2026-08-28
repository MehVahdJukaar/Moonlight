import com.mojang.serialization.Codec;
import net.mehvahdjukaar.moonlight.api.entity.VillagerAIHooks;
import net.mehvahdjukaar.moonlight.api.events.IVillagerBrainEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.mehvahdjukaar.moonlight.api.events.SimpleEvent;
import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.function.Supplier;

public class VillagersAIHooksExample {

    public static void init() {
        // Adds a callback to the event. MoonlightEventsHelper is ML-specific event helper class
        MoonlightEventsHelper.addListener(VillagersAIHooksExample::onBrainEvent, IVillagerBrainEvent.class);

        // shortcut call to do the same as above
        // VillagerAIHooks.addBrainModification(VillagersAIHooksExample::onBrainEvent);
    }

    // memory modules are registered normally, villagers pick them up from the sensors and activities added in the event
    public static final Supplier<MemoryModuleType<Integer>> CUSTOM_MEMORY = RegHelper.registerMemoryModule(
            Moonlight.res("custom_memory"), Codec.INT);

    // Custom event added by the mod. If you are on Forge, you can also use @SubscribeEvent annotation as it implements Event
    private static void onBrainEvent(IVillagerBrainEvent event) {

    }
}
