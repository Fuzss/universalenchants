package fuzs.universalenchants.world.item.enchantment.serialize.entry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;

import java.util.Locale;

public enum EnchantmentDataKey {
    ITEMS(Registries.ITEM), ANVIL_ITEMS(Registries.ITEM), INCOMPATIBLE(Registries.ENCHANTMENT);

    public final ResourceKey<Registry<?>> registryKey;

    @SuppressWarnings("unchecked")
    EnchantmentDataKey(ResourceKey<?> registryKey) {
        this.registryKey = (ResourceKey<Registry<?>>) registryKey;
    }

    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
