package net.atlas.defaulted.component.generators;

import java.util.List;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.component.ItemPatches;
import net.atlas.defaulted.component.PatchGenerator;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public record AttributeModifiersGenerator(ItemAttributeModifiers toAdd, List<RemovalEntry> toRemove) implements PatchGenerator {
    public static final MapCodec<AttributeModifiersGenerator> CODEC = RecordCodecBuilder.mapCodec(instance ->
        instance.group(ItemAttributeModifiers.CODEC.fieldOf("to_add").forGetter(AttributeModifiersGenerator::toAdd),
            RemovalEntry.CODEC.listOf().fieldOf("to_remove").forGetter(AttributeModifiersGenerator::toRemove)).apply(instance, AttributeModifiersGenerator::new));

    @Override
    public void patchDataComponentMap(Item item, PatchedDataComponentMap patchedDataComponentMap) {
        ItemAttributeModifiers base = patchedDataComponentMap.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        for (ItemAttributeModifiers.Entry entry : base.modifiers()) {
            innerloop: {
                for (RemovalEntry removalEntry : toRemove) if (removalEntry.matches(entry.attribute(), entry.modifier().id())) break innerloop;
                for (ItemAttributeModifiers.Entry added : toAdd.modifiers()) if (ItemPatches.matches(added, entry.attribute(), entry.modifier().id())) break innerloop;
                builder.add(entry.attribute(), entry.modifier(), entry.slot());
            }
        }
        for (ItemAttributeModifiers.Entry entry : toAdd.modifiers()) builder.add(entry.attribute(), entry.modifier(), entry.slot());
        patchedDataComponentMap.set(DataComponents.ATTRIBUTE_MODIFIERS, builder.build());
    }

    @Override
    public MapCodec<? extends PatchGenerator> codec() {
        return CODEC;
    }
    
    public record RemovalEntry(Holder<Attribute> attribute, UUID id) {
        public static final Codec<RemovalEntry> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("type").forGetter(RemovalEntry::attribute),
                    UUIDUtil.CODEC.fieldOf("uuid").forGetter(RemovalEntry::id)).apply(instance, RemovalEntry::new));
        public boolean matches(Holder<Attribute> holder, UUID uuid) {
			return holder.equals(this.attribute) && this.id.equals(uuid);
		}
    }
}
