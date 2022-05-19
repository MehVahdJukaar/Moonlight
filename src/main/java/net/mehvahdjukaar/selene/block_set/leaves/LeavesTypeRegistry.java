package net.mehvahdjukaar.selene.block_set.leaves;

import com.google.common.collect.ImmutableMap;
import net.mehvahdjukaar.selene.block_set.BlockTypeRegistry;
import net.mehvahdjukaar.selene.block_set.wood.WoodTypeRegistry;
import net.mehvahdjukaar.selene.resourcepack.DynamicLanguageManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class LeavesTypeRegistry extends BlockTypeRegistry<LeavesType> {

    public static LeavesTypeRegistry INSTANCE;

    public LeavesTypeRegistry() {
        INSTANCE = this;
    }

    /**
     * Do not access these to register your blocks since they are empty right before the last registration phase.
     * Use addWoodEntryRegistrationCallback instead
     */
    public static Map<ResourceLocation, LeavesType> LEAVES_TYPES = new LinkedHashMap<>();

    public static LeavesType fromNBT(String name) {
        return LEAVES_TYPES.getOrDefault(new ResourceLocation(name), LeavesType.OAK_LEAVES_TYPE);
    }

    @Override
    public LeavesType getDefaultType() {
        return LeavesType.OAK_LEAVES_TYPE;
    }

    @Override
    public Map<ResourceLocation, LeavesType> getTypes() {
        if (!frozen) {
            throw new UnsupportedOperationException("Tried to access wood types too early");
        }
        return LEAVES_TYPES;
    }

    @Override
    protected void saveTypes(ImmutableMap<ResourceLocation, LeavesType> types) {
        LEAVES_TYPES = types;
    }

    //returns if this block is the base plank block
    @Override
    public Optional<LeavesType> detectTypeFromBlock(@NotNull Block baseBlock) {
        ResourceLocation baseRes = baseBlock.getRegistryName();
        String name = null;
        String path = baseRes.getPath();
        //needs to contain planks in its name
        if (path.endsWith("_leaves")) {
            name = path.substring(0, path.length() - "_leaves".length());
        } else if (path.startsWith("leaves_")) {
            name = path.substring("leaves_".length());
        }
        if (name != null && !baseRes.getNamespace().equals("securitycraft")) {
            if (baseBlock instanceof LeavesBlock) {
                BlockState state = baseBlock.defaultBlockState();
                Material mat = state.getMaterial();
                //and have correct material
                if (mat == Material.LEAVES) {
                    ResourceLocation id = new ResourceLocation(baseRes.getNamespace(), name);

                    return Optional.of(new LeavesType(id, baseBlock, null));
                }
            }
            //}
        }
        return Optional.empty();
    }

    @Override
    public void buildAll() {
        if (!frozen) {
            for (var v : WoodTypeRegistry.WOOD_TYPES.values()) {
                if (v.leaves != null) {
                    this.registerBlockType(new LeavesType(v.id, v.leaves, v));
                }
            }
        }
        super.buildAll();
    }


    @Override
    public void addTypeTranslations(DynamicLanguageManager.LanguageAccessor language) {
        LEAVES_TYPES.forEach((r, w) -> {
            if (language.isDefault()) language.addEntry(w.getTranslationKey(), w.getReadableName());
        });
    }
}
