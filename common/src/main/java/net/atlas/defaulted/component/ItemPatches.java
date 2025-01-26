package net.atlas.defaulted.component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public record ItemPatches(HolderSet<Item> items, List<TagKey<Item>> tags, List<PatchGenerator> generators, DataComponentPatch dataComponentPatch, int priority) implements Comparable<ItemPatches> {
    public static final Codec<ItemPatches> DIRECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ExtraCodecs.nonEmptyHolderSet(RegistryCodecs.homogeneousList(Registries.ITEM)).optionalFieldOf("items", HolderSet.empty()).forGetter(ItemPatches::items),
                    TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("tags", Collections.emptyList()).forGetter(ItemPatches::tags),
                    PatchGenerator.CODEC.listOf().optionalFieldOf("patch_generators", Collections.emptyList()).forGetter(ItemPatches::generators),
                    DataComponentPatch.CODEC.fieldOf("patch").forGetter(ItemPatches::dataComponentPatch),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("priority", 1000).forGetter(ItemPatches::priority)).apply(instance, ItemPatches::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPatches> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.ITEM), ItemPatches::items,
        ByteBufCodecs.collection(ArrayList::new, TagKey.streamCodec(Registries.ITEM)), ItemPatches::tags,
        ByteBufCodecs.collection(ArrayList::new, ByteBufCodecs.fromCodecTrusted(PatchGenerator.CODEC)), ItemPatches::generators,
        DataComponentPatch.STREAM_CODEC, ItemPatches::dataComponentPatch,
        ByteBufCodecs.VAR_INT, ItemPatches::priority, ItemPatches::new);
    @Override
    public int compareTo(ItemPatches o) {
        return priority - o.priority;
    }

    public void apply(Item item, PatchedDataComponentMap newMap) {
        if (!matchItem(item)) return;
        newMap.applyPatch(dataComponentPatch);
    }
    
    public void applyGenerators(Item item, PatchedDataComponentMap newMap) {
        if (!matchItem(item)) return;
        generators.forEach(patchGenerator -> patchGenerator.patchDataComponentMap(item, newMap));
    }

    @SuppressWarnings("deprecation")
    public boolean matchItem(Item item) {
        if (items.size() != 0 || !tags.isEmpty()) {
            Holder<Item> itemHolder = item.builtInRegistryHolder();
            List<TagKey<Item>> matchedTags = tags.stream().filter(tagKey -> itemHolder.is(tagKey)).toList();
            if (!(items.contains(itemHolder) || !matchedTags.isEmpty())) return false;
        }
        return true;
    }
}
