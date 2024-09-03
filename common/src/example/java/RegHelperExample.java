import net.mehvahdjukaar.moonlight.api.platform.RegHelper;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.function.Supplier;

// RegHelper is the main utility class for anything registry related
public class RegHelperExample {

    // Call this on mod init. Use to add callbacks for special events
    public static void init() {
        //IEventBus bus;
        //RegHelper.startRegisteringFor(bus);

        // These callbacks pretty much just wrap forge and fabric events. Just showcasing a few
        RegHelper.addItemsToTabsRegistration(RegHelperExample::registerItemsToTabs);
        RegHelper.addLootTableInjects(RegHelperExample::registerLootInjects);
    }

    protected static final Supplier<FlowerBlock> LILAC_FLOWER = RegHelper.registerBlockWithItem(
            Moonlight.res("lilac"), () -> new FlowerBlock(
                    MobEffects.HARM, 1, BlockBehaviour.Properties.of())
    );

    // Generic entry registration. Just like Registry.register calls
    protected static final Supplier<GameEvent> CUSTOM_EVENT = RegHelper.register(
            Moonlight.res("custom_event"), () -> new GameEvent(2),
            Registries.GAME_EVENT
    );

    // Register items to tabs
    private static void registerItemsToTabs(RegHelper.ItemToTabEvent event) {
        // Adds our flower after all existing ones
        event.addAfter(CreativeModeTabs.BUILDING_BLOCKS,
                stack -> stack.is(ItemTags.FLOWERS),
                LILAC_FLOWER.get());
    }

    // Adds diamond loot to stone block
    private static void registerLootInjects(RegHelper.LootInjectEvent event) {
        if (event.getTable().equals(ResourceLocation.parse("stone"))) {
            event.addTableReference(ResourceLocation.parse("diamond"));
        }
    }

}
