package net.atlas.defaulted.neoforge;

import net.neoforged.fml.common.Mod;

import net.atlas.defaulted.Defaulted;

@Mod(Defaulted.MOD_ID)
public final class DefaultedNeoForge {
    public DefaultedNeoForge() {
        // Run our common setup.
        Defaulted.init();
    }
}
