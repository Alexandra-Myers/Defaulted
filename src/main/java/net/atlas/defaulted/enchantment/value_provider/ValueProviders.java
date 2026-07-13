package net.atlas.defaulted.enchantment.value_provider;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.atlas.defaulted.Defaulted;
import net.atlas.defaulted.init.registry.Bootstrapper;

public class ValueProviders extends Bootstrapper<MapCodec<? extends ValueProvider>> {
    public static final ValueProviders INSTANCE = new ValueProviders();
    public static final Codec<ValueProvider> DISPATCH_CODEC = INSTANCE.getRegistry().byNameCodec()
            .dispatch(ValueProvider::type, mapCodec -> mapCodec);
    public static final Codec<ValueProvider> CODEC = Codec.either(Constant.CODEC, DISPATCH_CODEC)
            .xmap((either) -> either.map((left) -> left, (right) -> right), (valueProvider) -> valueProvider instanceof Constant constant ? Either.left(constant) : Either.right(valueProvider));

    public ValueProviders() {
        super(Defaulted.key("value_providers"), "minecraft");
    }

    public void bootstrap(Registrar<MapCodec<? extends ValueProvider>> registrar) {
        registrar.register("constant", () -> Constant.MAP_CODEC);
        registrar.register("function", () -> MathFunction.CODEC);
        registrar.register("invert", () -> Invert.CODEC);
        registrar.register("variable", () -> Variable.CODEC);
    }
}
