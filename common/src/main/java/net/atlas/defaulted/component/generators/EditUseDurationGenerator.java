package net.atlas.defaulted.component.generators;

import com.mojang.serialization.MapCodec;

import net.atlas.defaulted.component.PatchGenerator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;

public record EditUseDurationGenerator(Float useSeconds) implements PatchGenerator {
    public static final MapCodec<EditUseDurationGenerator> CODEC = ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("seconds").xmap(EditUseDurationGenerator::new, EditUseDurationGenerator::useSeconds);
    
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        Consumable consumable = patchedDataComponentMap.get(DataComponents.CONSUMABLE);
        if (consumable != null) {
            Consumable newConsumable = new Consumable(useSeconds, consumable.animation(), consumable.sound(), consumable.hasConsumeParticles(), consumable.onConsumeEffects());
            patchedDataComponentMap.set(DataComponents.CONSUMABLE, newConsumable);
        }
    }
    
    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    };
    
}
