package net.mehvahdjukaar.moonlight.core.misc;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ModCriteriaTriggers {

    public static void register(){}

    public static final GrindItem GRIND = CriteriaTriggers.register(new GrindItem());
    
    public static class GrindItem extends SimpleCriterionTrigger<GrindItem.Instance> {
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

            public Instance(EntityPredicate.Composite composite, ItemPredicate item) {
                super(GrindItem.ID, composite);
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
