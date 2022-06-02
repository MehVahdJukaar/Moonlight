package net.mehvahdjukaar.selene.block_set.wood;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.block_set.BlockType;
import net.mehvahdjukaar.selene.block_set.IBlockType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.fml.IBindingsProvider;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class WoodType extends IBlockType {

    public static WoodType OAK_WOOD_TYPE = new WoodType(new ResourceLocation("oak"), Blocks.OAK_PLANKS, Blocks.OAK_LOG);

    public final Material material;


    public final Block planks;
    public final Block log;

    //additional optional blocks
    //TODO: remove
    @Nullable
    @Deprecated
    public final Block strippedLog;
    @Nullable
    @Deprecated
    public final Block leaves;
    //lazy cause wood types are loaded before items, so we can only access blocks
    @Deprecated
    public final Lazy<Item> signItem; //used for item textures
    @Deprecated
    public final Lazy<Item> boatItem;

    @Nullable
    private final net.minecraft.world.level.block.state.properties.WoodType vanillaType;

    protected WoodType(ResourceLocation id, Block baseBlock, Block logBlock) {
        super(id);
        this.planks = baseBlock;
        this.log = logBlock;
        this.material = baseBlock.defaultBlockState().getMaterial();

        this.strippedLog = this.findLogRelatedBlock("stripped","log");
        this.leaves = this.findRelatedEntry("leaves", ForgeRegistries.BLOCKS);

        //checks if it has a sign
        this.signItem = Lazy.of(() -> this.findRelatedEntry("sign", ForgeRegistries.ITEMS));
        this.boatItem = Lazy.of(() -> this.findRelatedEntry("boat", ForgeRegistries.ITEMS));
        String i = id.getNamespace().equals("minecraft") ? "" : id.getNamespace() + "/" + id.getPath();
        var o = net.minecraft.world.level.block.state.properties.WoodType.values().filter(v -> v.name().equals(i)).findAny();
        this.vanillaType = o.orElse(null);
    }

    @Nullable
    protected Block findLogRelatedBlock(String append, String postpend) {
        String post = postpend.isEmpty() ? "" : "_" + postpend;
        var id = this.getId();
        String log = this.log.getRegistryName().getPath();
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(),  log+ "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + log + post),
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + id.getPath() + post)
        };
        Block found = null;
        for (var r : targets) {
            if (ForgeRegistries.BLOCKS.containsKey(r)) {
                found = ForgeRegistries.BLOCKS.getValue(r);
                break;
            }
        }
        return found;
    }

    @Override
    public ItemLike mainChild() {
        return planks;
    }

    @Nullable
    public net.minecraft.world.level.block.state.properties.WoodType toVanilla() {
        return this.vanillaType;
    }

    /**
     * Use this to get the texture path of a wood type
     *
     * @return something like minecraft/oak
     */
    public String getTexturePath() {
        String namespace = this.getNamespace();
        if (namespace.equals("minecraft")) return this.getTypeName();
        return this.getNamespace() + "/" + this.getTypeName();
    }

    public boolean canBurn() {
        return this.material.isFlammable();
    }

    public MaterialColor getColor() {
        return this.material.getColor();
    }

    @Deprecated
    private static String abbreviateString(String string) {
        if (string.length() <= 5) return string;
        String[] a = string.split("_");
        if (a.length > 2) {
            return "" + a[0].charAt(0) + a[1].charAt(0) + a[2].charAt(0) + (a.length > 3 ? a[3].charAt(0) : "");
        } else if (a.length > 1) {
            return "" + a[0].substring(0, Math.min(2, a[0].length())) + a[1].substring(0, Math.min(2, a[0].length()));
        } else return string.substring(0, 4);
    }

    @Override
    public String getTranslationKey() {
        return "wood_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    protected void initializeChildren() {
        Block strippedWood = this.findLogRelatedBlock("stripped", "wood");
        Block wood = this.findRelatedEntry("wood", ForgeRegistries.BLOCKS);
        Block slab = this.findRelatedEntry("slab", ForgeRegistries.BLOCKS);
        Block stairs = this.findRelatedEntry("stairs", ForgeRegistries.BLOCKS);
        Block fence = this.findRelatedEntry("fence", ForgeRegistries.BLOCKS);
        Block fenceGate = this.findRelatedEntry("fence_gate", ForgeRegistries.BLOCKS);
        Block door = this.findRelatedEntry("door", ForgeRegistries.BLOCKS);
        Block trapdoor = this.findRelatedEntry("trapdoor", ForgeRegistries.BLOCKS);
        Block button = this.findRelatedEntry("button", ForgeRegistries.BLOCKS);
        Block pressurePlate = this.findRelatedEntry("pressure_plate", ForgeRegistries.BLOCKS);
        this.addChild("planks", this.planks);
        this.addChild("log", this.log);
        this.addChild("leaves", this.leaves);
        this.addChild("stripped_log", this.strippedLog);
        this.addChild("stripped_wood", strippedWood);
        this.addChild("wood", wood);
        this.addChild("slab", slab);
        this.addChild("stairs", stairs);
        this.addChild("fence", fence);
        this.addChild("fence_gate", fenceGate);
        this.addChild("door", door);
        this.addChild("trapdoor", trapdoor);
        this.addChild("button", button);
        this.addChild("pressure_plate", pressurePlate);
        this.addChild("sign", this.signItem.get());
        this.addChild("boat", this.boatItem.get());
    }

    public static class Finder extends SetFinder<WoodType> {

        private final Supplier<Block> planksFinder;
        private final Supplier<Block> logFinder;
        private final ResourceLocation id;

        public Finder(ResourceLocation id, Supplier<Block> planks, Supplier<Block> log) {
            this.id = id;
            this.planksFinder = planks;
            this.logFinder = log;
        }

        public static Finder simple(String modId, String woodTypeName, String planksName, String logName) {
            return simple(new ResourceLocation(modId, woodTypeName), new ResourceLocation(modId, planksName), new ResourceLocation(modId, logName));
        }

        public static Finder simple(ResourceLocation woodTypeName, ResourceLocation planksName, ResourceLocation logName) {
            return new Finder(woodTypeName,
                    () -> ForgeRegistries.BLOCKS.getValue(planksName),
                    () -> ForgeRegistries.BLOCKS.getValue(logName));
        }

        @Override
        public Optional<WoodType> get() {
            if (ModList.get().isLoaded(id.getNamespace())) {
                try {
                    Block plank = planksFinder.get();
                    Block log = logFinder.get();
                    var d = ForgeRegistries.BLOCKS.getValue(ForgeRegistries.BLOCKS.getDefaultKey());
                    if (plank != d && log != d && plank != null && log != null) {
                        return Optional.of(new WoodType(id, plank, log));
                    }
                } catch (Exception ignored) {
                }
                Selene.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }
    }


}
