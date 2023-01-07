package com.buuz135.industrial.utils;

import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.minecraft.world.item.ItemStack;
import team.reborn.energy.api.EnergyStorage;

public interface EnergyStorageItem {
    EnergyStorage getEnergyStorage(ItemStack stack, ContainerItemContext context);
}
