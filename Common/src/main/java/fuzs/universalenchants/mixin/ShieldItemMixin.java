package fuzs.universalenchants.mixin;

import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
abstract class ShieldItemMixin extends Item {

    public ShieldItemMixin(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return UniversalEnchants.CONFIG.get(ServerConfig.class).enchantableShields ? 1 : 0;
    }
}
