package net.mehvahdjukaar.moonlight.api.set;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.mehvahdjukaar.moonlight.api.events.AfterLanguageLoadEvent;
import net.mehvahdjukaar.moonlight.api.misc.MapRegistry;
import net.mehvahdjukaar.moonlight.api.platform.PlatHelper;
import net.mehvahdjukaar.moonlight.api.set.wood.WoodType;
import net.mehvahdjukaar.moonlight.api.util.INamedSupplier;
import net.mehvahdjukaar.moonlight.api.util.Utils;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.core.IdMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class BlockTypeRegistry<T extends BlockType>implements IdMap<T> {

    public static Codec<BlockTypeRegistry<?>> getRegistryCodec() {
        return BlockSetInternal.getRegistriesCodec();
    }

    protected boolean frozen = false;
    private final String name;
    private final List<BlockType.SetFinder<T>> finders = new ArrayList<>();
    private final Set<ResourceLocation> notInclude = new HashSet<>();
    protected final MapRegistry<T> valuesReg;
    private final Class<T> typeClass;
    private final Object2ObjectOpenHashMap<Object, T> childrenToType = new Object2ObjectOpenHashMap<>();

    protected BlockTypeRegistry(Class<T> typeClass, String name) {
        this.typeClass = typeClass;
        this.name = name;
        this.valuesReg = new MapRegistry<>(name);
    }

    @Override
    public @NotNull Iterator<T> iterator() {
        return valuesReg.iterator();
    }

    @Override
    public int size() {
        return valuesReg.size();
    }

    @Override
    public @Nullable T byId(int id) {
        return valuesReg.byId(id);
    }

    @Override
    public int getId(T value) {
        return valuesReg.getId(value);
    }

    public boolean isFrozen() {
        return frozen;
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
        return valuesReg.getValueOrDefault(new ResourceLocation(name), this.getDefaultType());
    }

    @Nullable
    public T get(ResourceLocation res) {
        if (!frozen && (!isBeingFrozenHack || PlatHelper.isDev())) {
            throw new AssertionError("Tried to get an object from block set registry before the registry was finalized.");
        }
        return valuesReg.getValue(res);
    }

    public ResourceLocation getKey(T input) {
        return valuesReg.getKey(input);
    }

    public Codec<T> getCodec() {
        return valuesReg;
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
    @ApiStatus.Internal
    protected abstract Optional<T> detectTypeFromBlock(Block block, ResourceLocation blockId);

    @ApiStatus.Internal
    protected T register(T newType) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register a wood types after registry events");
        }
        //ignore duplicates
        if (!valuesReg.containsKey(newType.id)) {
            valuesReg.register(newType.id, newType);
        }
        return newType;
    }

    public Collection<BlockType.SetFinder<T>> getFinders() {
        return finders;
    }

    public synchronized void addFinder(BlockType.SetFinder<T> finder) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried to register a block type finder after registry events");
        }
        finders.add(finder);
    }

    public synchronized void addRemover(ResourceLocation id) {
        if (frozen) {
            throw new UnsupportedOperationException("Tried remove a block type after registry events");
        }
        notInclude.add(id);
    }

    @ApiStatus.Internal
    public void finalizeAndFreeze() {
        if (frozen) {
            throw new UnsupportedOperationException("Block types are already finalized");
        }
        this.frozen = true;


        this.getValues().forEach(BlockType::initializeChildrenBlocks);
        this.getValues().forEach(BlockType::initializeChildrenItems);
    }

    @Deprecated(forRemoval = true)
    boolean isBeingFrozenHack = false;

    @ApiStatus.Internal
    public void buildAll() {
        if (!frozen) {
            //adds default
            T defaultType = this.getDefaultType();
            if (defaultType != null) this.register(defaultType);
            //adds finders
            finders.stream().map(BlockType.SetFinder::get).forEach(f -> f.ifPresent(this::register));
            isBeingFrozenHack = true;
            for (Block b : BuiltInRegistries.BLOCK) {
                this.detectTypeFromBlock(b, Utils.getID(b)).ifPresent(t -> {
                    if (!notInclude.contains(t.getId())) this.register(t);
                });
            }
            isBeingFrozenHack = false;
            finders.clear();
            notInclude.clear();
        }

    }

    /**
     * Called at the right time on language reload. Use to add translations of your block type names.
     * Useful to merge more complex translation strings using RPAwareDynamicTextureProvider::addDynamicLanguage
     */
    @ApiStatus.Internal
    public void addTypeTranslations(AfterLanguageLoadEvent language) {
        this.getValues().forEach((w) -> {
            if (language.isDefault()) language.addEntry(w.getTranslationKey(), w.getReadableName());
        });
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
                return null;
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

    public INamedSupplier<T> makeFutureHolder(ResourceLocation id) {
        return INamedSupplier.memoize(id, () -> this.get(id));
    }
}
