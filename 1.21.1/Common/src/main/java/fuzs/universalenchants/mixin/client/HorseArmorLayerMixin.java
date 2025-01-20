package fuzs.universalenchants.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HorseModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HorseArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.AnimalArmorItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(HorseArmorLayer.class)
abstract class HorseArmorLayerMixin extends RenderLayer<Horse, HorseModel<Horse>> {

    public HorseArmorLayerMixin(RenderLayerParent<Horse, HorseModel<Horse>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Deprecated(forRemoval = true)
    @ModifyVariable(method = "render", at = @At("STORE"), ordinal = 0)
    public VertexConsumer render(VertexConsumer vertexConsumer, PoseStack poseStack, MultiBufferSource multiBufferSource, int combinedLight, Horse horse) {
        ItemStack itemStack = horse.getBodyArmorItem();
        ResourceLocation resourceLocation = ((AnimalArmorItem) itemStack.getItem()).getTexture();
        return ItemRenderer.getArmorFoilBuffer(multiBufferSource,
                RenderType.armorCutoutNoCull(resourceLocation),
                itemStack.hasFoil());
    }
}
