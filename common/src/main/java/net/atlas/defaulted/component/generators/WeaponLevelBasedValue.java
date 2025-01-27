package net.atlas.defaulted.component.generators;

import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public interface WeaponLevelBasedValue {
    public Codec<List<WeaponLevelBasedValue>> CODEC = Codec.withAlternative(MatchingLevel.CODEC.xmap(matchingLevel -> (WeaponLevelBasedValue) matchingLevel, weaponLevelBasedValue -> (MatchingLevel) weaponLevelBasedValue).listOf(), Codec.FLOAT.xmap(Unconditional::new, Unconditional::value).xmap(unconditional -> (WeaponLevelBasedValue) unconditional, weaponLevelBasedValue -> (Unconditional) weaponLevelBasedValue), Collections::singletonList);
    float getResult(int weaponLevel, boolean applyTier);
    public static record LevelCondition(int min, int max) {
        public static final Codec<LevelCondition> CODEC = Codec.withAlternative(RecordCodecBuilder.create(instance ->
            instance.group(Codec.INT.fieldOf("min").forGetter(LevelCondition::min),
                Codec.INT.fieldOf("max").forGetter(LevelCondition::max)).apply(instance, LevelCondition::new)), Codec.INT, i -> new LevelCondition(i, i));
        public boolean matches(int value) {
            return value >= min && value <= max;
        }
    }
    public static record Unconditional(float value) implements WeaponLevelBasedValue {
        @Override
        public float getResult(int weaponLevel, boolean applyTier) {
            return value;
        }
    }
    public static record MatchingLevel(float value, LevelCondition levelCondition) implements WeaponLevelBasedValue {
        public static final Codec<MatchingLevel> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(Codec.FLOAT.fieldOf("value").forGetter(MatchingLevel::value),
                LevelCondition.CODEC.fieldOf("condition").forGetter(MatchingLevel::levelCondition)).apply(instance, MatchingLevel::new));
        @Override
        public float getResult(int weaponLevel, boolean applyTier) {
            return levelCondition.matches(weaponLevel) || !applyTier ? value : 0;
        }
    }
}
