package net.atlas.defaulted.component.generators;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue.LevelCondition;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue.*;
import net.atlas.defaulted.init.registry.Bootstrapper;

public class LevelConditions extends Bootstrapper<MapCodec<? extends LevelCondition>> {
    public static final LevelConditions INSTANCE = new LevelConditions();
    public static final Codec<LevelCondition> DISPATCH_CODEC = INSTANCE.getRegistry().byNameCodec()
            .dispatch(LevelCondition::codec, mapCodec -> mapCodec);
    public static final Codec<LevelCondition> CODEC = Codec.either(Codec.INT.xmap(i -> new ClampedCondition(i, i), ClampedCondition::min), DISPATCH_CODEC)
            .xmap((either) -> either.map((left) -> left, (right) -> right), (levelCondition) -> levelCondition instanceof ClampedCondition clampedCondition ? Either.left(clampedCondition) : Either.right(levelCondition));

    public LevelConditions() {
        super(Defaulted.key("weapon_level_conditions"), "minecraft");
    }

    public void bootstrap(Registrar<MapCodec<? extends LevelCondition>> registrar) {
        registrar.register("clamped", () -> ClampedCondition.CODEC);
        registrar.register("list", () -> ListCondition.CODEC);
    }
}
