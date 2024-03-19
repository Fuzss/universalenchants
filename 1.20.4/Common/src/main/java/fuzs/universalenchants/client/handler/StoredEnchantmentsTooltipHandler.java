package fuzs.universalenchants.client.handler;

import fuzs.puzzleslib.api.core.v1.ModLoaderEnvironment;
import fuzs.universalenchants.util.StoredEnchantmentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class StoredEnchantmentsTooltipHandler {

    public static void onItemTooltip(ItemStack itemStack, @Nullable Player player, List<Component> lines, TooltipFlag context) {
        if (shouldShowInTooltip(getHideFlags(itemStack), ItemStack.TooltipPart.ENCHANTMENTS)) {
            if (StoredEnchantmentHelper.hasStoredEnchantments(itemStack)) {
                List<Map.Entry<Enchantment, Integer>> storedEnchantments = new ArrayList<>(EnchantmentHelper.deserializeEnchantments(
                        StoredEnchantmentHelper.getStoredEnchantments(itemStack)).entrySet());
                if (!storedEnchantments.isEmpty()) {
                    Collections.reverse(storedEnchantments);
                    int index = getLastEnchantmentIndex(lines);
                    for (Map.Entry<Enchantment, Integer> entry : storedEnchantments) {
                        getEnchantmentDescription(entry.getKey()).ifPresent(component -> lines.add(index, component));
                        lines.add(index, Component.empty().append(entry.getKey().getFullname(entry.getValue())).withStyle(ChatFormatting.STRIKETHROUGH));
                    }
                }
            }
        }
    }

    private static int getLastEnchantmentIndex(List<Component> lines) {
        int index = lines.isEmpty() ? 0 : 1;
        for (int i = 0; i < lines.size(); i++) {
            Component component = lines.get(i);
            if (component.getContents() == PlainTextContents.EMPTY) {
                if (!component.getSiblings().isEmpty()) {
                    component = component.getSiblings().get(0);
                }
            }
            // also matches Enchantment Descriptions format, so we append afterward
            if (component.getContents() instanceof TranslatableContents contents &&
                    contents.getKey().matches("^enchantment\\.[a-z0-9_.-]+\\.[a-z0-9/._-]+")) {
                index = i + 1;
            }
        }
        return index;
    }

    private static int getHideFlags(ItemStack itemStack) {
        return itemStack.hasTag() && itemStack.getTag().contains("HideFlags", 99) ?
                itemStack.getTag().getInt("HideFlags") :
                0;
    }

    private static boolean shouldShowInTooltip(int hideFlags, ItemStack.TooltipPart part) {
        return (hideFlags & part.getMask()) == 0;
    }

    public static Optional<Component> getEnchantmentDescription(Enchantment enchantment) {
        // check for the mod to be present, we don't want some modded enchantments have descriptions since their mod ships them, while vanilla does not have any, since they are only provided by the Enchantment Descriptions mod
        if (!ModLoaderEnvironment.INSTANCE.isModLoaded("enchdesc")) {
            return Optional.empty();
        } else if (Language.getInstance().has(enchantment.getDescriptionId() + ".desc")) {
            return Optional.of(Component.translatable(enchantment.getDescriptionId() + ".desc")
                    .withStyle(ChatFormatting.GRAY));
        } else if (Language.getInstance().has(enchantment.getDescriptionId() + ".description")) {
            return Optional.of(Component.translatable(enchantment.getDescriptionId() + ".description")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            return Optional.empty();
        }
    }
}
