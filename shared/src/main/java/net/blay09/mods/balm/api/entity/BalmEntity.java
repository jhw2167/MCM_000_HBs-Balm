package net.blay09.mods.balm.api.entity;

import net.minecraft.nbt.CompoundTag;

public interface BalmEntity {

    CompoundTag getFabricBalmData();

    void setFabricBalmData(CompoundTag tag);

}
