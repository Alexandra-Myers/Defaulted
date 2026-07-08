package net.atlas.defaulted.utils;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;

public class Codecs {
    public static <E> Codec<List<E>> compactListCodec(Codec<E> elementCodec) {
        return compactListCodec(elementCodec, elementCodec.listOf());
    }
    public static <E> Codec<List<E>> compactListCodec(Codec<E> elementCodec, Codec<List<E>> listCodec) {
        return Codec.either(listCodec, elementCodec).xmap((either) -> either.map((list) -> list, List::of), (list) -> list.size() == 1 ? Either.right(list.getFirst()) : Either.left(list));
    }
}
