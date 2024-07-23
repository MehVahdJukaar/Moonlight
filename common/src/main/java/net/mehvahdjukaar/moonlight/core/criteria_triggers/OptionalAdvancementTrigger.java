package net.mehvahdjukaar.moonlight.core.criteria_triggers;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

//TODO: register and finish
public class OptionalAdvancementTrigger extends SimpleCriterionTrigger<OptionalAdvancementTrigger.Instance> {

    private final ResourceLocation id;
    private final Predicate<String> predicate;

    public OptionalAdvancementTrigger(ResourceLocation id, Predicate<String> predicate) {
        this.id = id;
        this.predicate = predicate;
    }

    public void trigger(ServerPlayer playerEntity, ItemStack stack) {
        this.trigger(playerEntity, (instance) -> predicate.test(instance.condition));
    }

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public record Instance(Optional<ContextAwarePredicate> player,String condition)  implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.fieldOf("flag").forGetter(Instance::condition)
        ).apply(instance, Instance::new));

    }
}