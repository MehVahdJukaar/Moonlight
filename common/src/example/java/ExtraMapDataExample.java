import net.mehvahdjukaar.moonlight.api.map.CustomMapData;
import net.mehvahdjukaar.moonlight.api.map.MapDecorationRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

public class ExtraMapDataExample {

    public static void init(){}

    // Register your custom data type
    private static final CustomMapData.Type<MyCustomData> DEPTH_DATA_KEY = MapDecorationRegistry.registerCustomMapSavedData(
            Moonlight.res("my_data"), MyCustomData::new
    );

    // Use this to access it
    public static MyCustomData getData(MapItemSavedData data){
        return DEPTH_DATA_KEY.getOrCreate(data, MyCustomData::new);
    }


    private static class MyCustomData implements CustomMapData {

        private int value = 0;

        public MyCustomData(CompoundTag tag) {
            tag.getInt("my_data");
        }

        public MyCustomData(){}

        @Override
        public Type<MyCustomData> getType() {
            return DEPTH_DATA_KEY;
        }

        @Override
        public void save(CompoundTag tag) {
            tag.putInt("my_data", value);
        }

        @Override
        public @Nullable Component onItemTooltip(MapItemSavedData data, ItemStack stack) {
            return Component.literal("my data value is: "+ value).withStyle(ChatFormatting.GRAY);
        }

        public void set(int data) {
            this.value = data;
        }
    }
}
