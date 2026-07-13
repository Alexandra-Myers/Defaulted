package net.atlas.defaulted.init.registry;

import com.google.common.base.Supplier;
import com.mojang.datafixers.util.Either;
import net.atlas.defaulted.utils.IDUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class AbstractedHolder<T> implements Holder<T>, Supplier<T> {
    protected final ResourceKey<T> key;
    private final Holder<T> delegate;

    protected AbstractedHolder(ResourceKey<T> key, Holder<T> delegate) {
        this.key = Objects.requireNonNull(key);
        this.delegate = delegate;
    }

    protected AbstractedHolder(Holder<T> delegate) {
        this(delegate.unwrapKey().orElse(null), delegate);
    }

    public @NotNull T value() {
        return this.delegate.value();
    }

    public T get() {
        return this.value();
    }

    public ResourceLocation getId() {
        return IDUtils.identifier(this.key);
    }

    public ResourceKey<T> getKey() {
        return this.key;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else {
            if (obj instanceof Holder<?> h)
                return h.kind() == Kind.REFERENCE && h.unwrapKey().equals(Optional.of(this.key));
            return false;
        }
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    @Override
    public String toString() {
        return "AbstractedHolder{" +
                "key=" + this.key +
                '}';
    }

    public boolean isBound() {
        return this.delegate.isBound();
    }

    public boolean is(ResourceLocation id) {
        return id.equals(IDUtils.identifier(this.key));
    }

    public boolean is(@NotNull ResourceKey<T> key) {
        return this.key.equals(key);
    }

    public boolean is(Predicate<ResourceKey<T>> filter) {
        return filter.test(this.key);
    }

    public boolean is(@NotNull TagKey<T> tag) {
        return this.delegate != null && this.delegate.is(tag);
    }

    @Deprecated
    public boolean is(@NotNull Holder<T> holder) {
        return this.delegate != null && this.delegate.is(holder);
    }

    public @NotNull Stream<TagKey<T>> tags() {
        return this.delegate != null ? this.delegate.tags() : Stream.empty();
    }

    public @NotNull Either<ResourceKey<T>, T> unwrap() {
        return Either.left(this.key);
    }

    public @NotNull Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(this.key);
    }

    public Holder.@NotNull Kind kind() {
        return Kind.REFERENCE;
    }

    public boolean canSerializeIn(@NotNull HolderOwner<T> owner) {
        return this.delegate != null && this.delegate.canSerializeIn(owner);
    }

    //? neoforge {
    /*public @NotNull Holder<T> getDelegate() {
        return this.delegate.getDelegate();
    }
    *///?}
}
