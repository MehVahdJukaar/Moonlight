package net.mehvahdjukaar.moonlight.api.set.wood;

import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class WoodTypeRegistry extends BlockTypeRegistry<WoodType> {

    public static final WoodTypeRegistry INSTANCE = new WoodTypeRegistry();
    @Deprecated(forRemoval = true)
    public static final WoodType OAK_TYPE = VanillaWoodTypes.OAK;

    @Deprecated(forRemoval = true)
    public static Collection<WoodType> getTypes() {
        return INSTANCE.getValues();
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public static WoodType getValue(ResourceLocation woodTypeId) {
        return INSTANCE.get(woodTypeId);
    }

    @Deprecated(forRemoval = true)
    @Nullable
    public static WoodType getValue(String woodTypeId) {
        return INSTANCE.get(new ResourceLocation(woodTypeId));
    }

    @Deprecated(forRemoval = true)
    public static WoodType fromNBT(String name) {
        return INSTANCE.getFromNBT(name);
    }

    @Deprecated(forRemoval = true)
    public static WoodType fromVanilla(net.minecraft.world.level.block.state.properties.WoodType vanillaType) {
        return INSTANCE.getFromVanilla(vanillaType);
    }

    //instance stuff

    private final Map<net.minecraft.world.level.block.state.properties.WoodType, WoodType> fromVanilla = new IdentityHashMap<>();

    public WoodTypeRegistry() {
        super(WoodType.class, "wood_type");
        this.addFinder(() -> {
            var b = new WoodType(new ResourceLocation("bamboo"), Blocks.BAMBOO_PLANKS, Blocks.BAMBOO_BLOCK);
            b.addChild("stripped_log", Blocks.STRIPPED_BAMBOO_BLOCK);
            return Optional.of(b);
        });
    }

    //manual registry method. Really only for vanilla
    @Override
    protected WoodType register(WoodType newType) {
       return super.register(newType);
    }

    @Override
    public WoodType getDefaultType() {
        return VanillaWoodTypes.OAK;
    }

    //- BLACKLISTED_MODS
    public static Set<String> BLACKLISTED_MODS = new HashSet<>(Set.of(
            "chipped", "compressedblocks", "securitycraft",
            "absentbydesign", "immersive_weathering", "dynamictrees", "dt"
    ));

    //returns if this block is the base plank block
    @Override
    public Optional<WoodType> detectTypeFromBlock(Block baseBlock, ResourceLocation baseRes) {
        String name = null;
        String path = baseRes.getPath();
        // Support TerraFirmaCraft (TFC) & ArborFirmaCraft (AFC)
        if (baseRes.getNamespace().equals("tfc") || baseRes.getNamespace().equals("afc")) {
            // Needs to contain palnks in its path
            if (path.contains("wood/planks/")) {
                var log = BuiltInRegistries.BLOCK.getOptional(
                        new ResourceLocation(baseRes.getNamespace(), path.replace("planks", "log")));
                if (log.isPresent()) {
                    ResourceLocation id = new ResourceLocation(baseRes.getNamespace(), path.replace("wood/planks/", ""));
                    return Optional.of(new WoodType(id, baseBlock, log.get()));
                }
            }
            return Optional.empty();
        }
        /// DEFAULT
        if (path.endsWith("_planks")) { //needs to contain planks in its name
            name = path.substring(0, path.length() - "_planks" .length());
        } else if (path.startsWith("planks_")) {
            name = path.substring("planks_" .length());
        } else if (path.endsWith("_plank")) {
            name = path.substring(0, path.length() - "_plank" .length());
        } else if (path.startsWith("plank_")) {
            name = path.substring("plank_" .length());
        }
        String namespace = baseRes.getNamespace();
        if (name != null && !BLACKLISTED_MODS.contains(namespace)) {

            BlockState state = baseBlock.defaultBlockState();
            //Can't check if the block is a full one, so I do this. Adding some checks here
            if (state.getProperties().size() <= 2 && !(baseBlock instanceof SlabBlock)) {
                //needs to use wood sound type
                //we do not allow "/" in the wood name
                name = name.replace("/", "_");
                ResourceLocation id = new ResourceLocation(baseRes.getNamespace(), name);
                Block logBlock = WoodType.findLog(id);
                if (logBlock != null && Objects.isNull(get(id))) {
                    return Optional.of(new WoodType(id, baseBlock, logBlock));
                }
            }
        }
        return Optional.empty();
    }

    @Nullable
    public WoodType getFromVanilla(net.minecraft.world.level.block.state.properties.WoodType woodType) {
        if (fromVanilla.isEmpty()) {
            for (WoodType w : getValues()) {
                var vanilla = w.toVanilla();
                if (vanilla != null) fromVanilla.put(vanilla, w);
            }
        }
        return fromVanilla.get(woodType);
    }

    //shorthand for add finder. Gives a builder-like object that's meant to be configured inline
    public WoodType.Finder addSimpleFinder(ResourceLocation typeId) {
        WoodType.Finder finder = new WoodType.Finder(typeId);
        this.addFinder(finder);
        return finder;
    }


    public WoodType.Finder addSimpleFinder(String typeId) {
        return addSimpleFinder(new ResourceLocation(typeId));
    }

    public WoodType.Finder addSimpleFinder(String namespace, String name) {
        return addSimpleFinder(new ResourceLocation(namespace, name));
    }
}
