package fuzs.universalenchants.client.gui.screens.inventory;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.platform.InputConstants;
import fuzs.puzzleslib.api.client.gui.v2.components.ScreenTooltipFactory;
import fuzs.puzzleslib.api.client.gui.v2.components.SpritelessImageButton;
import fuzs.puzzleslib.api.client.gui.v2.components.TooltipRenderHelper;
import fuzs.puzzleslib.api.client.gui.v2.screen.ScreenHelperV2;
import fuzs.universalenchants.UniversalEnchants;
import fuzs.universalenchants.client.handler.StoredEnchantmentsTooltipHandler;
import fuzs.universalenchants.network.client.ServerboundSetEnchantmentsMessage;
import fuzs.universalenchants.util.StoredEnchantmentHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class EditEnchantmentsScreen extends Screen {
    public static final Component COMPONENT_EDIT_ENCHANTMENTS = Component.translatable("enchantments.edit");
    public static final ResourceLocation EDIT_ENCHANTMENTS_TEXTURE = UniversalEnchants.id(
            "textures/gui/enchantments.png");
    private static final ResourceLocation TEXT_FIELD_SPRITE = new ResourceLocation("container/anvil/text_field");
    private static final ResourceLocation TEXT_FIELD_DISABLED_SPRITE = new ResourceLocation(
            "container/anvil/text_field_disabled");
    private static final ResourceLocation SCROLLER_SPRITE = new ResourceLocation("container/creative_inventory/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = new ResourceLocation(
            "container/creative_inventory/scroller_disabled");
    public static final String KEY_INCOMPATIBLE_ENCHANTMENTS =
            "gui." + UniversalEnchants.MOD_ID + ".tooltip.incompatible";

    private final Screen lastScreen;
    private final int containerId;
    private final ItemStack item;
    private final int slotIndex;
    private final Map<Enchantment, Integer> enchantmentLookup;
    private final Set<Enchantment> enchantments = Sets.newIdentityHashSet();
    private final Set<Enchantment> storedEnchantments = Sets.newIdentityHashSet();
    public int imageWidth = 176;
    public int imageHeight = 166;
    public int leftPos;
    public int topPos;
    private EditBox name;
    private float scrollAmount;
    private final List<ClickableEnchantmentButton> enchantmentButtons = new ArrayList<>();
    private boolean scrolling;
    @Nullable
    private List<? extends ClientTooltipComponent> itemTooltip;

    public EditEnchantmentsScreen(Screen lastScreen, int containerId, ItemStack item, int slotIndex) {
        super(COMPONENT_EDIT_ENCHANTMENTS);
        this.lastScreen = lastScreen;
        this.containerId = containerId;
        this.item = item.copy();
        this.slotIndex = slotIndex;
        this.enchantmentLookup = ImmutableMap.copyOf(StoredEnchantmentHelper.getAllEnchantments(this.item));
        this.enchantments.addAll(EnchantmentHelper.deserializeEnchantments(this.item.getEnchantmentTags()).keySet());
        this.storedEnchantments.addAll(EnchantmentHelper.deserializeEnchantments(StoredEnchantmentHelper.getStoredEnchantments(
                this.item)).keySet());
    }

    @Override
    protected void init() {
        this.enchantmentButtons.clear();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        this.addRenderableWidget(new SpritelessImageButton(this.leftPos + this.imageWidth - 9 - 4,
                this.topPos + 4,
                9,
                9,
                138,
                166,
                EDIT_ENCHANTMENTS_TEXTURE,
                $ -> this.onClose()
        ).setTextureLayout(SpritelessImageButton.LEGACY_TEXTURE_LAYOUT));
        this.name = new EditBox(this.font, this.leftPos + 62, this.topPos + 24, 103, 12, COMPONENT_EDIT_ENCHANTMENTS);
        this.name.setFocused(false);
        this.name.setTextColor(-1);
        this.name.setTextColorUneditable(-1);
        this.name.setBordered(false);
        this.name.setMaxLength(50);
        this.name.setValue(this.item.getHoverName().getString());
        this.name.setEditable(false);
        this.addWidget(this.name);
        int index = 0;
        for (Enchantment enchantment : this.enchantmentLookup.keySet()) {
            if (!enchantment.isCurse()) {
                this.enchantmentButtons.add(new ClickableEnchantmentButton(this.leftPos + 18,
                        this.topPos + 64 + index++ * 20,
                        enchantment
                ));
            }
        }

        this.refreshEnchantmentButtons();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.enableScissor(this.leftPos + 18, this.topPos + 64, this.leftPos + 18 + 126, this.topPos + 64 + 90);
        for (SpritelessImageButton imageButton : this.enchantmentButtons) {
            imageButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        guiGraphics.disableScissor();
        guiGraphics.drawString(this.font, this.title, this.leftPos + 62, this.topPos + 8, 4210752, false);
        this.name.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.0F, 2.0F, 2.0F);
        guiGraphics.renderItem(this.item, (this.leftPos + 17) / 2, (this.topPos + 8) / 2);
        guiGraphics.pose().popPose();
        guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.itemTooltip != null &&
                ScreenHelperV2.isHovering(this.leftPos + 17, this.topPos + 8, 32, 32, mouseX, mouseY)) {
            TooltipRenderHelper.renderTooltipInternal(guiGraphics,
                    mouseX,
                    mouseY,
                    (List<ClientTooltipComponent>) this.itemTooltip
            );
        }
    }

    private void refreshEnchantmentButtons() {

        List<Component> lines = new ArrayList<>();
        MutableComponent mutableComponent = Component.empty()
                .append(this.item.getHoverName())
                .withStyle(this.item.getRarity().color);
        if (this.item.hasCustomHoverName()) {
            mutableComponent.withStyle(ChatFormatting.ITALIC);
        }

        lines.add(mutableComponent);

        this.enchantments.stream()
                .sorted(StoredEnchantmentHelper.ENCHANTMENT_COMPARATOR)
                .mapMulti((Enchantment enchantment, Consumer<Component> consumer) -> {
                    consumer.accept(enchantment.getFullname(this.enchantmentLookup.getOrDefault(enchantment, 1)));
                    StoredEnchantmentsTooltipHandler.getEnchantmentDescription(enchantment).ifPresent(consumer);
                })
                .forEach(lines::add);
        this.storedEnchantments.stream()
                .sorted(StoredEnchantmentHelper.ENCHANTMENT_COMPARATOR)
                .mapMulti((Enchantment enchantment, Consumer<Component> consumer) -> {
                    Component component = enchantment.getFullname(this.enchantmentLookup.getOrDefault(enchantment, 1));
                    consumer.accept(Component.empty().append(component).withStyle(ChatFormatting.STRIKETHROUGH));
                    StoredEnchantmentsTooltipHandler.getEnchantmentDescription(enchantment).ifPresent(consumer);
                })
                .forEach(lines::add);

        this.itemTooltip = lines.stream()
                .flatMap(ScreenTooltipFactory::splitTooltipLines)
                .map(ClientTextTooltip::new)
                .toList();

        this.enchantmentButtons.forEach(ClickableEnchantmentButton::refresh);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.blit(EDIT_ENCHANTMENTS_TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        guiGraphics.blitSprite(TEXT_FIELD_SPRITE, this.leftPos + 59, this.topPos + 20, 110, 16);
        ResourceLocation resourceLocation = this.canScroll() ? SCROLLER_SPRITE : SCROLLER_DISABLED_SPRITE;
        guiGraphics.blitSprite(resourceLocation,
                this.leftPos + 153,
                this.topPos + 64 + (int) ((90 - 15) * this.scrollAmount),
                12,
                15
        );
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!this.canScroll()) {
            return false;
        } else {
            this.scrollAmount = this.subtractInputFromScroll(this.scrollAmount, scrollY);
            this.scrollTo(this.scrollAmount);
            return true;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT && this.insideScrollbar(mouseX, mouseY)) {
            this.scrolling = this.canScroll();
            if (this.scrolling) {
                this.scrollToPosition(mouseY);
            }
            return true;
        } else if (this.insideEnchantmentList(mouseX, mouseY)) {
            for (SpritelessImageButton imageButton : this.enchantmentButtons) {
                if (imageButton.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    protected boolean insideEnchantmentList(double mouseX, double mouseY) {
        return ScreenHelperV2.isHovering(this.leftPos + 18, this.topPos + 64, 126, 90, mouseX, mouseY);
    }

    protected boolean insideScrollbar(double mouseX, double mouseY) {
        return ScreenHelperV2.isHovering(this.leftPos + 153, this.topPos + 64, 12, 90, mouseX, mouseY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.scrolling) {
            this.scrollToPosition(mouseY);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
    }

    private void scrollToPosition(double mouseY) {
        int scrollBarStart = this.topPos + 64;
        int scrollBarEnd = scrollBarStart + 90;
        this.scrollAmount =
                ((float) mouseY - (float) scrollBarStart - 7.5F) / ((float) (scrollBarEnd - scrollBarStart) - 15.0F);
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0.0F, 1.0F);
        this.scrollTo(this.scrollAmount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.scrolling = false;
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    protected void scrollTo(float scrollAmount) {
        int scrollPosition = (int) ((this.enchantmentButtons.stream().mapToInt(AbstractWidget::getHeight).sum() - 90) *
                scrollAmount);
        int startY = this.topPos + 64 - scrollPosition;
        for (int i = 0; i < this.enchantmentButtons.size(); i++) {
            this.enchantmentButtons.get(i).setY(startY + i * 20);
        }
    }

    protected float subtractInputFromScroll(float scrollAmount, double input) {
        // 4 is the amount of buttons fully fitting on the screen
        return Mth.clamp(scrollAmount - (float) (input / (this.enchantmentButtons.size() - 4)), 0.0F, 1.0F);
    }

    protected boolean canScroll() {
        return this.enchantmentButtons.stream().mapToInt(AbstractWidget::getHeight).sum() > 90;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClose() {
        Set<Enchantment> storedEnchantments = EnchantmentHelper.deserializeEnchantments(StoredEnchantmentHelper.getStoredEnchantments(
                this.item)).keySet();
        if (!Objects.equals(this.storedEnchantments, storedEnchantments)) {
            // setting this on the client is important for the creative inventory, asince for tabs other than inventory the slot index does not match on the server, and it depends on client data being synced
            ServerboundSetEnchantmentsMessage.setEnchantments(this.minecraft.player,
                    this.containerId,
                    this.slotIndex,
                    this.storedEnchantments
            );
            UniversalEnchants.NETWORK.sendMessage(new ServerboundSetEnchantmentsMessage(this.containerId,
                    this.slotIndex,
                    this.storedEnchantments
            ));
        }

        this.minecraft.setScreen(this.lastScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    class ClickableEnchantmentButton extends SpritelessImageButton {
        private final Enchantment enchantment;

        public ClickableEnchantmentButton(int x, int y, Enchantment enchantment) {
            super(x, y, 126, 20, 0, 166, 20, EDIT_ENCHANTMENTS_TEXTURE, 256, 256, $ -> {
            }, StoredEnchantmentHelper.getEnchantmentName(enchantment));
            this.setTextureLayout(LEGACY_TEXTURE_LAYOUT);
            this.enchantment = enchantment;
        }

        public void refresh() {
            this.active = true;
            Component component = StoredEnchantmentsTooltipHandler.getEnchantmentDescription(this.enchantment).orElse(null);
            if (EditEnchantmentsScreen.this.storedEnchantments.contains(this.enchantment)) {
                Optional<MutableComponent> optional = EditEnchantmentsScreen.this.enchantments.stream()
                        .filter(enchantment -> !enchantment.isCompatibleWith(this.enchantment))
                        .map(enchantment -> Component.translatable(enchantment.getDescriptionId()))
                        .reduce((o1, o2) -> o1.append(", ").append(o2));
                if (optional.isPresent()) {
                    component = Component.translatable(KEY_INCOMPATIBLE_ENCHANTMENTS,
                            optional.get().withStyle(ChatFormatting.GRAY)
                    );
                    this.active = false;
                }
            }

            this.setTooltip(component != null ? Tooltip.create(component) : null);
        }

        @Override
        public void onPress() {
            if (EditEnchantmentsScreen.this.enchantments.remove(this.enchantment)) {
                EditEnchantmentsScreen.this.storedEnchantments.add(this.enchantment);
                EditEnchantmentsScreen.this.refreshEnchantmentButtons();
            } else if (EditEnchantmentsScreen.this.storedEnchantments.remove(this.enchantment)) {
                if (EnchantmentHelper.isEnchantmentCompatible(EditEnchantmentsScreen.this.enchantments,
                        this.enchantment
                )) {
                    EditEnchantmentsScreen.this.enchantments.add(this.enchantment);
                    EditEnchantmentsScreen.this.refreshEnchantmentButtons();
                } else {
                    EditEnchantmentsScreen.this.storedEnchantments.add(this.enchantment);
                }
            }
        }

        @Override
        public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            if (!EditEnchantmentsScreen.this.insideEnchantmentList(mouseX, mouseY)) {
                this.isHovered = false;
            }

            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            int uOffset = EditEnchantmentsScreen.this.storedEnchantments.contains(this.enchantment) ? 147 + 16 : 147;
            guiGraphics.blit(this.resourceLocation,
                    this.getX() + 2,
                    this.getY() + 2,
                    uOffset,
                    166 + 16 * this.getTextureY(),
                    16,
                    16,
                    this.textureWidth,
                    this.textureHeight
            );
            guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
            this.renderString(guiGraphics,
                    EditEnchantmentsScreen.this.minecraft.font,
                    this.getFontColor() | Mth.ceil(this.alpha * 255.0F) << 24
            );
        }

        @Override
        public void renderString(GuiGraphics guiGraphics, Font font, int color) {
            this.renderScrollingString(guiGraphics, font, 2, color);
        }

        @Override
        protected void renderScrollingString(GuiGraphics guiGraphics, Font font, int borderGap, int color) {
            int startX = this.getX() + 20 + borderGap;
            int endX = this.getX() + this.getWidth() - borderGap;
            renderScrollingString(guiGraphics,
                    font,
                    this.getMessage(),
                    startX,
                    this.getY(),
                    endX,
                    this.getY() + this.getHeight(),
                    color
            );
        }

        protected int getFontColor() {
            int textureY = this.getTextureY();
            return textureY == 0 ? 0x685E4A : textureY == 2 ? ChatFormatting.YELLOW.getColor() : 0xFFFFFF;
        }
    }
}
