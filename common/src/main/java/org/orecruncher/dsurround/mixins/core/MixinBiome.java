package org.orecruncher.dsurround.mixins.core;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.orecruncher.dsurround.config.biome.BiomeInfo;
import org.orecruncher.dsurround.mixinutils.IBiomeExtended;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Biome.class)
public abstract class MixinBiome implements IBiomeExtended {

    @Unique
    private BiomeInfo dsurround_info;

    @Final
    @Shadow
    private Biome.ClimateSettings climateSettings;

    @Final
    @Shadow
    private BiomeSpecialEffects specialEffects;

    @Override
    public BiomeSpecialEffects dsurround_getSpecialEffects() {
        return this.specialEffects;
    };

    @Override
    public BiomeInfo dsurround_getInfo() {
        return this.dsurround_info;
    }

    @Override
    public void dsurround_setInfo(BiomeInfo info) {
        this.dsurround_info = info;
    }

    @Override
    public Biome.ClimateSettings dsurround_getWeather() {
        return this.climateSettings;
    }

    @Invoker("getTemperature")
    public abstract float dsurround_getTemperature(BlockPos pos, int seaLevel);
}
