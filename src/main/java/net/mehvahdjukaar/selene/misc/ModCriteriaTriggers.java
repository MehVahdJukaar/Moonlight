package net.mehvahdjukaar.selene.misc;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class ModCriteriaTriggers {

    public static void init(){}

    public static final GrindedItem GRIND = CriteriaTriggers.register(new GrindedItem());
    
    public static class GrindedItem extends SimpleCriterionTrigger<GrindedItem.Instance> {
        private static final ResourceLocation ID = new ResourceLocation("grind_item");

        @Override
        public @NotNull ResourceLocation getId() {
            return ID;
        }

        @Override
        public GrindedItem.Instance createInstance(JsonObject json, EntityPredicate.Composite predicate, DeserializationContext p_230241_3_) {
            ItemPredicate itempredicate = ItemPredicate.fromJson(json.get("item"));
            return new GrindedItem.Instance(predicate, itempredicate);
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
