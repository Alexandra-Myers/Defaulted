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

public record ItemPatches(HolderSet<Item> items, List<TagKey<Item>> tags, DataComponentPatch dataComponentPatch) {
    public static final Codec<ItemPatches> DIRECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ExtraCodecs.nonEmptyHolderSet(RegistryCodecs.homogeneousList(Registries.ITEM)).optionalFieldOf("items", HolderSet.empty()).forGetter(ItemPatches::items),
                    TagKey.codec(Registries.ITEM).listOf().optionalFieldOf("tags", Collections.emptyList()).forGetter(ItemPatches::tags),
                    DataComponentPatch.CODEC.fieldOf("patch").forGetter(ItemPatches::dataComponentPatch)).apply(instance, ItemPatches::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPatches> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.ITEM), ItemPatches::items,
        ByteBufCodecs.collection(ArrayList::new, TagKey.streamCodec(Registries.ITEM)), ItemPatches::tags,
        DataComponentPatch.STREAM_CODEC, ItemPatches::dataComponentPatch, ItemPatches::new);

    public void apply(Item item, PatchedDataComponentMap newMap) {
        if (items.size() != 0 && !tags.isEmpty()) {
            Holder<Item> itemHolder = item.builtInRegistryHolder();
            List<TagKey<Item>> matchedTags = tags.stream().filter(tagKey -> itemHolder.is(tagKey)).toList();
            if (!(items.contains(itemHolder) || !matchedTags.isEmpty())) return;
        }
        newMap.applyPatch(dataComponentPatch);
    }
}
