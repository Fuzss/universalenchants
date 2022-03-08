package fuzs.universalenchants.capability;

import fuzs.puzzleslib.capability.data.CapabilityComponent;

public interface ArrowLootingCapability extends CapabilityComponent {
    void setLevel(byte level);

    byte getLevel();
}
