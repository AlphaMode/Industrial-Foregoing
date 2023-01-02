package com.buuz135.industrial.utils;

import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FabricUtils {
    public static final ItemApiLookup<Storage<ItemVariant>, ContainerItemContext> ITEM =
            ItemApiLookup.get(new ResourceLocation("fabric:item_storage"), Storage.asClass(), ContainerItemContext.class);

    public static int getBurnTime(ItemStack stack, RecipeType<?> type) {
        if (stack.isEmpty())
            return 0;
        Integer time = FuelRegistry.INSTANCE.get(stack.getItem());
        return time != null ? time : 0;
    }

    public static <V> Optional<Holder<V>> getReverseTag(Registry<V> registry, @NotNull V value) {
        return registry.getHolder(registry.getResourceKey(value).get());
    }
}
