package net.atlas.defaulted.component.generators;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.component.PatchGenerator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

public record EditUseDurationGenerator(Float useSeconds) implements PatchGenerator {
    public static final MapCodec<EditUseDurationGenerator> CODEC = Codec.floatRange(0, Float.MAX_VALUE).fieldOf("seconds").xmap(EditUseDurationGenerator::new, EditUseDurationGenerator::useSeconds);
    
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        FoodProperties foodProperties = patchedDataComponentMap.get(DataComponents.FOOD);
        if (foodProperties != null) {
            FoodProperties newFoodProperties = new FoodProperties(foodProperties.nutrition(), foodProperties.saturation(), foodProperties.canAlwaysEat(), useSeconds, foodProperties.effects());
            patchedDataComponentMap.set(DataComponents.FOOD, newFoodProperties);
        }
    }
    
    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
    
}