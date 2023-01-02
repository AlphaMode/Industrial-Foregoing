package com.buuz135.industrial.utils;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

public interface FluidStorageItem {
    Storage<FluidVariant> getFluidStorage(ItemStack stack, ContainerItemContext context);
}
