package net.mehvahdjukaar.selene.block_set.wood;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.selene.block_set.BlockTypeRegistry;
import net.mehvahdjukaar.selene.resourcepack.AfterLanguageLoadEvent;
import net.mehvahdjukaar.selene.resourcepack.DynamicLanguageManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class WoodTypeRegistry extends BlockTypeRegistry<WoodType> {

    public static WoodTypeRegistry INSTANCE;

    public WoodTypeRegistry() {
        INSTANCE = this;
    }

    /**
     * Do not access these to register your blocks since they are empty right before the last registration phase.
     * Use addWoodEntryRegistrationCallback instead
     */
    public static Map<ResourceLocation, WoodType> WOOD_TYPES = new LinkedHashMap<>();

    public static WoodType fromNBT(String name) {
        return WOOD_TYPES.getOrDefault(new ResourceLocation(name), WoodType.OAK_WOOD_TYPE);
    }

    @Override
    public WoodType getDefaultType() {
        return WoodType.OAK_WOOD_TYPE;
    }

    @Override
    public Map<ResourceLocation, WoodType> getTypes() {
        if (!frozen) {
            throw new UnsupportedOperationException("Tried to access wood types too early");
        }
        return WOOD_TYPES;
    }

    @Override
    protected void saveTypes(ImmutableMap<ResourceLocation, WoodType> types) {
        WOOD_TYPES = types;
    }

    //returns if this block is the base plank block
    @Override
    public Optional<WoodType> detectTypeFromBlock(Block baseBlock) {
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
        if (name != null && !baseRes.getNamespace().equals("securitycraft")) {
            BlockState state = baseBlock.defaultBlockState();
            //cant check if the block is a full one so I do this. Adding some checks here
            if (state.getProperties().size() <= 2 && !(baseBlock instanceof SlabBlock)) {
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


    @Override
    public void addTypeTranslations(AfterLanguageLoadEvent language) {
        WOOD_TYPES.forEach((r, w) -> {
            if (language.isDefault()) language.addEntry(w.getTranslationKey(), w.getReadableName());
        });
    }
}
