package net.atlas.defaulted.component.generators;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.PatchGenerator;
import net.atlas.defaulted.component.ToolMaterialWrapper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.Enchantable;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.level.block.Block;

public record ModifyTierStatsGenerator(List<TierComponents> components,  Optional<TagKey<Block>> toolMineable, Optional<Float> damageFactor) implements PatchGenerator {
    public static final MapCodec<ModifyTierStatsGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(TierComponents.CODEC.listOf().fieldOf("components").forGetter(ModifyTierStatsGenerator::components),
            TagKey.codec(Registries.BLOCK).optionalFieldOf("mineable_blocks").forGetter(ModifyTierStatsGenerator::toolMineable),
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("max_damage_factor").forGetter(ModifyTierStatsGenerator::damageFactor))
        .apply(instance, ModifyTierStatsGenerator::new));

    @SuppressWarnings("deprecation")
    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        ToolMaterialWrapper tier = item.defaulted$getToolMaterial();
        if (tier != null) {
            if (components.contains(TierComponents.DURABILITY)) {
                int maxDamage = tier.durability();
                if (damageFactor.isPresent()) maxDamage = Math.round(maxDamage * damageFactor.get());
                Defaulted.setDurability(maxDamage, patchedDataComponentMap);
            }
            if (components.contains(TierComponents.ENCHANTABLE)) patchedDataComponentMap.set(DataComponents.ENCHANTABLE, new Enchantable(tier.enchantmentValue()));
            if (components.contains(TierComponents.REPAIRABLE)) patchedDataComponentMap.set(DataComponents.REPAIRABLE, new Repairable(BuiltInRegistries.ITEM.getOrThrow(tier.repairItems())));
            if (components.contains(TierComponents.TOOL)) {
                if (!toolMineable.isPresent()) {
                    LogManager.getLogger("Defaulted").warn("Attempted to update tool component for tiered item " + item.builtInRegistryHolder() + " but mineable tag is not present!");
                    return;
                }
                patchedDataComponentMap.set(DataComponents.TOOL, new Tool(List.of(Tool.Rule.deniesDrops(BuiltInRegistries.BLOCK.getOrThrow(tier.incorrectBlocksForDrops())), Tool.Rule.minesAndDrops(BuiltInRegistries.BLOCK.getOrThrow(toolMineable.get()), tier.speed())), 1.0F, 1));
            }
        }
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    };
}
