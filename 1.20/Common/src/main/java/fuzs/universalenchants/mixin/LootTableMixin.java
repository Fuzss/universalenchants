package fuzs.universalenchants.mixin;

import fuzs.universalenchants.util.EnchantmentHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.Map;

@Mixin(LootTable.class)
abstract class LootTableMixin {

    @ModifyVariable(method = "getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;", at = @At(value = "TAIL", shift = At.Shift.BEFORE))
    private ObjectArrayList<ItemStack> getRandomItems(ObjectArrayList<ItemStack> items, LootContext context) {
        ObjectListIterator<ItemStack> iterator = items.iterator();
        while (iterator.hasNext()) {
            ItemStack itemStack = iterator.next();
            if (itemStack.isEnchanted() || itemStack.getItem() instanceof EnchantedBookItem) {
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
                boolean result = enchantments.keySet().removeIf(enchantment -> {
                    return !((FeatureElement) enchantment).isEnabled(context.getLevel().enabledFeatures());
                });
                if (result) {
                    iterator.set(EnchantmentHelper.setEnchantments(enchantments, itemStack));
                }
            }
        }
        return items;
    }
}
