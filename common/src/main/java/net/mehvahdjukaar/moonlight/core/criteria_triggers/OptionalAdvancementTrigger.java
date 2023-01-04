package net.mehvahdjukaar.moonlight.core.criteria_triggers;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class OptionalAdvancementTrigger extends SimpleCriterionTrigger<OptionalAdvancementTrigger.Instance> {

    private final ResourceLocation id;
    private final Predicate<String> predicate;

    public OptionalAdvancementTrigger(ResourceLocation id, Predicate<String> predicate) {
        this.id = id;
        this.predicate = predicate;
    }

    @Override
    public @NotNull ResourceLocation getId() {
        return id;
    }

    @Override
    public Instance createInstance(JsonObject json, EntityPredicate.Composite predicate, DeserializationContext deserializationContext) {
        String condition = json.get("flag").getAsString();
        return new Instance(predicate, condition);
    }

    public void trigger(ServerPlayer playerEntity, ItemStack stack) {
        this.trigger(playerEntity, (instance) -> predicate.test(instance.condition));
    }

    protected class Instance extends AbstractCriterionTriggerInstance {
        private final String condition;

        public Instance(EntityPredicate.Composite composite, String condition) {
            super(OptionalAdvancementTrigger.this.id, composite);
            this.condition = condition;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.addProperty("flag", this.condition);
            return jsonobject;
        }
    }
}
