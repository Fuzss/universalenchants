package fuzs.universalenchants.capability;

import fuzs.puzzleslib.api.capability.v3.data.CapabilityComponent;
import fuzs.universalenchants.UniversalEnchants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.projectile.AbstractArrow;

public class ArrowLootingCapability extends CapabilityComponent<AbstractArrow> {
    public static final String KEY_LOOTING = UniversalEnchants.id("looting").toString();

    private byte level;

    public void setLevel(byte level) {
        if (this.level != level) {
            this.level = level;
            this.setChanged();
        }
    }

    public byte getLevel() {
        return this.level;
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putByte(KEY_LOOTING, this.level);
    }

    @Override
    public void read(CompoundTag tag) {
        this.level = tag.getByte(KEY_LOOTING);
    }
}
