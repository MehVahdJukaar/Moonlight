package net.mehvahdjukaar.selene.block_set.wood;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.block_set.IBlockSetContainer;
import net.mehvahdjukaar.selene.block_set.IBlockType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WoodSetContainer implements IBlockSetContainer<WoodType> {

    /**
     * Do not access these to register your blocks since they are empty right before the last registration phase.
     * Use addWoodEntryRegistrationCallback instead
     */
    public static Map<ResourceLocation, WoodType> WOOD_TYPES = new LinkedHashMap<>();


    private boolean frozen = false;
    private final List<IBlockType.SetFinder<WoodType>> WOOD_FINDERS = new ArrayList<>();
    private final Set<WoodType> builder = new HashSet<>();

    /**
     * Gets corresponding wood type or oak if the provided one is not installed or missing
     *
     * @param name string resource location name of the type
     * @return wood type
     */
    public WoodType fromNBT(String name) {
        return WOOD_TYPES.getOrDefault(new ResourceLocation(name), this.getDefaultType());
    }

    @Override
    public WoodType getDefaultType() {
        return WoodType.OAK_WOOD_TYPE;
    }

    @Override
    public Map<ResourceLocation,WoodType> getTypes() {
        if (!frozen) {
            throw new UnsupportedOperationException("Tried to access wood types too early");
        }
        return WOOD_TYPES;
    }

    @Override
    public void registerBlockType(WoodType newType) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register wood types after registry events");
        }
        builder.add(newType);
    }

    @Override
    public void finalizeAndFreeze() {
        if (frozen) {
            throw new UnsupportedOperationException("Wood types are already finalized");
        }
        LinkedHashMap<ResourceLocation, WoodType> linkedHashMap = new LinkedHashMap<>();
        builder.forEach(e->{
            if(linkedHashMap.containsKey(e.id)){
                Selene.LOGGER.warn("Found wood type with duplicate id ({}), overriding",e.id);
            }
            linkedHashMap.put(e.id,e);
        });
        WOOD_TYPES = ImmutableMap.copyOf(linkedHashMap);
        builder.clear();
        this.frozen = true;
    }

    @Override
    public void addFinder(IBlockType.SetFinder<WoodType> finder) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register wood type finder after registry events");
        }
        WOOD_FINDERS.add(finder);
    }

    public List<IBlockType.SetFinder<WoodType>> getFinders() {
        return WOOD_FINDERS;
    }

    //returns if this block is the base plank block
    @Override
    public Optional<WoodType> scanAndGet(Block baseBlock) {
        ResourceLocation baseRes = baseBlock.getRegistryName();
        String name = null;
        String path = baseRes.getPath();
        //needs to contain planks in its name
        if (path.endsWith("_planks")) {
            name = path.substring(0, path.length() - "_planks".length());
        } else if (path.startsWith("planks_")) {
            name = path.substring("planks_".length());
        } else if (path.endsWith("_plank")) {
            name = path.substring(0, path.length() - "_plank".length());
        } else if (path.startsWith("plank_")) {
            name = path.substring("plank_".length());
        }
        if (name != null) {
            BlockState state = baseBlock.defaultBlockState();
            //needs to use wood sound type
            //if (state.getSoundType() == SoundType.WOOD) { //wood from tcon has diff sounds
            Material mat = state.getMaterial();
            //and have correct material
            if (mat == Material.WOOD || mat == Material.NETHER_WOOD) {
                ResourceLocation id = new ResourceLocation(baseRes.getNamespace(), name);
                Block logBlock = findLog(id);
                if (logBlock != null) {
                    return Optional.of(new WoodType(id, baseBlock, logBlock));
                }
            }
            //}
        }
        return Optional.empty();
    }

    @Nullable
    private static Block findLog(ResourceLocation id) {
        ResourceLocation[] test = {
                new ResourceLocation(id.getNamespace(), id.getPath() + "_log"),
                new ResourceLocation(id.getNamespace(), "log_" + id.getPath()),
                new ResourceLocation(id.getPath() + "_log"),
                new ResourceLocation("log_" + id.getPath()),
                new ResourceLocation(id.getNamespace(), id.getPath() + "_stem"),
                new ResourceLocation(id.getNamespace(), "stem_" + id.getPath()),
                new ResourceLocation(id.getPath() + "_stem"),
                new ResourceLocation("stem_" + id.getPath())
        };
        Block temp = null;
        for (var r : test) {
            if (ForgeRegistries.BLOCKS.containsKey(r)) {
                temp = ForgeRegistries.BLOCKS.getValue(r);
                break;
            }
        }
        return temp;
    }


}
