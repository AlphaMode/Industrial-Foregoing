///*
// * This file is part of Industrial Foregoing.
// *
// * Copyright 2021, Buuz135
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy of
// * this software and associated documentation files (the "Software"), to deal in the
// * Software without restriction, including without limitation the rights to use, copy,
// * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
// * and to permit persons to whom the Software is furnished to do so, subject to the
// * following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all copies
// * or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
// * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
// * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
// * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
// * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// */
//
//package com.buuz135.industrial.item.infinity;
//
//import com.hrznstudio.titanium.api.IFactory;
//import com.hrznstudio.titanium.api.capability.IStackHolder;
//import com.hrznstudio.titanium.capability.CapabilityItemStackHolder;
//import com.hrznstudio.titanium.capability.FluidHandlerScreenProviderItemStack;
//import com.hrznstudio.titanium.capability.ItemStackHolderCapability;
//import dev.onyxstudios.cca.api.v3.component.ComponentProvider;
//import net.minecraft.core.Direction;
//import net.minecraft.world.item.ItemStack;
//
//import javax.annotation.Nonnull;
//import javax.annotation.Nullable;
//
//
//public class InfinityCapabilityProvider implements ComponentProvider {
//
//    private final FluidHandlerScreenProviderItemStack tank;
//    private final InfinityEnergyStorage energyStorage;
//    private final ItemStackHolderCapability itemStackHolder;
//    private final LazyOptional<IEnergyStorage> energyStorageCap;
//    private final LazyOptional<IFluidHandlerItem> tankCap;
//    private final LazyOptional<IStackHolder> stackCap;
//    private final ItemStack stack;
//
//    public InfinityCapabilityProvider(ItemStack stack, IFactory<? extends FluidHandlerScreenProviderItemStack> tankFactory, IFactory<InfinityEnergyStorage> energyFactory) {
//        this.tank = tankFactory.create();
//        this.energyStorage = energyFactory.create();
//        this.itemStackHolder = new InfinityStackHolder();
//        this.tankCap = LazyOptional.of(() -> tank);
//        this.energyStorageCap = LazyOptional.of(() -> energyStorage);
//        this.stackCap = LazyOptional.of(() -> itemStackHolder);
//        this.stack = stack;
//    }
//
//    @Nullable
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
//        if (capability == ForgeCapabilities.FLUID_HANDLER_ITEM) return tankCap.cast();
//        if (capability == ForgeCapabilities.ENERGY) return energyStorageCap.cast();
//        if (capability == CapabilityItemStackHolder.ITEMSTACK_HOLDER_CAPABILITY) return stackCap.cast();
//        return LazyOptional.empty();
//    }
//
//    public ItemStack getStack() {
//        return stack;
//    }
//}
