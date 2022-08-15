package fuzs.universalenchants;

import fuzs.puzzleslib.core.CoreServices;
import fuzs.universalenchants.api.event.entity.living.LivingEntityUseItemEvents;
import fuzs.universalenchants.api.event.entity.living.LivingExperienceDropCallback;
import fuzs.universalenchants.api.event.entity.living.LivingHurtCallback;
import fuzs.universalenchants.api.event.entity.living.LootingLevelCallback;
import fuzs.universalenchants.api.event.entity.player.ArrowLooseCallback;
import fuzs.universalenchants.api.event.world.FarmlandTrampleCallback;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import fuzs.universalenchants.handler.ItemCompatHandler;
import fuzs.universalenchants.init.FabricModRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class UniversalEnchantsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        CoreServices.FACTORIES.modConstructor(UniversalEnchants.MOD_ID).accept(new UniversalEnchants());
        FabricModRegistry.touch();
        registerHandlers();
    }

    private static void registerHandlers() {
        ItemCompatHandler itemCompatHandler = new ItemCompatHandler();
        ArrowLooseCallback.EVENT.register((Player player, ItemStack bow, Level level, int charge, boolean hasAmmo) -> {
            itemCompatHandler.onArrowLoose(player, bow, level, charge, hasAmmo);
            return Optional.empty();
        });
        LivingEntityUseItemEvents.TICK.register(itemCompatHandler::onItemUseTick);
        LootingLevelCallback.EVENT.register(itemCompatHandler::onLootingLevel);
        BetterEnchantsHandler betterEnchantsHandler = new BetterEnchantsHandler();
        UseItemCallback.EVENT.register(betterEnchantsHandler::onArrowNock);
        UseItemCallback.EVENT.register(betterEnchantsHandler::onRightClickItem);
        LivingHurtCallback.EVENT.register(betterEnchantsHandler::onLivingHurt);
        FarmlandTrampleCallback.EVENT.register(betterEnchantsHandler::onFarmlandTrample);
        LivingExperienceDropCallback.EVENT.register(betterEnchantsHandler::onLivingExperienceDrop);
    }
}
