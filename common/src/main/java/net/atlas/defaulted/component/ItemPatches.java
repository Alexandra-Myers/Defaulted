package net.atlas.defaulted.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;

public record ItemPatches(HolderSet<Item> items, DataComponentPatch dataComponentPatch) {
    public static final Codec<ItemPatches> DIRECT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(ExtraCodecs.nonEmptyHolderSet(RegistryCodecs.homogeneousList(Registries.ITEM)).fieldOf("items").forGetter(ItemPatches::items),
                    DataComponentPatch.CODEC.fieldOf("patch").forGetter(ItemPatches::dataComponentPatch)).apply(instance, ItemPatches::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemPatches> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.holderSet(Registries.ITEM), ItemPatches::items, DataComponentPatch.STREAM_CODEC, ItemPatches::dataComponentPatch, ItemPatches::new);

    public void apply(Item item, PatchedDataComponentMap newMap) {
        if (!items.contains(item.builtInRegistryHolder())) return;
        newMap.applyPatch(dataComponentPatch);
    }
}
