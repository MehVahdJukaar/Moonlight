package net.mehvahdjukaar.moonlight.api.map;

import net.mehvahdjukaar.moonlight.api.integration.MapAtlasCompat;
import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

import static net.mehvahdjukaar.moonlight.core.CompatHandler.MAP_ATLASES;

public class MapHelper {

    @Nullable
    public static MapItemSavedData getMapData(ItemStack stack, Level level, @Nullable Player player) {
        MapItemSavedData data;
        data = MapItem.getSavedData(stack, level);
        if (data == null && MAP_ATLASES && player != null)
            data = MapAtlasCompat.getSavedDataFromAtlas(stack, level, player);
        return data;
    }

    /**
     * adds a vanilla decoration
     *
     * @param stack    map item stack
     * @param pos      decoration world pos
     * @param type     vanilla decorationType
     * @param mapColor map item tint color
     */
    public static void addVanillaDecorations(ItemStack stack, BlockPos pos, MapDecoration.Type type, int mapColor) {
        MapItemSavedData.addTargetDecoration(stack, pos, "+", type);
        if (mapColor != 0) {
            CompoundTag com = stack.getOrCreateTagElement("display");
            com.putInt("MapColor", mapColor);
        }
    }

    /**
     * Adds a static decoration tp a map itemstack NBT.<br>
     * Such decoration will not have any world marker associated and wont be toggleable
     *
     * @param stack    map item stack
     * @param pos      decoration world pos
     * @param type     custom decorationType
     * @param mapColor map item tint color
     */
    public static void addDecorationToStack(ItemStack stack, BlockPos pos, MapDecorationType<?, ?> type, int mapColor) {

        ListTag tags;
        if (stack.hasTag() && stack.getTag().contains("CustomDecorations", 9)) {
            tags = stack.getTag().getList("CustomDecorations", 10);
        } else {
            tags = new ListTag();
            stack.addTagElement("CustomDecorations", tags);
        }
        CompoundTag tag = new CompoundTag();
        tag.putString("type", Utils.getID(type).toString());
        tag.putInt("x", pos.getX());
        tag.putInt("z", pos.getZ());
        tags.add(tag);
        if (mapColor != 0) {
            CompoundTag com = stack.getOrCreateTagElement("display");
            com.putInt("MapColor", mapColor);
        }
    }

    /**
     * see addDecorationToMap
     * This is useful when you don't have a reference to a map decoration object as it couldbe one that has been added with datapack
     *
     * @param id decoration type id. if invalid will default to generic structure decoration
     */
    public static void addDecorationToStack(ItemStack stack, BlockPos pos, ResourceLocation id, int mapColor) {
        if (id.getNamespace().equals("minecraft")) {
            MapDecoration.Type type = getVanillaType(id);
            if (type != null) {
                addVanillaDecorations(stack, pos, type, mapColor);
                return;
            }
        }
        MapDecorationType<?, ?> type = MapDataRegistry.get(id);
        if (type != null) {
            addDecorationToStack(stack, pos, type, mapColor);
        } else {
            addVanillaDecorations(stack, pos, MapDecoration.Type.TARGET_X, mapColor);
        }
    }

    @Nullable
    private static MapDecoration.Type getVanillaType(ResourceLocation id) {
        return Arrays.stream(MapDecoration.Type.values()).filter(t -> t.toString().toLowerCase().equals(id.getPath())).findFirst()
                .orElse(null);
    }

    /**
     * Adds all the map markers that can originate from the block at a given position
     */
    public static boolean toggleMarkersAtPos(Level level, BlockPos pos, ItemStack stack, @Nullable Player player) {
        MapItemSavedData data = getMapData(stack, level, player);
        if (data instanceof ExpandedMapData expandedMapData) {
            return expandedMapData.toggleCustomDecoration(level, pos);
        }
        return false;
    }

    public static boolean removeAllCustomMarkers(Level level, ItemStack stack, @Nullable Player player) {
        MapItemSavedData data = getMapData(stack, level, player);
        if (data instanceof ExpandedMapData expandedMapData) {
            if (!level.isClientSide) {
                expandedMapData.resetCustomDecoration();
                return true;
            }
        }
        return false;
    }

    /**
     * Helper that map decoration directly to map data using a persistent map marker. Only supports moonlight markers
     */
    public static boolean addPersistentDecoration(MapItemSavedData data, Level level, ResourceLocation id,
                                                  BlockPos pos, @Nullable Component name) {
        MapDecorationType<?, ?> type = MapDataRegistry.get(id);
        if (type != null) {
            var marker = type.createEmptyMarker();
            marker.setPersistent(true);
            marker.setPos(pos);
            marker.setName(name);
            ((ExpandedMapData) data).addCustomMarker(marker);
            return true;
        }
        return false;
    }

}
