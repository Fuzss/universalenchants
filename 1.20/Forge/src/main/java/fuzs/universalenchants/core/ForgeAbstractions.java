package fuzs.universalenchants.core;

import fuzs.universalenchants.world.item.enchantment.serialize.entry.DataEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.common.ForgeHooks;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ForgeAbstractions implements CommonAbstractions {

    @Override
    public boolean isArrowInfinite(LivingEntity entity, ItemStack rangedStack, ItemStack arrowStack) {
        return arrowStack.getItem() instanceof ArrowItem item && entity instanceof Player player && item.isInfinite(arrowStack, rangedStack, player);
    }

    @Override
    public EnchantmentCategory createEnchantmentCategory(String enumConstantName, Predicate<Item> predicate) {
        return EnchantmentCategory.create(enumConstantName, predicate);
    }

    @Override
    public int getMobLootingLevel(Entity target, @Nullable Entity killer, @Nullable DamageSource cause) {
        return ForgeHooks.getLootingLevel(target, killer, cause);
    }

    @Override
    public DataEntry.BuilderHolder getDefaultEnchantmentDataBuilder(Enchantment enchantment) {
        DataEntry.BuilderHolder holder = DataEntry.getDefaultBuilder(enchantment);
        // Forge has IForgeItem::canApplyAtEnchantingTable method for making an item compatible with enchantments outside the Enchantment#category
        // to honor this we need to find all those additional enchantments and add them manually (this means configs will have to be recreated when such mods are added)
        // example: Farmer's Delight's skillet item
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack itemStack = new ItemStack(item);
            if (enchantment.canEnchant(itemStack)) {
                holder.anvilBuilder().add(item);
            }
            if (item.canApplyAtEnchantingTable(itemStack, enchantment)) {
                holder.categoryBuilder().add(item);
            }
        }
        return holder;
    }
}
