package com.solarbiscuit.ratcurrency.event;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.enchantment.OpulenceEnchantment;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = RatCurrency.MOD_ID)
public class MobDropHandler {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType().getCategory() != MobCategory.MONSTER) {
            return;
        }
        if (RatCurrencyConfig.get(RatCurrencyConfig.REQUIRE_PLAYER_KILL)) {
            if (!(entity.getKillCredit() instanceof Player)) {
                return;
            }
        }
        if (entity.getRandom().nextDouble() > RatCurrencyConfig.get(RatCurrencyConfig.DROP_CHANCE)) {
            return;
        }
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
        boolean vanilla = id != null && "minecraft".equals(id.getNamespace());
        double categoryChance = vanilla
                ? RatCurrencyConfig.get(RatCurrencyConfig.VANILLA_HOSTILE_DROP_CHANCE)
                : RatCurrencyConfig.get(RatCurrencyConfig.MODDED_HOSTILE_DROP_CHANCE);
        if (entity.getRandom().nextDouble() > categoryChance) {
            return;
        }
        long units = rollUnits(entity);
        units = applyOpulence(event.getSource().getEntity(), units, entity);
        for (ItemStack drop : CoinHelper.denominate(units)) {
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), drop));
        }
    }

    private static long rollUnits(LivingEntity entity) {
        int min = RatCurrencyConfig.get(RatCurrencyConfig.MIN_COPPER);
        int max = Math.max(min, RatCurrencyConfig.get(RatCurrencyConfig.MAX_COPPER));
        return min + entity.getRandom().nextInt(max - min + 1);
    }

    private static long applyOpulence(Entity attacker, long units, LivingEntity dropped) {
        if (!(attacker instanceof LivingEntity living)) {
            return units;
        }
        return OpulenceEnchantment.applyBonus(units, living.getMainHandItem(), dropped.getRandom());
    }
}
