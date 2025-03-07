package net.atlas.defaulted.component.generators;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;

public interface WeaponLevelBasedValue {
    public static final ExtraCodecs.LateBoundIdMapper<ResourceLocation, MapCodec<? extends WeaponLevelBasedValue>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper<>();
    public static final Codec<WeaponLevelBasedValue> BASE_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatch(WeaponLevelBasedValue::codec, mapCodec -> mapCodec);
    public static void bootstrap() {
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("unconditional"), Unconditional.CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("match_weapon_level"), MatchingLevel.CODEC);
    }
    public Codec<List<WeaponLevelBasedValue>> CODEC = Codec.withAlternative(BASE_CODEC.listOf(), Codec.FLOAT.xmap(Unconditional::new, Unconditional::value), Collections::singletonList);
    Float getResult(int weaponLevel, boolean applyTier);
    MapCodec<? extends WeaponLevelBasedValue> codec();
    public static record LevelCondition(int min, int max) {
        public static final Codec<LevelCondition> CODEC = Codec.withAlternative(RecordCodecBuilder.create(instance ->
            instance.group(Codec.INT.fieldOf("min").forGetter(LevelCondition::min),
                Codec.INT.fieldOf("max").forGetter(LevelCondition::max)).apply(instance, LevelCondition::new)), Codec.INT, i -> new LevelCondition(i, i));
        public boolean matches(int value) {
            return value >= min && value <= max;
        }
    }
    public static record Unconditional(float value) implements WeaponLevelBasedValue {
        public static final MapCodec<Unconditional> CODEC = Codec.FLOAT.xmap(Unconditional::new, Unconditional::value).fieldOf("value");
        @Override
        public Float getResult(int weaponLevel, boolean applyTier) {
            return value;
        }
        @Override
        public MapCodec<? extends WeaponLevelBasedValue> codec() {
            return CODEC;
        }
    }
    public static record MatchingLevel(float value, LevelCondition levelCondition) implements WeaponLevelBasedValue {
        public static final MapCodec<MatchingLevel> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Codec.FLOAT.fieldOf("value").forGetter(MatchingLevel::value),
                LevelCondition.CODEC.fieldOf("condition").forGetter(MatchingLevel::levelCondition)).apply(instance, MatchingLevel::new));
        @Override
        public Float getResult(int weaponLevel, boolean applyTier) {
            return levelCondition.matches(weaponLevel) || !applyTier ? value : null;
        }
        @Override
        public MapCodec<? extends WeaponLevelBasedValue> codec() {
            return CODEC;
        }
    }
}
