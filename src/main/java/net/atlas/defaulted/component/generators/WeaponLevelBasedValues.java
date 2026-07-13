package net.atlas.defaulted.component.generators;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.init.registry.Bootstrapper;
import net.atlas.defaulted.component.generators.WeaponLevelBasedValue.*;

public class WeaponLevelBasedValues extends Bootstrapper<MapCodec<? extends WeaponLevelBasedValue>> {
    public static final WeaponLevelBasedValues INSTANCE = new WeaponLevelBasedValues();
    public static final Codec<WeaponLevelBasedValue> DISPATCH_CODEC = INSTANCE.getRegistry().byNameCodec()
            .dispatch(WeaponLevelBasedValue::codec, mapCodec -> mapCodec);
    public static final Codec<WeaponLevelBasedValue> CODEC = Codec.either(Codec.FLOAT.xmap(Constant::new, Constant::value), DISPATCH_CODEC)
            .xmap((either) -> either.map((left) -> left, (right) -> right), (weaponLevelBasedValue) -> weaponLevelBasedValue instanceof Constant constant ? Either.left(constant) : Either.right(weaponLevelBasedValue));

    public WeaponLevelBasedValues() {
        super(Defaulted.key("weapon_level_based_values"), "minecraft");
    }

    public void bootstrap(Registrar<MapCodec<? extends WeaponLevelBasedValue>> registrar) {
        registrar.register("constant", () -> Constant.CODEC);
        registrar.register("lookup", () -> Lookup.CODEC);
        registrar.register("linear", () -> Linear.CODEC);
    }
}
