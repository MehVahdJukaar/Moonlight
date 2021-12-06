package net.mehvahdjukaar.selene.map;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MapDecorationHandler {

    public static final Map<String, CustomDataHolder<?>> CUSTOM_MAP_DATA_TYPES = new HashMap<>();

    private static final Map<String, CustomDecorationType<? extends CustomDecoration, ?>> DECORATION_TYPES = new HashMap<>();

    public static final CustomDecorationType<CustomDecoration, ?> GENERIC_STRUCTURE_TYPE = makeSimpleType(Selene.MOD_ID, "generic_structure");

    /**
     * registers a decoration type. use register simple for decoration that doesn't need a world marker
     *
     * @param newType new decoration type
     */
    public static <T extends CustomDecoration> void register(CustomDecorationType<T, ?> newType) {
        String id = newType.getRegistryId();
        if (DECORATION_TYPES.containsKey(id)) {
            throw new IllegalArgumentException("Duplicate map decoration registration " + id);
        } else {
            DECORATION_TYPES.put(id, newType);
        }
    }

    /**
     * creates & registers a simple decoration with no associated world marker.<br>
     * useful for exploration maps
     *
     * @param modId mod id
     * @param name  decoration name
     */
    public static void registerSimple(String modId, String name) {
        register(makeSimpleType(modId, name));
    }

    /**
     * creates a simple decoration type with no associated marker
     *
     * @param modId mod id
     * @param name  decoration name
     * @return newly created decoration type
     */
    public static CustomDecorationType<CustomDecoration, ?> makeSimpleType(String modId, String name) {
        return new CustomDecorationType<>(new ResourceLocation(modId, name), CustomDecoration::new);
    }

    @Nullable
    public static CustomDecorationType<?, ?> get(ResourceLocation id) {
        return get(id.toString());
    }

    @Nullable
    public static CustomDecorationType<? extends CustomDecoration, ?> get(String id) {
        return DECORATION_TYPES.get(id);
    }

    @Nullable
    public static MapWorldMarker<?> readWorldMarker(CompoundTag compound) {
        for (String s : DECORATION_TYPES.keySet()) {
            if (compound.contains(s)) {
                return DECORATION_TYPES.get(s).loadMarkerFromNBT(compound.getCompound(s));
            }
        }
        return null;
    }

    /**
     * returns a list of suitable world markers associated to a position. called by mixin code
     *
     * @param reader world
     * @param pos    world position
     * @return markers found, null if none found
     */
    public static List<MapWorldMarker<?>> getMarkersFromWorld(BlockGetter reader, BlockPos pos) {
        List<MapWorldMarker<?>> list = new ArrayList<>();
        for (CustomDecorationType<?, ?> type : DECORATION_TYPES.values()) {
            MapWorldMarker<?> c = type.getWorldMarkerFromWorld(reader, pos);
            if (c != null) list.add(c);
        }
        return list;
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
    public static void addTargetDecoration(ItemStack stack, BlockPos pos, CustomDecorationType<?, ?> type, int mapColor) {

        ListTag listnbt;
        if (stack.hasTag() && stack.getTag().contains("CustomDecorations", 9)) {
            listnbt = stack.getTag().getList("CustomDecorations", 10);
        } else {
            listnbt = new ListTag();
            stack.addTagElement("CustomDecorations", listnbt);
        }
        CompoundTag compoundnbt = new CompoundTag();
        compoundnbt.putString("type", type.getRegistryId());
        compoundnbt.putInt("x", pos.getX());
        compoundnbt.putInt("z", pos.getZ());
        listnbt.add(compoundnbt);
        if (mapColor != 0) {
            CompoundTag com = stack.getOrCreateTagElement("display");
            com.putInt("MapColor", mapColor);
        }

    }

    /**
     * adds a vanilla decoration
     *
     * @param stack    map item stack
     * @param pos      decoration world pos
     * @param type     vanilla decorationType
     * @param mapColor map item tint color
     */
    public static void addVanillaTargetDecorations(ItemStack stack, BlockPos pos, MapDecoration.Type type, int mapColor) {
        MapItemSavedData.addTargetDecoration(stack, pos, "+", type);
        if (mapColor != 0) {
            CompoundTag com = stack.getOrCreateTagElement("display");
            com.putInt("MapColor", mapColor);
        }
    }

    /**
     * see addTargetDecoration
     *
     * @param id decoration type id. if invalid will default to generic structure decoration
     */
    public static void addTargetDecoration(ItemStack stack, BlockPos pos, ResourceLocation id, int mapColor) {
        if (id.getNamespace().equals("minecraft")) {
            Optional<MapDecoration.Type> opt = Arrays.stream(MapDecoration.Type.values()).filter(t -> t.toString().toLowerCase().equals(id.getPath())).findFirst();
            if (opt.isPresent()) {
                addVanillaTargetDecorations(stack, pos, opt.get(), mapColor);
                return;
            }
        }
        CustomDecorationType<?, ?> type = DECORATION_TYPES.getOrDefault(id.toString(), GENERIC_STRUCTURE_TYPE);
        addTargetDecoration(stack, pos, type, mapColor);
    }


    /**
     * Registers a custom data type to be stored in map data
     *
     * @param name          id
     * @param type          data type class
     * @param load          load function
     * @param save          save function
     * @param onItemUpdate  callback for when a map item is updated
     * @param onItemTooltip callback on map item tooltip
     * @param <T>           data type
     */
    public static <T> void registerCustomMapSavedData(String name, Class<T> type,
                                                      Function<CompoundTag, T> load,
                                                      BiConsumer<CompoundTag, T> save,
                                                      PropertyDispatch.TriFunction<MapItemSavedData, Entity, T, Boolean> onItemUpdate,
                                                      PropertyDispatch.TriFunction<MapItemSavedData, ItemStack, T, Component> onItemTooltip) {
        if (CUSTOM_MAP_DATA_TYPES.containsKey("name")) {
            throw new IllegalArgumentException("Duplicate custom map data registration " + name);
        } else {
            CUSTOM_MAP_DATA_TYPES.put(name, new CustomDataHolder<>(name, load, save, onItemUpdate, onItemTooltip));
        }
    }


}
