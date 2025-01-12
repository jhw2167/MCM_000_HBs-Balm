package net.blay09.mods.balm.forge.world;

import com.mojang.serialization.Codec;
import net.blay09.mods.balm.api.Balm;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public class BalmBiomeModifier implements BiomeModifier {

    public static final BalmBiomeModifier INSTANCE = new BalmBiomeModifier();

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        ForgeBalmWorldGen worldGen = (ForgeBalmWorldGen) Balm.getWorldGen();
        worldGen.modifyBiome(biome, phase, builder);
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return ForgeBalmWorldGen.BALM_BIOME_MODIFIER_CODEC;
    }

}
