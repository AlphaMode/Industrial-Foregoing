/*
 * This file is part of Industrial Foregoing.
 *
 * Copyright 2021, Buuz135
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in the
 * Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 * and to permit persons to whom the Software is furnished to do so, subject to the
 * following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies
 * or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.buuz135.industrial.capability;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.item.FluidHandlerItemStack;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import javax.annotation.Nonnull;

public class BlockFluidHandlerItemStack extends FluidHandlerItemStack.SwapEmpty {

    private String tagName;

    public BlockFluidHandlerItemStack(ContainerItemContext container, ItemStack emptyContainer, int capacity, String tagName) {
        super(container, emptyContainer, capacity);
        this.tagName = tagName;
    }

    @Override
    @Nonnull
    public FluidStack getFluid() {
        CompoundTag tagCompound = container.getItemVariant().getNbt();
        if (tagCompound == null || !tagCompound.contains("BlockEntityTag") || !tagCompound.getCompound("BlockEntityTag").contains(tagName)) {
            return FluidStack.EMPTY;
        }
        return FluidStack.loadFluidStackFromNBT(tagCompound.getCompound("BlockEntityTag").getCompound(tagName));
    }

    @Override
    protected boolean setFluid(FluidStack fluid, TransactionContext tx) {
        ItemStack newStack = container.getItemVariant().toStack();
        if (!container.getItemVariant().hasNbt()) {
            CompoundTag compoundNBT = new CompoundTag();
            CompoundTag blockEntityTag = new CompoundTag();
            compoundNBT.put("BlockEntityTag", blockEntityTag);
            newStack.setTag(compoundNBT);
        }

        CompoundTag fluidTag = new CompoundTag();
        fluid.writeToNBT(fluidTag);
        newStack.getTag().getCompound("BlockEntityTag").put(tagName, fluidTag);
        if (container.exchange(ItemVariant.of(newStack), 1, tx) == 1)
            return true;
        return false;
    }
}
