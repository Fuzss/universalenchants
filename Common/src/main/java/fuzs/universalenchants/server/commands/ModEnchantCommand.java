package fuzs.universalenchants.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.config.ServerConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ItemEnchantmentArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Collection;

public class ModEnchantCommand {
	private static final DynamicCommandExceptionType ERROR_NOT_LIVING_ENTITY = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.enchant.failed.entity", object)
	);
	private static final DynamicCommandExceptionType ERROR_NO_ITEM = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.enchant.failed.itemless", object)
	);
	private static final DynamicCommandExceptionType ERROR_INCOMPATIBLE = new DynamicCommandExceptionType(
		object -> Component.translatable("commands.enchant.failed.incompatible", object)
	);
	private static final Dynamic2CommandExceptionType ERROR_LEVEL_TOO_HIGH = new Dynamic2CommandExceptionType(
			(object, object2) -> Component.translatable("commands.enchant.failed.level", object, object2)
	);
	private static final SimpleCommandExceptionType ERROR_NOTHING_HAPPENED = new SimpleCommandExceptionType(Component.translatable("commands.enchant.failed"));

	public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
		commandDispatcher.register(
			Commands.literal("enchant")
				.requires(commandSourceStack -> commandSourceStack.hasPermission(2))
				.then(
					Commands.argument("targets", EntityArgument.entities())
						.then(
							Commands.argument("enchantment", ItemEnchantmentArgument.enchantment())
								.executes(
									commandContext -> enchant(
											commandContext.getSource(),
											EntityArgument.getEntities(commandContext, "targets"),
											ItemEnchantmentArgument.getEnchantment(commandContext, "enchantment"),
											1
										)
								)
								.then(
										// restrict this to 255, enchantment levels above 255 are not supported in vanilla and will be reset to that anyway
										// also set min of 1, as 0 doesn't do anything
									Commands.argument("level", IntegerArgumentType.integer(1, 255))
										.executes(
											commandContext -> enchant(
													commandContext.getSource(),
													EntityArgument.getEntities(commandContext, "targets"),
													ItemEnchantmentArgument.getEnchantment(commandContext, "enchantment"),
													IntegerArgumentType.getInteger(commandContext, "level")
												)
										)
								)
						)
				)
		);
	}

	private static int enchant(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, Enchantment enchantment, int level) throws CommandSyntaxException {
		// removed max level check (/effect command doesn't have it as well)
		if (!UniversalEnchants.CONFIG.get(ServerConfig.class).fixEnchantCommand && level > enchantment.getMaxLevel()) {
			throw ERROR_LEVEL_TOO_HIGH.create(level, enchantment.getMaxLevel());
		}
		int successes = 0;
		for (Entity entity : collection) {
			if (entity instanceof LivingEntity livingEntity) {
				ItemStack itemStack = livingEntity.getMainHandItem();
				if (!itemStack.isEmpty()) {
					if (UniversalEnchants.CONFIG.get(ServerConfig.class).fixEnchantCommand) {
						// allow overriding existing enchantment level
						if (enchantment.canEnchant(itemStack) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(itemStack).keySet().stream().filter(e -> e != enchantment).toList(), enchantment)) {
							if (!replaceEnchantmentLevel(enchantment, level, itemStack)) {
								itemStack.enchant(enchantment, level);
							}
							++successes;
						} else if (collection.size() == 1) {
							throw ERROR_INCOMPATIBLE.create(itemStack.getItem().getName(itemStack).getString());
						}
					} else {
						if (enchantment.canEnchant(itemStack) && EnchantmentHelper.isEnchantmentCompatible(EnchantmentHelper.getEnchantments(itemStack).keySet(), enchantment)) {
							itemStack.enchant(enchantment, level);
							++successes;
						} else if (collection.size() == 1) {
							throw ERROR_INCOMPATIBLE.create(itemStack.getItem().getName(itemStack).getString());
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
				commandSourceStack.sendSuccess(
						Component.translatable("commands.enchant.success.single", enchantment.getFullname(level), collection.iterator().next().getDisplayName()), true
				);
			} else {
				commandSourceStack.sendSuccess(Component.translatable("commands.enchant.success.multiple", enchantment.getFullname(level), collection.size()), true);
			}

			return successes;
		}
	}

	private static boolean replaceEnchantmentLevel(Enchantment enchantment, int level, ItemStack stack) {
		// when attempting to override existing enchantment level, vanilla will just add it as a duplicate
		// this ensures the old entry is overridden instead
		ListTag list = stack.getEnchantmentTags();
		ResourceLocation id = EnchantmentHelper.getEnchantmentId(enchantment);
		for (int i = 0; i < list.size(); ++i) {
			CompoundTag compoundtag = list.getCompound(i);
			ResourceLocation currentId = EnchantmentHelper.getEnchantmentId(compoundtag);
			if (currentId != null && currentId.equals(id)) {
				list.set(i, EnchantmentHelper.storeEnchantment(id, level));
				return true;
			}
		}
		return false;
	}
}
