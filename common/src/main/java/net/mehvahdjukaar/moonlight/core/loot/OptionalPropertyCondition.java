package net.mehvahdjukaar.moonlight.core.loot;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.mehvahdjukaar.moonlight.api.MoonlightRegistry;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class OptionalPropertyCondition implements LootItemCondition {
    @Nullable
    final Block block;
    final StatePropertiesPredicate properties;
    private final ResourceLocation blockId;

    OptionalPropertyCondition(ResourceLocation blockId, Block block, StatePropertiesPredicate predicate) {
        this.properties = predicate;
        this.block = block;
        this.blockId = blockId;
    }

    @Override
    public LootItemConditionType getType() {
        return MoonlightRegistry.LAZY_PROPERTY.get();
    }

    @Override
    public Set<LootContextParam<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (block == null) return false;
        BlockState blockState = lootContext.getParamOrNull(LootContextParams.BLOCK_STATE);
        return blockState != null && blockState.is(this.block) && this.properties.matches(blockState);
    }

    public static class ConditionSerializer implements Serializer<OptionalPropertyCondition> {
        @Override
        public void serialize(JsonObject jsonObject, OptionalPropertyCondition condition, JsonSerializationContext context) {
            jsonObject.addProperty("block", condition.blockId.toString());
            jsonObject.add("properties", condition.properties.serializeToJson());
        }

        @Override
        public OptionalPropertyCondition deserialize(JsonObject jsonObject, JsonDeserializationContext context) {
            ResourceLocation resourceLocation = new ResourceLocation(GsonHelper.getAsString(jsonObject, "block"));
            Block block = BuiltInRegistries.BLOCK.getOptional(resourceLocation).orElse(null);
            StatePropertiesPredicate predicate = null;
            if (block != null) {
                predicate = StatePropertiesPredicate.fromJson(jsonObject.get("properties"));
                predicate.checkState(block.getStateDefinition(), string -> {
                    throw new JsonSyntaxException("Block " + block + " has no property " + string);
                });
            }
            return new OptionalPropertyCondition(resourceLocation, block, predicate);
        }

    }
}
