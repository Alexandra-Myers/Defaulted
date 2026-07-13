package net.atlas.defaulted.init.registry;

import net.atlas.defaulted.init.DefaultedRegistries;
import net.minecraft.core.Registry;
//? fabric
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
//? neoforge {
/*import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
*///?}

import java.util.function.Supplier;

public abstract class Bootstrapper<T> {
    private final ResourceKey<Registry<T>> key;
    //? fabric
    private final String namespace;
    //? neoforge
    //private final DeferredRegister<T> deferredRegister;
    private final Registry<T> registry;

    public Bootstrapper(ResourceKey<Registry<T>> key, String namespace) {
        this.key = key;
        //? fabric
        this.namespace = namespace;
        //? neoforge
        //this.deferredRegister = DeferredRegister.create(this.key, namespace);
        this.registry = DefaultedRegistries.createRegistry(/*? fabric {*/ this.key /*?} neoforge {*/ /*this.deferredRegister *//*?}*/);
    }

    @SuppressWarnings("unused")
    public Bootstrapper(ResourceKey<Registry<T>> key, String namespace, Registry<T> registry) {
        this.key = key;
        //? fabric
        this.namespace = namespace;
        //? neoforge
        //this.deferredRegister = DeferredRegister.create(this.key, namespace);
        this.registry = registry;
    }
    public AbstractedHolder<T> register(String path, Supplier<T> value) {
        //? fabric {
        return new AbstractedHolder<>(Registry.registerForHolder(this.registry, ResourceKey.create(this.key, ResourceLocation.fromNamespaceAndPath(this.namespace, path)), value.get()));
         //?} neoforge {
        /*return new AbstractedHolder<>(this.deferredRegister.register(path, value));
        *///?}
    }
    //? neoforge {
    /*public void bootstrap(IEventBus modBus) {
    *///?} fabric {
    public void bootstrap() {
    //?}
        bootstrap(this::register);
        //? neoforge
        //this.deferredRegister.register(modBus);
    }
    protected abstract void bootstrap(Registrar<T> registrar);

    public ResourceKey<Registry<T>> getKey() {
        return key;
    }

    public Registry<T> getRegistry() {
        return registry;
    }

    @FunctionalInterface
    public interface Registrar<T> {
        AbstractedHolder<T> register(String name, Supplier<T> value);
    }
}
