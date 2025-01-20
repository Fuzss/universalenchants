package fuzs.universalenchants.fabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import fuzs.puzzleslib.api.event.v1.data.DefaultedValue;
import fuzs.universalenchants.handler.BetterEnchantsHandler;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
abstract class PlayerFabricMixin extends LivingEntity {

    protected PlayerFabricMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyVariable(
            method = "attack", at = @At(
            value = "CONSTANT", args = "classValue=net/minecraft/world/item/SwordItem"
    ), ordinal = 3
    )
    public boolean attack(boolean isSweepingSupported) {
        // sweeping is hardcoded to instances of the sword item class, so we check for the attribute value instead
        // this is patched in before vanilla, so we are overridden again if vanilla sets the value to true, but that is fine,
        // since all swords should still support sweeping regardless of the sweeping edge enchantment being present
        return isSweepingSupported || this.getAttributeValue(Attributes.SWEEPING_DAMAGE_RATIO) > 0.0;
    }

    @Deprecated(forRemoval = true)
    @ModifyReturnValue(method = "getProjectile", at = @At("RETURN"))
    public ItemStack getProjectile(ItemStack projectileItemStack, ItemStack weaponItemStack) {
        if (weaponItemStack.getItem() instanceof ProjectileWeaponItem) {
            DefaultedValue<ItemStack> projectileItemStackValue = DefaultedValue.fromValue(projectileItemStack);
            BetterEnchantsHandler.onGetProjectile(this, weaponItemStack, projectileItemStackValue);
            return projectileItemStackValue.getAsOptional().orElse(projectileItemStack);
        } else {
            return projectileItemStack;
        }
    }
}
