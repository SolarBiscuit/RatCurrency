package com.solarbiscuit.ratcurrency.event;

import com.solarbiscuit.ratcurrency.RatCurrency;
import com.solarbiscuit.ratcurrency.config.RatCurrencyConfig;
import com.solarbiscuit.ratcurrency.util.CoinHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = RatCurrency.MOD_ID)
public class MobDropHandler {
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getType().getCategory() != MobCategory.MONSTER) {
            return;
        }
        if (RatCurrencyConfig.REQUIRE_PLAYER_KILL.get()) {
            if (!(entity.getKillCredit() instanceof Player)) {
                return;
            }
        }
        if (entity.getRandom().nextDouble() > RatCurrencyConfig.DROP_CHANCE.get()) {
            return;
        }
        int min = RatCurrencyConfig.MIN_COPPER.get();
        int max = Math.max(min, RatCurrencyConfig.MAX_COPPER.get());
        int units = min + entity.getRandom().nextInt(max - min + 1);
        for (ItemStack drop : CoinHelper.denominate(units)) {
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), drop));
        }
    }
}
