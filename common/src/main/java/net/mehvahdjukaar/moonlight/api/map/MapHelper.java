package net.mehvahdjukaar.moonlight.api.map;

import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.api.integration.MapAtlasCompat;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapDecorationType;
import net.mehvahdjukaar.moonlight.api.map.decoration.MLMapMarker;
import net.mehvahdjukaar.moonlight.api.map.decoration.SimpleMapMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

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
    public static void addVanillaTargetDecorationToItem(ItemStack stack, BlockPos pos, Holder<net.minecraft.world.level.saveddata.maps.MapDecorationType> type, int mapColor) {
        MapItemSavedData.addTargetDecoration(stack, pos, "+", type);
        if (mapColor != 0) {
            //custom map color overrides the deco one
            stack.set(DataComponents.MAP_COLOR, new MapItemColor(mapColor));
        }
    }

    /**
     * Adds a static decoration to a map itemstack NBT.<br>
     * Such decoration will not have any world marker associated and wont be toggleable
     *
     * @param stack    map item stack
     * @param pos      decoration world pos
     * @param type     custom decorationType
     * @param mapColor map item tint color
     */
    public static void addCustomTargetDecorationToItem(ItemStack stack, BlockPos pos, Holder<MLMapDecorationType<?, ?>> type, int mapColor) {
        MLMapDecorationsComponent customDecoMap = stack.getOrDefault(MoonlightRegistry.CUSTOM_MAP_DECORATIONS.get(),
                MLMapDecorationsComponent.EMPTY);
        MLMapMarker<?> marker = new SimpleMapMarker(type, pos, 0f, Optional.empty());
        customDecoMap = customDecoMap.copyAndAdd(marker);
        stack.set(MoonlightRegistry.CUSTOM_MAP_DECORATIONS.get(), customDecoMap);
        if (mapColor != 0) {
            stack.set(DataComponents.MAP_COLOR, new MapItemColor(mapColor));
        }
    }

    /**
     * see addDecorationToMap
     * This is useful when you don't have a reference to a map decoration object as it could one that has been added with datapack
     * If it fails to find the decoration type it will default to a target_x decoration
     *
     * @param id decoration type id. if invalid will default to generic structure decoration
     */
    public static void addTargetDecorationToItem(ItemStack stack, BlockPos pos, ResourceLocation id, int mapColor) {
        var vanillaType = BuiltInRegistries.MAP_DECORATION_TYPE.getHolder(id);
        if (vanillaType.isPresent()) {
            addVanillaTargetDecorationToItem(stack, pos, vanillaType.get(), mapColor);
            return;
        }
        var moddedType = MapDataRegistry.getHolder(id);
        if (moddedType != null) {
            addCustomTargetDecorationToItem(stack, pos, moddedType, mapColor);
        } else {
            addVanillaTargetDecorationToItem(stack, pos, MapDecorationTypes.TARGET_X, mapColor);
        }
    }

    /**
     * Adds all the map markers that can originate from the block at a given position
     */
    public static boolean toggleMarkersAtPos(Level level, BlockPos pos, ItemStack stack, @Nullable Player player) {
        MapItemSavedData data = getMapData(stack, level, player);
        if (data instanceof ExpandedMapData expandedMapData) {
            return expandedMapData.ml$toggleCustomDecoration(level, pos);
        }
        return false;
    }

    public static boolean removeAllCustomMarkers(Level level, ItemStack stack, @Nullable Player player) {
        MapItemSavedData data = getMapData(stack, level, player);
        if (data instanceof ExpandedMapData expandedMapData) {
            if (!level.isClientSide) {
                expandedMapData.ml$resetCustomDecoration();
                return true;
            }
        }
        return false;
    }

    /**
     * Helper that map decoration directly to map data using a persistent map marker. Only supports moonlight markers that have a simple marker type
     */
    public static boolean addSimpleDecorationToMap(MapItemSavedData data, Holder<MLMapDecorationType<?, ?>> type,
                                                   BlockPos pos, float rotation, @Nullable Component name) {
        //hack only works with these
        if (type.value().getMarkerCodec() == SimpleMapMarker.REFERENCE_CODEC) {
            var marker = new SimpleMapMarker(type, pos, rotation, Optional.ofNullable(name));
            ((ExpandedMapData) data).ml$addCustomMarker(marker);
            return true;
        }
        return false;
    }

}
