import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDataRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class ExtraMapDataExample {

    public static void init() {
    }

    //sorry i havent updated this yet. Check out my impl in supplementaries for example for an updated version
    /*
    // Register your custom data type
    private static final CustomMapData.Type<Integer, MyCustomData> DEPTH_DATA_KEY = MapDataRegistry.registerCustomMapSavedData(
            Moonlight.res("my_data"), MyCustomData::new, ByteBufCodecs.INT);

    // Use this to access it
    public static MyCustomData getData(MapItemSavedData data) {
        return DEPTH_DATA_KEY.get(data);
    }


    public static class MyCustomData extends CustomMapData.Simple<Integer> {

        public MyCustomData() {
            // Set default value
            super(0);
        }

        public MyCustomData(int defaultValue) {
            super(defaultValue);
        }

        @Override
        public Type<Integer, ?> getType() {
            return DEPTH_DATA_KEY;
        }

        @Override
        public void save(CompoundTag tag, HolderLookup.Provider lookup) {
            tag.putInt("my_data", value);
        }

        @Override
        public @Nullable Component onItemTooltip(MapItemSavedData data, ItemStack stack) {
            return Component.literal("my data value is: " + value).withStyle(ChatFormatting.GRAY);
        }

        @Override
        public void load(CompoundTag tag, HolderLookup.Provider lookup) {
            this.value = tag.getInt("my_data");
        }

        public void set(int data) {
            this.value = data;
        }
    }*/
}
