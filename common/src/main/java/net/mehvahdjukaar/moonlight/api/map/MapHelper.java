package net.mehvahdjukaar.moonlight.api.map;

import net.mehvahdjukaar.moonlight.api.map.type.MapDecorationType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.api.integration.MapAtlasCompat;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class MapHelper {

    public static final boolean MAP_ATLASES = PlatHelper.isModLoaded("map_atlases");

    @Nullable
    public static MapItemSavedData getMapData(ItemStack stack, Level level, @Nullable Player player) {
        MapItemSavedData data;
        data = MapItem.getSavedData(stack, level);
        if (data == null && MAP_ATLASES && player != null) data = MapAtlasCompat.getSavedDataFromAtlas(stack, level, player);
        return data;
    }

    @Deprecated(forRemoval = true)
    public static Integer getMapId(ItemStack stack, Player player, Object data) {
        Integer i = MapItem.getMapId(stack);
        if (i == null && MAP_ATLASES) i = MapAtlasCompat.getMapIdFromAtlas(stack, player.level(), data);
        return i;
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
    public static void addDecorationToMap(ItemStack stack, BlockPos pos, MapDecorationType<?, ?> type, int mapColor) {

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
    public static void addDecorationToMap(ItemStack stack, BlockPos pos, ResourceLocation id, int mapColor) {
        if (id.getNamespace().equals("minecraft")) {
            Optional<MapDecoration.Type> opt = Arrays.stream(MapDecoration.Type.values()).filter(t -> t.toString().toLowerCase().equals(id.getPath())).findFirst();
            if (opt.isPresent()) {
                addVanillaDecorations(stack, pos, opt.get(), mapColor);
                return;
            }
        }
        MapDecorationType<?, ?> type = MapDecorationRegistry.get(id.toString());
        if (type != null) {
            addDecorationToMap(stack, pos, type, mapColor);
        } else {
            addVanillaDecorations(stack, pos, MapDecoration.Type.TARGET_X, mapColor);
        }
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

}
