package fuzs.universalenchants;

import fuzs.puzzleslib.api.config.v3.ConfigHolder;
import fuzs.puzzleslib.api.core.v1.ModConstructor;
import fuzs.puzzleslib.api.core.v1.utility.ResourceLocationHelper;
import fuzs.puzzleslib.api.event.v1.FinalizeItemComponentsCallback;
import fuzs.puzzleslib.api.event.v1.core.EventPhase;
import fuzs.puzzleslib.api.event.v1.entity.living.*;
import fuzs.puzzleslib.api.event.v1.level.BlockEvents;
import fuzs.puzzleslib.api.event.v1.server.TagsUpdatedCallback;
import fuzs.universalenchants.config.ServerConfig;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import fuzs.universalenchants.handler.ItemCompatHandler;
import fuzs.universalenchants.init.ModRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.equipment.Equippable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Consumer;
import java.util.function.Function;

public class UniversalEnchants implements ModConstructor {
    public static final String MOD_ID = "universalenchants";
    public static final String MOD_NAME = "Universal Enchants";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final ConfigHolder CONFIG = ConfigHolder.builder(MOD_ID).server(ServerConfig.class);

    @Override
    public void onConstructMod() {
        ModRegistry.bootstrap();
        registerEventHandlers();
    }

    private static void registerEventHandlers() {
        FinalizeItemComponentsCallback.EVENT.register((Item item, Consumer<Function<DataComponentMap, DataComponentPatch>> consumer) -> {
            if (item instanceof ShearsItem || item instanceof ShieldItem) {
                consumer.accept((DataComponentMap components) -> {
                    return DataComponentPatch.builder().set(DataComponents.ENCHANTABLE, new Enchantable(1)).build();
                });
            } else {
                consumer.accept((DataComponentMap components) -> {
                    if (!components.has(DataComponents.ENCHANTABLE)) {
                        Equippable equippable = components.get(DataComponents.EQUIPPABLE);
                        if (equippable != null && equippable.slot() == EquipmentSlot.BODY) {
                            ItemAttributeModifiers itemAttributeModifiers = components.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS,
                                    ItemAttributeModifiers.EMPTY);
                            double defenseValue = itemAttributeModifiers.modifiers()
                                    .stream()
                                    .filter((ItemAttributeModifiers.Entry entry) -> entry.attribute()
                                            .is(Attributes.ARMOR))
                                    .map(ItemAttributeModifiers.Entry::modifier)
                                    .mapToDouble(AttributeModifier::amount)
                                    .sum();
                            return DataComponentPatch.builder()
                                    .set(DataComponents.ENCHANTABLE,
                                            new Enchantable(Math.max(1, Mth.ceil(defenseValue))))
                                    .build();
                        }
                    }

                    return DataComponentPatch.EMPTY;
                });
            }
        });
        TagsUpdatedCallback.EVENT.register(ItemCompatHandler::onTagsUpdated);
        UseItemEvents.TICK.register(ItemCompatHandler::onUseItemTick);
        ComputeEnchantedLootBonusCallback.EVENT.register(ItemCompatHandler::onComputeEnchantedLootBonus);
        LivingHurtCallback.EVENT.register(BetterEnchantsHandler::onLivingHurt);
        BlockEvents.FARMLAND_TRAMPLE.register(BetterEnchantsHandler::onFarmlandTrample);
        ShieldBlockCallback.EVENT.register(ItemCompatHandler::onShieldBlock);
        // run after other mods had a chance to change looting level
        LivingExperienceDropCallback.EVENT.register(EventPhase.AFTER, BetterEnchantsHandler::onLivingExperienceDrop);
        BlockEvents.DROP_EXPERIENCE.register(EventPhase.AFTER, BetterEnchantsHandler::onDropExperience);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocationHelper.fromNamespaceAndPath(MOD_ID, path);
    }
}
