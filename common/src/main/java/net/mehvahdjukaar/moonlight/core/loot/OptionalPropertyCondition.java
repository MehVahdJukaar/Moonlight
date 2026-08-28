package net.mehvahdjukaar.moonlight.core.loot;

import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.criterion.StatePropertiesPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.context.ContextKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public class OptionalPropertyCondition implements LootItemCondition {

    public static final MapCodec<OptionalPropertyCondition> CODEC = RecordCodecBuilder.<OptionalPropertyCondition>mapCodec((i) -> i.group(
            Identifier.CODEC.fieldOf("block").forGetter(o -> o.blockId),
            StatePropertiesPredicate.CODEC.optionalFieldOf("properties").forGetter(o -> o.properties)
    ).apply(i, OptionalPropertyCondition::new)).validate(OptionalPropertyCondition::validate);


    @Nullable
    protected final Block block;
    protected final Optional<StatePropertiesPredicate> properties;
    protected final Identifier blockId;

    OptionalPropertyCondition(Identifier blockId, Optional<StatePropertiesPredicate> predicate) {
        this.properties = predicate;
        this.block = BuiltInRegistries.BLOCK.getOptional(blockId).orElse(null);
        this.blockId = blockId;
    }

    @Override
    public MapCodec<? extends LootItemCondition> codec() {
        return CODEC;
    }

    @Override
    public Set<ContextKey<?>> getReferencedContextParams() {
        return ImmutableSet.of(LootContextParams.BLOCK_STATE);
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (block == null) return false;
        BlockState blockState = lootContext.getOptionalParameter(LootContextParams.BLOCK_STATE);
        return blockState != null && blockState.is(this.block) && (properties.isEmpty() || this.properties.get().matches(blockState));
    }


    private static DataResult<OptionalPropertyCondition> validate(OptionalPropertyCondition condition) {
        if (condition.block != null) {
            return condition.properties.flatMap((prop) -> prop.checkState(condition.block.getStateDefinition()))
                    .map((string) -> DataResult.<OptionalPropertyCondition>error(() -> "Block " + condition + " has no property" + string))
                    .orElse(DataResult.success(condition));
        }
        return DataResult.success(condition);
    }
}
