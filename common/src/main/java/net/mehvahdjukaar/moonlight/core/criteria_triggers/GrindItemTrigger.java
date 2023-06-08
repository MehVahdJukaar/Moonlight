package net.mehvahdjukaar.moonlight.core.criteria_triggers;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.Moonlight;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class GrindItemTrigger extends SimpleCriterionTrigger<GrindItemTrigger.Instance> {
    private static final ResourceLocation ID = Moonlight.res("grind_item");

    @Override
    public @NotNull ResourceLocation getId() {
        return ID;
    }

    @Override
    public Instance createInstance(JsonObject json, ContextAwarePredicate predicate, DeserializationContext deserializationContext) {
        ItemPredicate itempredicate = ItemPredicate.fromJson(json.get("item"));
        return new Instance(predicate, itempredicate);
    }

    public void trigger(ServerPlayer playerEntity, ItemStack stack) {
        this.trigger(playerEntity, (instance) -> instance.matches(stack));
    }

    protected static class Instance extends AbstractCriterionTriggerInstance {
        private final ItemPredicate item;

        public Instance(ContextAwarePredicate contextAwarePredicate, ItemPredicate item) {
            super(GrindItemTrigger.ID, contextAwarePredicate);
            this.item = item;
        }


        public boolean matches(ItemStack stack) {
            return this.item.matches(stack);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializer) {
            JsonObject jsonobject = super.serializeToJson(serializer);
            jsonobject.add("item", this.item.serializeToJson());
            return jsonobject;
        }
    }
}
