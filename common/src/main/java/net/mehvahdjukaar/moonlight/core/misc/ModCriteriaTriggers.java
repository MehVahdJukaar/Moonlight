package net.mehvahdjukaar.moonlight.core.misc;

import com.google.gson.JsonObject;
import net.mehvahdjukaar.moonlight.core.mixins.accessor.CriteriaTriggerAccessor;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModCriteriaTriggers {

    public static void register(){}

    public static final GrindedItem GRIND = CriteriaTriggerAccessor.invokeRegister(new GrindedItem());
    
    public static class GrindedItem extends SimpleCriterionTrigger<GrindedItem.Instance> {
        private static final ResourceLocation ID = new ResourceLocation("grind_item");

        @Override
        public @NotNull ResourceLocation getId() {
            return ID;
        }

        @Override
        public Instance createInstance(JsonObject json, EntityPredicate.Composite predicate, DeserializationContext p_230241_3_) {
            ItemPredicate itempredicate = ItemPredicate.fromJson(json.get("item"));
            return new Instance(predicate, itempredicate);
        }

        public void trigger(ServerPlayer playerEntity, ItemStack stack) {
            this.trigger(playerEntity, (instance) -> instance.matches(stack));
        }

        public static class Instance extends AbstractCriterionTriggerInstance {
            private final ItemPredicate item;

            public Instance(EntityPredicate.Composite p_i231585_1_, ItemPredicate item) {
                super(GrindedItem.ID, p_i231585_1_);
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


}
