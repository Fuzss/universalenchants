package fuzs.universalenchants.capability;

import fuzs.puzzleslib.api.capability.v2.data.CapabilityComponent;

public interface ArrowLootingCapability extends CapabilityComponent {

    void setLevel(byte level);

    byte getLevel();
}
