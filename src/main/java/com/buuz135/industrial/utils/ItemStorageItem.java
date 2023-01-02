package com.buuz135.industrial.utils;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.item.ItemStack;

public interface ItemStorageItem {
    Storage<ItemVariant> getItemStorage(ItemStack stack, ContainerItemContext context);
}
