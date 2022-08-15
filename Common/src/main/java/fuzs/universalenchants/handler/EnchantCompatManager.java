package fuzs.universalenchants.handler;

import com.google.common.collect.Lists;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.core.Registry;
import net.minecraft.world.item.enchantment.DamageEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;

import java.util.List;

public class EnchantCompatManager {
    public static final EnchantCompatManager INSTANCE = new EnchantCompatManager();

    private final List<CompatibilityEntry> entries = Lists.newArrayList();

    public void init() {
        this.entries.clear();
        if (UniversalEnchants.CONFIG.get(ServerConfig.class).infinityMendingFix) {
            this.entries.add(new CompatibilityEntry(Enchantments.INFINITY_ARROWS, Enchantments.MENDING));
        }
        if (UniversalEnchants.CONFIG.get(ServerConfig.class).multishotPiercingFix) {
            this.entries.add(new CompatibilityEntry(Enchantments.MULTISHOT, Enchantments.PIERCING));
        }
        if (UniversalEnchants.CONFIG.get(ServerConfig.class).damageEnchantmentsFix) {
            Registry.ENCHANTMENT.forEach(enchantment -> {
                if (enchantment instanceof DamageEnchantment && enchantment != Enchantments.SHARPNESS) {
                    this.entries.add(new CompatibilityEntry(Enchantments.SHARPNESS, enchantment));
                }
            });
        }
        if (UniversalEnchants.CONFIG.get(ServerConfig.class).protectionEnchantmentsFix) {
            Registry.ENCHANTMENT.forEach(enchantment -> {
                if (enchantment instanceof ProtectionEnchantment && enchantment != Enchantments.ALL_DAMAGE_PROTECTION && enchantment != Enchantments.FALL_PROTECTION) {
                    this.entries.add(new CompatibilityEntry(Enchantments.ALL_DAMAGE_PROTECTION, enchantment));
                }
            });
        }
    }

    public boolean isCompatibleWith(Enchantment enchantment1, Enchantment enchantment2) {
        for (CompatibilityEntry entry : this.entries) {
            if (entry.validate(enchantment1, enchantment2)) {
                return true;
            }
        }
        return false;
    }

    private record CompatibilityEntry(Enchantment enchantment1, Enchantment enchantment2, boolean addRestrictions) {

        public CompatibilityEntry(Enchantment enchantment1, Enchantment enchantment2) {
            this(enchantment1, enchantment2, false);
        }

        public boolean validate(Enchantment enchantment1, Enchantment enchantment2) {
            if (enchantment1 == this.enchantment1) {
                return enchantment2 == this.enchantment2;
            }
            return enchantment2 == this.enchantment1 && enchantment1 == this.enchantment2;
        }
    }
}
