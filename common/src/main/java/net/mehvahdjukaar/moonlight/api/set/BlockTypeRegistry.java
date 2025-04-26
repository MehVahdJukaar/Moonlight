package net.mehvahdjukaar.moonlight.api.set;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.CompatHandler;
import net.mehvahdjukaar.moonlight.core.integration.PolymerCompat;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BlockTypeRegistry<T extends BlockType> {


    public static Codec<BlockTypeRegistry<?>> getRegistryCodec() {
        return BlockSetInternal.getRegistriesCodec();
    }

    public static StreamCodec<ByteBuf, BlockTypeRegistry<?>> getRegistryStreamCodec() {
        return BlockSetInternal.getRegistriesStreamCodec();
    }

    protected boolean frozen = false;
    private final String name;
    private final List<BlockType.SetFinder<T>> finders = new ArrayList<>();
    private final Set<ResourceLocation> notInclude = new HashSet<>();
    private final MapRegistry<T> valuesReg; //TODO: extend this instead
    private final Class<T> typeClass;
    private final Object2ObjectOpenHashMap<Object, T> childrenToType = new Object2ObjectOpenHashMap<>();
    private final StreamCodec<ByteBuf, T> streamCodecSlow;

    protected BlockTypeRegistry(Class<T> typeClass, String name) {
        this.typeClass = typeClass;
        this.name = name;
        this.valuesReg = new MapRegistry<>(name);
        this.streamCodecSlow = ByteBufCodecs.fromCodec(this.getCodec());
    }

    public Class<T> getType() {
        return typeClass;
    }

    /**
     * Gets corresponding block type or oak if the provided one is not installed or missing
     *
     * @param name string resource location name of the type
     * @return wood type
     */
    @Deprecated(forRemoval = true)
    public T getFromNBT(String name) {
        return valuesReg.getValueOrDefault(ResourceLocation.parse(name), this.getDefaultType());
    }

    @Nullable
    public T get(ResourceLocation res) {
        return valuesReg.getValue(res);
    }

    public ResourceLocation getKey(T input) {
        return valuesReg.getKey(input);
    }

    public Codec<T> getCodec() {
        return valuesReg;
    }

    public StreamCodec<FriendlyByteBuf, T> getStreamCodec() {
        return valuesReg.getStreamCodec();
    }


    public StreamCodec<ByteBuf, T> getStreamCodecExplicit() {
        return streamCodecSlow;
    }

    public abstract T getDefaultType();

    public Collection<T> getValues() {
        return valuesReg.getValues();
    }

    public String typeName() {
        return name;
    }

    /**
     * Returns an optional block Type based on the given block. Pretty much defines the logic of how a block set is constructed
     */
    public abstract Optional<T> detectTypeFromBlock(Block block, ResourceLocation blockId);

    public void registerBlockType(T newType) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register a block types after registry events");
        }
        //ignore duplicates
        if (!valuesReg.containsKey(newType.id)) {
            valuesReg.register(newType.id, newType);
        }
    }

    public Collection<BlockType.SetFinder<T>> getFinders() {
        return finders;
    }

    public void addFinder(BlockType.SetFinder<T> finder) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register a block type finder after registry events");
        }
        finders.add(finder);
    }

    public void addRemover(ResourceLocation id) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried remove a block type after registry events");
        }
        notInclude.add(id);
    }

    protected void finalizeAndFreeze() {
        if (frozen) {
            throw new UnsupportedOperationException("Block types are already finalized");
        }
        this.frozen = true;
    }

    @ApiStatus.Internal
    public void onBlockInit() {
        this.getValues().forEach(BlockType::initializeChildrenBlocks);
    }

    @ApiStatus.Internal
    public void onItemInit() {
        this.getValues().forEach(BlockType::initializeChildrenItems);
    }

    @ApiStatus.Internal
    public void buildAll() {
        if (!frozen) {
            //adds default
            this.registerBlockType(this.getDefaultType());
            //adds finders
            finders.stream().map(BlockType.SetFinder::get).forEach(f -> f.ifPresent(this::registerBlockType));
            for (Block b : BuiltInRegistries.BLOCK) {
                //skip stuff that wont be on the client
                if (CompatHandler.POLYMER && PolymerCompat.isPolymerObj(b)) continue;
                this.detectTypeFromBlock(b, Utils.getID(b)).ifPresent(t -> {
                    if (!notInclude.contains(t.getId())) this.registerBlockType(t);
                });
            }
            finders.clear();
            notInclude.clear();
            this.finalizeAndFreeze();
        }
    }

    /**
     * Called at the right time on language reload. Use to add translations of your block type names.
     * Useful to merge more complex translation strings using RPAwareDynamicTextureProvider::addDynamicLanguage
     */
    public void addTypeTranslations(AfterLanguageLoadEvent language) {

    }

    @Nullable
    public T getBlockTypeOf(ItemLike itemLike) {
        //we must check items and blocks correctly here since map might just contain blocks or items
        var t = childrenToType.get(itemLike);
        if (t != null) return t;
        if (itemLike == Items.AIR || itemLike == Blocks.AIR) return null;
        if (itemLike instanceof BlockItem bi) {
            var ofBlock = childrenToType.get(bi.getBlock());
            if (ofBlock != null) return ofBlock;
        }
        if (itemLike instanceof Block b) {
            Item item = b.asItem();
            if (item == Items.AIR) {
                throw new IllegalStateException("Block " + b + " has no item. This likely means getBlockTypeOf was called too early. This is a bug");
            }
            return childrenToType.get(item);
        }
        return null;
    }

    // we cant add items yet. item map has not been populated yet
    protected void mapObjectToType(Object itemLike, BlockType type) {
        this.childrenToType.put(itemLike, (T) type);
        if (itemLike instanceof BlockItem bi) {
            if (!this.childrenToType.containsKey(bi.getBlock()))
                this.childrenToType.put(bi.getBlock(), (T) type);
        }
    }

    // load priority. higher is loaded first. 100 is default
    public int priority() {
        return 100;
    }
}
