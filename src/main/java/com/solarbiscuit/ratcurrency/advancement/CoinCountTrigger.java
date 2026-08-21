package com.solarbiscuit.ratcurrency.advancement;

import com.google.gson.JsonObject;
import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.item.CoinTier;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

import java.util.Locale;

public class CoinCountTrigger extends SimpleCriterionTrigger<CoinCountTrigger.TriggerInstance> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(RatCurrency.MOD_ID, "coin_count");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
        CoinTier tier = CoinTier.valueOf(GsonHelper.getAsString(json, "tier").toUpperCase(Locale.ROOT));
        int min = GsonHelper.getAsInt(json, "min");
        return new TriggerInstance(player, tier, min);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> instance.matches(player));
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        private final CoinTier tier;
        private final int min;

        public TriggerInstance(ContextAwarePredicate player, CoinTier tier, int min) {
            super(ID, player);
            this.tier = tier;
            this.min = min;
        }

        public boolean matches(ServerPlayer player) {
            return CoinHelper.countCoins(player, this.tier) >= this.min;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext context) {
            JsonObject json = super.serializeToJson(context);
            json.addProperty("tier", this.tier.name().toLowerCase(Locale.ROOT));
            json.addProperty("min", this.min);
            return json;
        }
    }
}
