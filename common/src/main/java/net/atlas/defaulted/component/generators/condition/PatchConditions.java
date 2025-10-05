package net.atlas.defaulted.component.generators.condition;

import java.util.List;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.extension.LateBoundIdMapper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public class PatchConditions {
    public static final LateBoundIdMapper<ResourceLocation, MapCodec<? extends PatchCondition>> CONDITION_MAPPER = new LateBoundIdMapper<>();
    public static final MapCodec<PatchCondition> MAP_CODEC = CONDITION_MAPPER.codec(ResourceLocation.CODEC)
		.dispatchMap("condition", PatchCondition::codec, mapCodec -> mapCodec);
    public static void bootstrap() {
        CONDITION_MAPPER.put(new ResourceLocation("invert"), InvertCondition.CODEC);
        CONDITION_MAPPER.put(new ResourceLocation("condition_list"), ListCondition.CODEC);
        CONDITION_MAPPER.put(new ResourceLocation("is_item"), ItemIsCondition.CODEC);
        CONDITION_MAPPER.put(new ResourceLocation("in_tag"), ItemHasTagCondition.CODEC);
        CONDITION_MAPPER.put(new ResourceLocation("has_components"), ComponentsPresentCondition.CODEC);
        CONDITION_MAPPER.put(new ResourceLocation("matches_components"), ExactComponentsCondition.CODEC);
    }
    
    public interface PatchCondition {
        boolean matches(Item item, PatchedDataComponentMap patchedDataComponentMap);
        MapCodec<? extends PatchCondition> codec();
    }
    public record InvertCondition(PatchCondition patchCondition) implements PatchCondition {
        public static final MapCodec<InvertCondition> CODEC = PatchConditions.MAP_CODEC.codec().fieldOf("inverted").xmap(InvertCondition::new, InvertCondition::patchCondition);
        @Override
        public boolean matches(Item item, PatchedDataComponentMap patchedDataComponentMap) {
            return !patchCondition.matches(item, patchedDataComponentMap);
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
        public boolean matches(Item item, PatchedDataComponentMap patchedDataComponentMap) {
            for (PatchCondition condition : conditions) {
                boolean result = condition.matches(item, patchedDataComponentMap);
                if (result != allMatch) return result;
            }
            return allMatch;
        }
        @Override
        public MapCodec<? extends PatchCondition> codec() {
            return null;
        }
    }
    public record ItemIsCondition(HolderSet<Item> items) implements PatchCondition {
        public static final MapCodec<ItemIsCondition> CODEC = ExtraCodecs.nonEmptyHolderSet(RegistryCodecs.homogeneousList(Registries.ITEM)).xmap(ItemIsCondition::new, ItemIsCondition::items).fieldOf("items");
        @SuppressWarnings("deprecation")
        @Override
        public boolean matches(Item item, PatchedDataComponentMap patchedDataComponentMap) {
            for (Holder<Item> itemHolder : items) {
                if (item.builtInRegistryHolder().is(itemHolder)) return true;
            }
            return false;
        }

        @Override
        public MapCodec<? extends PatchCondition> codec() {
            return CODEC;
        }
    }
    public record ItemHasTagCondition(List<TagKey<Item>> tags) implements PatchCondition {
        public static final MapCodec<ItemHasTagCondition> CODEC = TagKey.codec(Registries.ITEM).listOf().xmap(ItemHasTagCondition::new, ItemHasTagCondition::tags).fieldOf("tags");
        @SuppressWarnings("deprecation")
        @Override
        public boolean matches(Item item, PatchedDataComponentMap patchedDataComponentMap) {
            for (TagKey<Item> itemTag : tags) {
                if (item.builtInRegistryHolder().is(itemTag)) return true;
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
            instance.group(DataComponentType.CODEC.listOf().fieldOf("components").forGetter(ComponentsPresentCondition::presentComponents),
                Codec.BOOL.fieldOf("all_required").forGetter(ComponentsPresentCondition::allMatch)).apply(instance, ComponentsPresentCondition::new));
        @Override
        public boolean matches(Item item, PatchedDataComponentMap patchedDataComponentMap) {
            for (DataComponentType<?> dataComponentType : presentComponents) {
                boolean result = patchedDataComponentMap.has(dataComponentType);
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
            instance.group(DataComponentMap.CODEC.fieldOf("components").forGetter(ExactComponentsCondition::exactComponents),
                Codec.BOOL.fieldOf("all_required").forGetter(ExactComponentsCondition::allMatch)).apply(instance, ExactComponentsCondition::new));
        @Override
        public boolean matches(Item item, PatchedDataComponentMap patchedDataComponentMap) {
            for (TypedDataComponent<?> dataComponent : exactComponents) {
                TypedDataComponent<?> present = patchedDataComponentMap.getTyped(dataComponent.type());
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
