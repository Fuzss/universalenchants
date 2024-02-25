package fuzs.universalenchants.capability;

import net.minecraft.nbt.CompoundTag;

public class ArrowLootingCapabilityImpl implements ArrowLootingCapability {
    private byte level;

    @Override
    public void setLevel(byte level) {
        this.level = level;
    }

    @Override
    public byte getLevel() {
        return this.level;
    }

    @Override
    public void write(CompoundTag tag) {
        tag.putByte("Looting", this.level);
    }

    @Override
    public void read(CompoundTag tag) {
        this.level = tag.getByte("Looting");
    }
}
