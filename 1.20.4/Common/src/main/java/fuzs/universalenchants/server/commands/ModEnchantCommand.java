package fuzs.universalenchants.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.CommonConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.commands.EnchantCommand;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Collection;
import java.util.Map;

public class ModEnchantCommand {
    public static final String KEY_REMOVE_FAILED_MISSING = "commands.enchant.remove.failed.missing";
    public static final String KEY_REMOVE_SUCCESS_SINGLE = "commands.enchant.remove.success.single";
    public static final String KEY_REMOVE_SUCCESS_MULTIPLE = "commands.enchant.remove.success.multiple";
    private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(object -> Component.translatable(
            "commands.enchant.failed.entity",
            object
    ));
    private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType(object -> Component.translatable(
            "commands.enchant.failed.itemless",
            object
    ));
    private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType(object -> Component.translatable(
            "commands.enchant.failed.incompatible",
            object
    ));
    private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable(
            "commands.enchant.failed.level",
            object,
            object2
    ));
    private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(Component.translatable(
            "commands.enchant.failed"));
    private static final DynamicCommandExceptionType ERROR_MISSING = new DynamicCommandExceptionType(object -> Component.translatable(
            KEY_REMOVE_FAILED_MISSING,
            object
    ));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext context, Commands.CommandSelection environment) {
        if (!UniversalEnchants.CONFIG.get(CommonConfig.class).enchantCommand.replaceVanillaCommand()) {
            EnchantCommand.register(dispatcher, context);
        } else {
            dispatcher.register(Commands.literal("enchant")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .then(Commands.argument("targets", EntityArgument.entities())
                            .then(Commands.argument("enchantment",
                                            ResourceArgument.resource(context, Registries.ENCHANTMENT)
                                    )
                                    .executes(commandContext -> enchant(commandContext.getSource(),
                                            EntityArgument.getEntities(commandContext, "targets"),
                                            ResourceArgument.getEnchantment(commandContext, "enchantment")
                                    ))
                                    .then(
                                            // restrict this to 255, enchantment levels above 255 are not supported in vanilla and will be reset to that anyway
                                            // min of 0 wouldn't do anything in vanilla, but now we use it to remove enchantments
                                            Commands.argument("level", IntegerArgumentType.integer(0, 255))
                                                    .executes(commandContext -> enchant(commandContext.getSource(),
                                                            EntityArgument.getEntities(commandContext, "targets"),
                                                            ResourceArgument.getEnchantment(commandContext,
                                                                    "enchantment"
                                                            ),
                                                            IntegerArgumentType.getInteger(commandContext, "level")
                                                    ))))));
        }
    }

    private static int enchant(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Holder<Enchantment> holder) throws CommandSyntaxException {
        return enchant(commandSourceStack, collection, holder, holder.value().getMaxLevel());
    }

    private static int enchant(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Holder<Enchantment> holder, int level) throws CommandSyntaxException {
        Enchantment enchantment = holder.value();
        // removed max level check (/effect command doesn't have it as well)
        if (!UniversalEnchants.CONFIG.get(CommonConfig.class).enchantCommand.removeMaxLevelLimit &&
                level > enchantment.getMaxLevel()) {
            throw ERROR_LEVEL_TOO_HIGH.create(level, enchantment.getMaxLevel());
        } else {
            // this should actually be restricted via the argument type, but doesn't seem to work reliably (maybe because we override vanilla's command?)
            // so just throw the same exception the argument type would
            if (level > 255) throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.integerTooHigh().create(level, 255);
        }
        int successes = 0;
        for (Entity entity : collection) {
            if (entity instanceof LivingEntity livingEntity) {
                ItemStack mainHandItem = livingEntity.getMainHandItem();
                if (!mainHandItem.isEmpty()) {
                    if (UniversalEnchants.CONFIG.get(CommonConfig.class).enchantCommand.fixEnchantCommand) {
                        ItemStack itemStack = mainHandItem;
                        // handle books, don't forget to set the new stack to the main hand when successful
                        if (itemStack.getCount() == 1 && itemStack.is(Items.BOOK)) {
                            itemStack = new ItemStack(Items.ENCHANTED_BOOK);
                            CompoundTag compoundTag = mainHandItem.getTag();
                            if (compoundTag != null) {
                                itemStack.setTag(compoundTag.copy());
                            }
                        }
                        // allow overriding existing enchantment level
                        if (level == 0 || (itemStack.is(Items.ENCHANTED_BOOK) || enchantment.canEnchant(itemStack) ||
                                enchantment.category.canEnchant(itemStack.getItem())) &&
                                EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(itemStack)
                                        .keySet()
                                        .stream()
                                        .filter(currentEnchantment -> currentEnchantment != enchantment)
                                        .toList(), enchantment)) {
                            Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(itemStack);
                            // when attempting to override existing enchantment level, vanilla will just add it as a duplicate
                            // this ensures the old entry is overridden instead, this method also supports removing enchantments
                            if (level > 0) {
                                Integer oldLevel = enchantments.put(enchantment, level);
                                // do not show a success message later when nothing changes
                                if (oldLevel != null && oldLevel == level) {
                                    if (collection.size() == 1) {
                                        throw ERROR_INCOMPATIBLE.create(mainHandItem.getItem()
                                                .getName(mainHandItem)
                                                .getString());
                                    }
                                }
                            } else {
                                // do not show success message when nothing was removed
                                if (enchantments.remove(enchantment) == null) {
                                    throw ERROR_MISSING.create(mainHandItem.getItem()
                                            .getName(mainHandItem)
                                            .getString());
                                }
                            }
                            setEnchantments(enchantments, itemStack);
                            if (itemStack.is(Items.ENCHANTED_BOOK) && enchantments.isEmpty()) {
                                itemStack = new ItemStack(Items.BOOK);
                                CompoundTag compoundTag = mainHandItem.getTag();
                                if (compoundTag != null) {
                                    itemStack.setTag(compoundTag.copy());
                                }
                            }
                            // don't set this to the hand before, in case enchanting isn't successful
                            livingEntity.setItemInHand(InteractionHand.MAIN_HAND, itemStack);
                            ++successes;
                        } else if (collection.size() == 1) {
                            throw ERROR_INCOMPATIBLE.create(mainHandItem.getItem().getName(mainHandItem).getString());
                        }
                    } else {
                        if ((enchantment.canEnchant(mainHandItem) ||
                                enchantment.category.canEnchant(mainHandItem.getItem())) &&
                                EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(mainHandItem)
                                        .keySet(), enchantment)) {
                            mainHandItem.enchant(enchantment, level);
                            ++successes;
                        } else if (collection.size() == 1) {
                            throw ERROR_INCOMPATIBLE.create(mainHandItem.getItem().getName(mainHandItem).getString());
                        }
                    }
                } else if (collection.size() == 1) {
                    throw ERROR_NO_ITEM.create(livingEntity.getName().getString());
                }
            } else if (collection.size() == 1) {
                throw ERROR_NOT_LIVING_ENTITY.create(entity.getName().getString());
            }
        }
        if (successes == 0) {
            throw ERROR_NOTHING_HAPPENED.create();
        } else {
            if (collection.size() == 1) {
                commandSourceStack.sendSuccess(() -> level > 0 ||
                        !UniversalEnchants.CONFIG.get(CommonConfig.class).enchantCommand.fixEnchantCommand ?
                        Component.translatable("commands.enchant.success.single",
                                enchantment.getFullname(level),
                                collection.iterator().next().getDisplayName()
                        ) :
                        Component.translatable(KEY_REMOVE_SUCCESS_SINGLE,
                                getEnchantmentName(enchantment),
                                collection.iterator().next().getDisplayName()
                        ), true);
            } else {
                commandSourceStack.sendSuccess(() -> level > 0 ||
                        !UniversalEnchants.CONFIG.get(CommonConfig.class).enchantCommand.fixEnchantCommand ?
                        Component.translatable("commands.enchant.success.multiple",
                                enchantment.getFullname(level),
                                collection.size()
                        ) :
                        Component.translatable(KEY_REMOVE_SUCCESS_MULTIPLE,
                                getEnchantmentName(enchantment),
                                collection.size()
                        ), true);
            }

            return successes;
        }
    }

    private static void setEnchantments(Map<Enchantment, Integer> map, ItemStack itemStack) {
        // vanilla handles enchanted books much differently using EnchantedBookItem::addEnchantment
        // we don't want that, as it doesn't allow for overriding/removing existing enchantment levels
        ListTag list = new ListTag();
        for (Map.Entry<Enchantment, Integer> entry : map.entrySet()) {
            Enchantment enchantment = entry.getKey();
            if (enchantment != null) {
                list.add(EnchantmentHelper.storeEnchantment(EnchantmentHelper.getEnchantmentId(enchantment),
                        entry.getValue()
                ));
            }
        }
        String enchantmentsKey = itemStack.is(Items.ENCHANTED_BOOK) ?
                EnchantedBookItem.TAG_STORED_ENCHANTMENTS :
                ItemStack.TAG_ENCH;
        if (list.isEmpty()) {
            itemStack.removeTagKey(enchantmentsKey);
        } else {
            itemStack.addTagElement(enchantmentsKey, list);
        }
    }

    private static Component getEnchantmentName(Enchantment enchantment) {
        MutableComponent mutableComponent = Component.translatable(enchantment.getDescriptionId());
        if (enchantment.isCurse()) {
            mutableComponent.withStyle(ChatFormatting.RED);
        } else {
            mutableComponent.withStyle(ChatFormatting.GRAY);
        }

        return mutableComponent;
    }
}
