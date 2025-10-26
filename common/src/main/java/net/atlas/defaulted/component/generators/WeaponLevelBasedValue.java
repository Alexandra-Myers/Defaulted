package net.atlas.defaulted.component.generators;

import java.util.List;
import java.util.stream.IntStream;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.atlas.defaulted.extension.LateBoundIdMapper;
import net.minecraft.resources.ResourceLocation;

public interface WeaponLevelBasedValue {
    LateBoundIdMapper<ResourceLocation, MapCodec<? extends WeaponLevelBasedValue>> ID_MAPPER = new LateBoundIdMapper<>();
    Codec<WeaponLevelBasedValue> BASE_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
		.dispatch(WeaponLevelBasedValue::codec, mapCodec -> mapCodec);
    static void bootstrap() {
        LevelCondition.bootstrap();
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("unconditional"), Unconditional.CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("lookup"), Lookup.CODEC);
        ID_MAPPER.put(ResourceLocation.withDefaultNamespace("linear"), Linear.CODEC);
    }
    Codec<WeaponLevelBasedValue> CODEC = Codec.withAlternative(BASE_CODEC, Codec.FLOAT.xmap(Unconditional::new, Unconditional::value));
    Float getResult(int weaponLevel, float addedValue, boolean applyTier);
    default Float getResult(int weaponLevel, boolean applyTier) {
        return getResult(weaponLevel, 0, applyTier);
    }
    MapCodec<? extends WeaponLevelBasedValue> codec();
    interface LevelCondition {
        LateBoundIdMapper<ResourceLocation, MapCodec<? extends LevelCondition>> ID_MAPPER = new LateBoundIdMapper<>();
        Codec<LevelCondition> BASE_CODEC = ID_MAPPER.codec(ResourceLocation.CODEC)
                .dispatch(LevelCondition::codec, mapCodec -> mapCodec);
        Codec<LevelCondition> CODEC = Codec.withAlternative(BASE_CODEC, Codec.INT.xmap(i -> new ClampedCondition(i, i), ClampedCondition::min));
        static void bootstrap() {
            ID_MAPPER.put(ResourceLocation.withDefaultNamespace("clamped"), ClampedCondition.CODEC);
            ID_MAPPER.put(ResourceLocation.withDefaultNamespace("list"), ListCondition.CODEC);
        }
        boolean matches(int value);
        MapCodec<? extends LevelCondition> codec();
    }
    record ClampedCondition(int min, int max) implements LevelCondition {
        public static final MapCodec<ClampedCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(Codec.INT.fieldOf("min").forGetter(ClampedCondition::min),
                Codec.INT.fieldOf("max").forGetter(ClampedCondition::max)).apply(instance, ClampedCondition::new));
        @Override
        public boolean matches(int value) {
            return value >= min && value <= max;
        }

        @Override
        public MapCodec<? extends LevelCondition> codec() {
            return CODEC;
        }
    }
    record ListCondition(IntStream values) implements LevelCondition {
        public static final MapCodec<ListCondition> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(Codec.INT_STREAM.fieldOf("values").forGetter(ListCondition::values))
                        .apply(instance, ListCondition::new));
        @Override
        public boolean matches(int value) {
            return values.anyMatch(val -> val == value);
        }

        @Override
        public MapCodec<? extends LevelCondition> codec() {
            return CODEC;
        }
    }
    record Unconditional(float value) implements WeaponLevelBasedValue {
        public static final MapCodec<Unconditional> CODEC = Codec.FLOAT.xmap(Unconditional::new, Unconditional::value).fieldOf("value");
        @Override
        public Float getResult(int weaponLevel, float addedValue, boolean applyTier) {
            return value + (applyTier ? addedValue : 0);
        }
        @Override
        public MapCodec<? extends WeaponLevelBasedValue> codec() {
            return CODEC;
        }
    }
    record Lookup(List<MatchingLevel> values, Float fallback) implements WeaponLevelBasedValue {
        public static final MapCodec<Lookup> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(MatchingLevel.CODEC.listOf().fieldOf("values").forGetter(Lookup::values),
                        Codec.FLOAT.fieldOf("fallback").forGetter(Lookup::fallback)).apply(instance, Lookup::new));
        @Override
        public Float getResult(int weaponLevel, float addedValue, boolean applyTier) {
            for (MatchingLevel value : values) {
                Float res = value.getResult(weaponLevel, addedValue, applyTier);
                if (res != null) return value.getResult(weaponLevel, addedValue, applyTier);
            }
            return fallback + (applyTier ? addedValue : 0);
        }
        @Override
        public MapCodec<? extends WeaponLevelBasedValue> codec() {
            return CODEC;
        }
    }
    record MatchingLevel(WeaponLevelBasedValue value, LevelCondition levelCondition) {
        public static final Codec<MatchingLevel> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(WeaponLevelBasedValue.CODEC.fieldOf("value").forGetter(MatchingLevel::value),
                LevelCondition.CODEC.fieldOf("condition").forGetter(MatchingLevel::levelCondition)).apply(instance, MatchingLevel::new));
        public Float getResult(int weaponLevel, float addedValue, boolean applyTier) {
            return levelCondition.matches(weaponLevel) || !applyTier ? value.getResult(weaponLevel, addedValue, applyTier) : null;
        }
    }
    record Linear(WeaponLevelBasedValue base, WeaponLevelBasedValue perLevelAboveFirst) implements WeaponLevelBasedValue {
        public static final MapCodec<Linear> CODEC = RecordCodecBuilder.mapCodec(instance ->
                instance.group(WeaponLevelBasedValue.CODEC.fieldOf("base").forGetter(Linear::base),
                        WeaponLevelBasedValue.CODEC.fieldOf("per_level_above_first").forGetter(Linear::perLevelAboveFirst)).apply(instance, Linear::new));

        @Override
        public Float getResult(int weaponLevel, float addedValue, boolean applyTier) {
            return base.getResult(weaponLevel, addedValue, applyTier) + weaponLevel * perLevelAboveFirst.getResult(weaponLevel, addedValue, applyTier);
        }

        @Override
        public MapCodec<? extends WeaponLevelBasedValue> codec() {
            return CODEC;
        }
    }
}
