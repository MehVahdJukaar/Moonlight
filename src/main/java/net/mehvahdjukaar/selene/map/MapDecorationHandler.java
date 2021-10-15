package net.mehvahdjukaar.selene.map;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.map.markers.MapWorldMarker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapDecorationHandler {
    private static final Map<String, CustomDecorationType<?,?>> DECORATION_TYPES = new HashMap<>();

    public static final CustomDecorationType<?,?> GENERIC_STRUCTURE_TYPE = makeSimpleType(Selene.MOD_ID, "generic_structure");

    /**
     * registers a decoration type. use register simple for decoration that doesn't need a world marker
     * @param newType new decoration type
     */
    public static void register(CustomDecorationType<?,?> newType){
        String id = newType.getRegistryId();
        if(DECORATION_TYPES.containsKey(id)){
            throw new IllegalArgumentException("Duplicate map decoration registration " + id);
        }
        else {
            DECORATION_TYPES.put(id, newType);
        }
    }

    /**
     * creates & registers a simple decoration with no associated world marker.<br>
     * useful for exploration maps
     * @param modId mod id
     * @param name decoration name
     */
    public static void registerSimple(String modId, String name){
        register(makeSimpleType(modId,name));
    }

    /**
     * creates a simple decoration type with no associated marker
     * @param modId mod id
     * @param name decoration name
     * @return newly created decoration type
     */
    public static CustomDecorationType<?,?> makeSimpleType(String modId,String name){
        return new CustomDecorationType<>(new ResourceLocation(modId,name),CustomDecoration::new);
    }

    @Nullable
    public static CustomDecorationType<?,?> get(ResourceLocation id){
        return get(id.toString());
    }

    @Nullable
    public static CustomDecorationType<?,?> get(String id){
        return DECORATION_TYPES.get(id);
    }

    @Nullable
    public static MapWorldMarker<?> readWorldMarker(CompoundTag compound){
        for(String s : DECORATION_TYPES.keySet()){
            if(compound.contains(s)){
                return DECORATION_TYPES.get(s).loadMarkerFromNBT(compound.getCompound(s));
            }
        }
        return null;
    }

    /**
     * returns a list of suitable world markers associated to a position. called by mixin code
     * @param reader world
     * @param pos world position
     * @return markers found, null if none found
     */
    public static List<MapWorldMarker<?>> getMarkersFromWorld(BlockGetter reader, BlockPos pos){
        List<MapWorldMarker<?>> list = new ArrayList<>();
        for(CustomDecorationType<?,?> type : DECORATION_TYPES.values()){
            MapWorldMarker<?> c = type.getWorldMarkerFromWorld(reader,pos);
            if(c!=null)list.add(c);
        }
        return list;
    }

    /**
     * Adds a static decoration tp a map itemstack NBT.<br>
     * Such decoration will not have any world marker associated and wont be toggleable
     * @param stack map item stack
     * @param pos decoration world pos
     * @param type decorationType
     * @param mapColor map item tint color
     */
    public static void addTargetDecoration(ItemStack stack, BlockPos pos, CustomDecorationType<?,?> type, int mapColor) {

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
        if (mapColor!=0) {
            CompoundTag com = stack.getOrCreateTagElement("display");
            com.putInt("MapColor", mapColor);
        }

    }

    /**
     * see addTargetDecoration
     * @param id decoration type id. if invalid will default to generic structure decoration
     */
    public static void addTargetDecoration(ItemStack stack, BlockPos pos, ResourceLocation id, int mapColor) {
        CustomDecorationType<?,?> type = DECORATION_TYPES.getOrDefault(id.toString(),GENERIC_STRUCTURE_TYPE);
        addTargetDecoration(stack,pos,type,mapColor);
    }



}
