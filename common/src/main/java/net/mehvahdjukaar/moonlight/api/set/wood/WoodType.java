package net.mehvahdjukaar.moonlight.api.set.wood;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class WoodType extends BlockType {

    public static final Codec<WoodType> CODEC = ResourceLocation.CODEC.flatXmap(r -> {
                WoodType w = WoodTypeRegistry.getValue(r);
                if (w == null) return DataResult.error(() -> "No such wood type: " + r);
                return DataResult.success(w);
            },
            t -> DataResult.success(t.id));

    public final Material material;
    public final Block planks;
    public final Block log;

    private final Supplier<net.minecraft.world.level.block.state.properties.WoodType> vanillaType = Suppliers.memoize(() -> {
        String i = (id.getNamespace().equals("minecraft") ? "" : id.getNamespace() + "/") + id.getPath();
        var o = net.minecraft.world.level.block.state.properties.WoodType.values().filter(v -> v.name().equals(i)).findAny();
        return o.orElse(null);
    });

    protected WoodType(ResourceLocation id, Block baseBlock, Block logBlock) {
        super(id);
        this.planks = baseBlock;
        this.log = logBlock;
        this.material = baseBlock.defaultBlockState().getMaterial();


    }

    @Nullable
    protected Block findLogRelatedBlock(String append, String postpend) {
        if (this.id.getNamespace().equals("tfc")) {
            var o = BuiltInRegistries.BLOCK.getOptional(
                    new ResourceLocation(id.getNamespace(),
                            "wood/" + append + "_" + postpend + "/" + id.getPath()));
            if (o.isPresent()) return o.get();
        }

        String post = postpend.isEmpty() ? "" : "_" + postpend;
        var id = this.getId();
        String logN = Utils.getID(this.log).getPath();
        ResourceLocation[] targets = {
                new ResourceLocation(id.getNamespace(), logN + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + logN + post),
                new ResourceLocation(id.getNamespace(), id.getPath() + "_" + append + post),
                new ResourceLocation(id.getNamespace(), append + "_" + id.getPath() + post)
        };
        Block found = null;
        for (var r : targets) {
            if (BuiltInRegistries.BLOCK.containsKey(r)) {
                found = BuiltInRegistries.BLOCK.get(r);
                break;
            }
        }
        return found;
    }

    public Material getMaterial() {
        return material;
    }

    @Override
    public ItemLike mainChild() {
        return planks;
    }

    @Nullable
    public net.minecraft.world.level.block.state.properties.WoodType toVanilla() {
        return this.vanillaType.get();
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

    @Override
    public String getTranslationKey() {
        return "wood_type." + this.getNamespace() + "." + this.getTypeName();
    }

    @Override
    public void initializeChildrenBlocks() {
        this.addChild("planks", this.planks);
        this.addChild("log", this.log);
        this.addChild("leaves", this.findRelatedEntry("leaves", BuiltInRegistries.BLOCK));
        this.addChild("stripped_log", this.findLogRelatedBlock("stripped", "log"));
        this.addChild("stripped_wood", this.findLogRelatedBlock("stripped", "wood"));
        this.addChild("wood", this.findRelatedEntry("wood", BuiltInRegistries.BLOCK));
        this.addChild("slab", this.findRelatedEntry("slab", BuiltInRegistries.BLOCK));
        this.addChild("stairs", this.findRelatedEntry("stairs", BuiltInRegistries.BLOCK));
        this.addChild("fence", this.findRelatedEntry("fence", BuiltInRegistries.BLOCK));
        this.addChild("fence_gate", this.findRelatedEntry("fence_gate", BuiltInRegistries.BLOCK));
        this.addChild("door", this.findRelatedEntry("door", BuiltInRegistries.BLOCK));
        this.addChild("trapdoor", this.findRelatedEntry("trapdoor", BuiltInRegistries.BLOCK));
        this.addChild("button", this.findRelatedEntry("button", BuiltInRegistries.BLOCK));
        this.addChild("pressure_plate", this.findRelatedEntry("pressure_plate", BuiltInRegistries.BLOCK));
    }

    @Override
    public void initializeChildrenItems() {
        this.addChild("boat", this.findRelatedEntry("boat", BuiltInRegistries.ITEM));
        this.addChild("sign", this.findRelatedEntry("sign", BuiltInRegistries.ITEM));
    }

    public static class Finder implements SetFinder<WoodType> {

        private final Map<String, ResourceLocation> childNames = new HashMap<>();
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
                    () -> BuiltInRegistries.BLOCK.get(planksName),
                    () -> BuiltInRegistries.BLOCK.get(logName));
        }

        public void addChild(String childType, String childName) {
            addChild(childType, new ResourceLocation(id.getNamespace(), childName));
        }

        public void addChild(String childType, ResourceLocation childName) {
            this.childNames.put(childType, childName);
        }

        @ApiStatus.Internal
        @Override
        public Optional<WoodType> get() {
            if (PlatHelper.isModLoaded(id.getNamespace())) {
                try {
                    Block plank = planksFinder.get();
                    Block log = logFinder.get();
                    var d = BuiltInRegistries.BLOCK.get(BuiltInRegistries.BLOCK.getDefaultKey());
                    if (plank != d && log != d && plank != null && log != null) {
                        var w = new WoodType(id, plank, log);
                        childNames.forEach((key, value) -> w.addChild(key, (Object) BuiltInRegistries.BLOCK.get(value)));
                        return Optional.of(w);
                    }
                } catch (Exception ignored) {
                }
                Moonlight.LOGGER.warn("Failed to find custom wood type {}", id);
            }
            return Optional.empty();
        }
    }


}
