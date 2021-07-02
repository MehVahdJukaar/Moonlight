package net.mehvahdjukaar.selene.common;

import com.google.gson.JsonObject;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.AbstractCriterionTrigger;
import net.minecraft.advancements.criterion.CriterionInstance;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.ConditionArrayParser;
import net.minecraft.loot.ConditionArraySerializer;
import net.minecraft.util.ResourceLocation;

public class ModCriteriaTriggers {

    public static void init(){}

    public static final GrindedItem GRIND = CriteriaTriggers.register(new GrindedItem());
    
    public static class GrindedItem extends AbstractCriterionTrigger<GrindedItem.Instance> {
        private static final ResourceLocation ID = new ResourceLocation("grind_item");

        @Override
        public ResourceLocation getId() {
            return ID;
        }

        @Override
        public GrindedItem.Instance createInstance(JsonObject json, EntityPredicate.AndPredicate predicate, ConditionArrayParser p_230241_3_) {
            ItemPredicate itempredicate = ItemPredicate.fromJson(json.get("item"));
            return new GrindedItem.Instance(predicate, itempredicate);
        }

        public void trigger(ServerPlayerEntity playerEntity, ItemStack stack) {
            this.trigger(playerEntity, (instance) -> instance.matches(stack));
        }

        public static class Instance extends CriterionInstance {
            private final ItemPredicate item;

            public Instance(EntityPredicate.AndPredicate p_i231585_1_, ItemPredicate item) {
                super(GrindedItem.ID, p_i231585_1_);
                this.item = item;
            }

            public boolean matches(ItemStack stack) {
                return this.item.matches(stack);
            }

            @Override
            public JsonObject serializeToJson(ConditionArraySerializer serializer) {
                JsonObject jsonobject = super.serializeToJson(serializer);
                jsonobject.add("item", this.item.serializeToJson());
                return jsonobject;
            }
        }
    }


}
