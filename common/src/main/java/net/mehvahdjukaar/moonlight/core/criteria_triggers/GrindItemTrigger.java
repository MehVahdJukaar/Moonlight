package net.mehvahdjukaar.moonlight.core.criteria_triggers;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class GrindItemTrigger extends SimpleCriterionTrigger<GrindItemTrigger.Instance> {

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer playerEntity, ItemStack stack) {
        this.trigger(playerEntity, (instance) -> instance.matches(stack));
    }

    public record Instance(Optional<ContextAwarePredicate> player,
                           ItemPredicate item) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(Instance::player),
                ItemPredicate.CODEC.fieldOf("item").forGetter(Instance::item)
        ).apply(instance, Instance::new));

        public boolean matches(ItemStack stack) {
            return this.item.matches(stack);
        }

    }
}
