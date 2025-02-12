package net.atlas.defaulted;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;

import net.atlas.defaulted.component.ItemPatches;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

public class DefaultedDataReloadListener {
    public static List<ItemPatches> cached = new ArrayList<>();
    public static void reload(HolderLookup.Provider lookup, ResourceManager manager) {
        List<ItemPatchesEntry> entries = new ArrayList<>();
        String filePath = "defaulted/default_component_patches";
        Map<ResourceLocation, Resource> resources = manager.listResources(filePath, resourceLocation -> resourceLocation.getPath().endsWith(".json"));
        for(Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation resourceLocation = entry.getKey();
            String path = resourceLocation.getPath().substring(0, resourceLocation.getPath().length() - ".json".length());
            if (path.startsWith(filePath + '/')) path = path.substring((filePath + '/').length());
            ResourceLocation id = resourceLocation.withPath(path);
            try(BufferedReader reader = entry.getValue().openAsReader()) {
                JsonObject patchObject = JsonParser.parseReader(new JsonReader(reader)).getAsJsonObject();
                ItemPatches result = ItemPatches.DIRECT_CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, lookup), patchObject).getOrThrow();
                entries.add(new ItemPatchesEntry(id, result));
            } catch(Exception e) {
                Defaulted.LOGGER.error("Error occurred while loading item patches... \nLocation:" + id.toString(), e);
            }
        }
        cached = entries.stream().sorted(Comparator.naturalOrder()).map(ItemPatchesEntry::itemPatches).toList();
        Defaulted.patchItemComponents(cached);
        Defaulted.EXECUTE_ON_RELOAD.forEach(collectionConsumer -> collectionConsumer.accept(cached));
    }
    public record ItemPatchesEntry(ResourceLocation id, ItemPatches itemPatches) implements Comparable<ItemPatchesEntry> {
        @Override
        public int compareTo(ItemPatchesEntry other) {
            int priority = itemPatches.compareTo(other.itemPatches);
            return priority == 0 ? id.compareTo(other.id) : priority;
        }

    }
}
