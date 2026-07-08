package net.atlas.defaulted.enchantment.generators.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.atlas.defaulted.enchantment.EnchantmentBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;

import java.util.List;
import java.util.Objects;

public class PatchConditions {
    public static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends PatchCondition>> CONDITION_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
    public static final MapCodec<PatchCondition> MAP_CODEC = CONDITION_MAPPER.codec(ResourceLocation.CODEC)
		.dispatchMap("condition", PatchCondition::codec, mapCodec -> mapCodec);
    public static void bootstrap() {
        CONDITION_MAPPER.put(ResourceLocation.withDefaultNamespace("invert"), InvertCondition.CODEC);
        CONDITION_MAPPER.put(ResourceLocation.withDefaultNamespace("condition_list"), ListCondition.CODEC);
        CONDITION_MAPPER.put(ResourceLocation.withDefaultNamespace("is_enchantment"), EnchantmentIsCondition.CODEC);
        CONDITION_MAPPER.put(ResourceLocation.withDefaultNamespace("in_tag"), EnchantmentHasTagCondition.CODEC);
        CONDITION_MAPPER.put(ResourceLocation.withDefaultNamespace("has_components"), ComponentsPresentCondition.CODEC);
        CONDITION_MAPPER.put(ResourceLocation.withDefaultNamespace("matches_components"), ExactComponentsCondition.CODEC);
    }
    
    public interface PatchCondition {
        boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder);
        MapCodec<? extends PatchCondition> codec();
    }
    public record InvertCondition(PatchCondition patchCondition) implements PatchCondition {
        public static final MapCodec<InvertCondition> CODEC = PatchConditions.MAP_CODEC.codec().fieldOf("inverted").xmap(InvertCondition::new, InvertCondition::patchCondition);
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            return !patchCondition.matches(enchantment, builder);
        }

        @Override
        public MapCodec<? extends PatchCondition> codec() {
            return CODEC;
        }
        
    }
    public record ListCondition(List<PatchCondition> conditions, boolean allMatch) implements PatchCondition {
        public static final MapCodec<ListCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(MAP_CODEC.codec().listOf().fieldOf("conditions").forGetter(ListCondition::conditions),
                Codec.BOOL.fieldOf("all_required").forGetter(ListCondition::allMatch)).apply(instance, ListCondition::new));
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (PatchCondition condition : conditions) {
                boolean result = condition.matches(enchantment, builder);
                if (result != allMatch) return result;
            }
            return allMatch;
        }
        @Override
        public MapCodec<? extends PatchCondition> codec() {
            return null;
        }
    }
    public record EnchantmentIsCondition(HolderSet<Enchantment> enchantments) implements PatchCondition {
        public static final MapCodec<EnchantmentIsCondition> CODEC = ExtraCodecs.nonEmptyHolderSet(RegistryCodecs.homogeneousList(Registries.ENCHANTMENT)).xmap(EnchantmentIsCondition::new, EnchantmentIsCondition::enchantments).fieldOf("enchantments");
        @SuppressWarnings("deprecation")
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (Holder<Enchantment> enchantmentHolder : enchantments) {
                if (enchantment.is(enchantmentHolder)) return true;
            }
            return false;
        }

        @Override
        public MapCodec<? extends PatchCondition> codec() {
            return CODEC;
        }
    }
    public record EnchantmentHasTagCondition(List<TagKey<Enchantment>> tags) implements PatchCondition {
        public static final MapCodec<EnchantmentHasTagCondition> CODEC = TagKey.codec(Registries.ENCHANTMENT).listOf().xmap(EnchantmentHasTagCondition::new, EnchantmentHasTagCondition::tags).fieldOf("tags");
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (TagKey<Enchantment> enchantmentTag : tags) {
                if (enchantment.is(enchantmentTag)) return true;
            }
            return false;
        }

        @Override
        public MapCodec<? extends PatchCondition> codec() {
            return CODEC;
        }
    }
    public record ComponentsPresentCondition(List<DataComponentType<?>> presentComponents, boolean allMatch) implements PatchCondition {
        public static final MapCodec<ComponentsPresentCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(EnchantmentEffectComponents.COMPONENT_CODEC.listOf().fieldOf("components").forGetter(ComponentsPresentCondition::presentComponents),
                Codec.BOOL.fieldOf("all_required").forGetter(ComponentsPresentCondition::allMatch)).apply(instance, ComponentsPresentCondition::new));
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (DataComponentType<?> dataComponentType : presentComponents) {
                boolean result = builder.has(dataComponentType);
                if (result != allMatch) return result;
            }
            return allMatch;
        }
        @Override
        public MapCodec<? extends PatchCondition> codec() {
            return CODEC;
        }
    }
    public record ExactComponentsCondition(DataComponentMap exactComponents, boolean allMatch) implements PatchCondition {
        public static final MapCodec<ExactComponentsCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(EnchantmentEffectComponents.CODEC.fieldOf("components").forGetter(ExactComponentsCondition::exactComponents),
                Codec.BOOL.fieldOf("all_required").forGetter(ExactComponentsCondition::allMatch)).apply(instance, ExactComponentsCondition::new));
        @Override
        public boolean matches(Holder<Enchantment> enchantment, EnchantmentBuilder builder) {
            for (TypedDataComponent<?> dataComponent : exactComponents) {
                TypedDataComponent<?> present = builder.getTyped(dataComponent.type());
                boolean result = present == null ? false : Objects.equals(dataComponent.value(), present.value());
                if (result != allMatch) return result;
            }
            return allMatch;
        }
        @Override
        public MapCodec<? extends PatchCondition> codec() {
            return CODEC;
        }
    }
}
