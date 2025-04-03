package net.mehvahdjukaar.moonlight.api.resources.recipe.forge;

import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.api.resources.recipe.BlockTypeSwapIngredient;
import net.mehvahdjukaar.moonlight.api.set.BlockType;
import net.mehvahdjukaar.moonlight.api.set.BlockTypeRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.mehvahdjukaar.moonlight.core.set.BlockSetInternal;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.common.crafting.AbstractIngredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BlockTypeSwapIngredientImpl<T extends BlockType> extends AbstractIngredient implements BlockTypeSwapIngredient {

    private final Ingredient inner;
    private final T fromType;
    private final T toType;
    private final BlockTypeRegistry<T> registry;

    private ItemStack[] items;

    public BlockTypeSwapIngredientImpl(Ingredient inner, T fromType, T toType, BlockTypeRegistry<T> registry) {
        super();
        Preconditions.checkNotNull(fromType, "From block type cannot be null");
        Preconditions.checkNotNull(toType, "To block type cannot be null");
        this.inner = inner;
        this.fromType = fromType;
        this.toType = toType;
        this.registry = registry;
    }

    @Override
    public Ingredient getInner() {
        return inner;
    }

    @Override
    public boolean isSimple() {
        return false;
    }

    @Override
    public List<ItemStack> convertItems(List<ItemStack> toConvert) {
        List<ItemStack> newItems = new ArrayList<>();
        boolean success = false;
        for (ItemStack it : toConvert) {
            var type = this.registry.getBlockTypeOf(it.getItem());
            if (type != this.fromType) {
                break;
            } else {
                var newItem = BlockType.changeItemType(it.getItem(), this.fromType, this.toType);
                if (newItem != null) {
                    newItems.add(new ItemStack(newItem));
                    success = true;
                }
            }
        }
        if (!success) {
            newItems.addAll(toConvert);
        }
        return newItems;
    }

    @Override
    public ItemStack[] getItems() {
        if (this.items == null) {
            var original = this.inner.getItems();
            this.items = convertItems(Arrays.asList(original)).toArray(new ItemStack[0]);
        }
        return this.items;
    }

    @Override
    protected void invalidate() {
        super.invalidate();
        this.items = null;
    }

    @Override
    public boolean isEmpty() {
        return this.inner.isEmpty();
    }

    @Override
    public IIngredientSerializer<BlockTypeSwapIngredientImpl<?>> getSerializer() {
        return SERIALIZER;
    }

    private static final IIngredientSerializer<BlockTypeSwapIngredientImpl<?>> SERIALIZER =
            new IIngredientSerializer<>() {
                @Override
                public BlockTypeSwapIngredientImpl<?> parse(FriendlyByteBuf arg) {
                    var reg = BlockSetInternal.getRegistry(arg.readUtf());
                    String fromWood = arg.readUtf();
                    var from = reg.getFromNBT(fromWood);
                    Preconditions.checkNotNull(from, "Found null block type with name: " + fromWood );
                    String toWood = arg.readUtf();
                    var to = reg.getFromNBT(toWood);
                    Preconditions.checkNotNull(to, "Found null block type with name: " + toWood );

                    var ing = Ingredient.fromNetwork(arg);
                    return new BlockTypeSwapIngredientImpl<>(ing, from, to, (BlockTypeRegistry) reg);
                }

                @Override
                public BlockTypeSwapIngredientImpl<?> parse(JsonObject jsonObject) {
                    var reg = BlockSetInternal.getRegistry(jsonObject.get("block_type").getAsString());
                    String fromWood = jsonObject.get("from").getAsString();
                    var from = reg.getFromNBT(fromWood);
                    Preconditions.checkNotNull(from, "Found null block type with name: " + fromWood );
                    String toWood = jsonObject.get("to").getAsString();
                    var to = reg.getFromNBT(toWood);
                    Preconditions.checkNotNull(to, "Found null block type with name: " + toWood );
                    var ing = Ingredient.fromJson(jsonObject.get("ingredient"));
                    return new BlockTypeSwapIngredientImpl<>(ing, from, to, (BlockTypeRegistry) reg);
                }

                @Override
                public void write(FriendlyByteBuf buf, BlockTypeSwapIngredientImpl ing) {
                    buf.writeUtf(ing.registry.typeName());
                    buf.writeUtf(ing.fromType.getId().toString());
                    buf.writeUtf(ing.toType.getId().toString());
                    ing.inner.toNetwork(buf);
                }
            };

    @Override
    public JsonElement toJson() {
        var obj = new JsonObject();
        obj.addProperty("block_type", this.registry.typeName());
        obj.addProperty("from", this.fromType.getId().toString());
        obj.addProperty("to", this.toType.getId().toString());
        obj.add("ingredient", this.inner.toJson());
        obj.addProperty("type", CraftingHelper.getID(SERIALIZER).toString());
        return obj;
    }

    public static <T extends BlockType> Ingredient create(Ingredient original, @NotNull T from,@NotNull T to) {
        return new BlockTypeSwapIngredientImpl<>(original, from, to, from.getRegistry());
    }

    public static void init() {
        CraftingHelper.register(Moonlight.res("block_type_swap"), SERIALIZER);
    }

}
