package com.buuz135.industrial.utils;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class FabricUtils {
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
