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

package com.buuz135.industrial.block.transportstorage.tile;

import com.buuz135.industrial.block.transportstorage.BlackHoleTankBlock;
import com.buuz135.industrial.block.transportstorage.BlackHoleUnitBlock;
import com.buuz135.industrial.module.ModuleTransportStorage;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.block.BasicTileBlock;
import com.hrznstudio.titanium.block.tile.ActiveTile;
import com.hrznstudio.titanium.component.inventory.InventoryComponent;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class BlackHoleControllerTile extends ActiveTile<BlackHoleControllerTile> {

    @Save
    private InventoryComponent<BlackHoleControllerTile> units_storage;

//    private BlackHoleControllerInventory inventory;
//    private BlackHoleControllerTank tank;

    public BlackHoleControllerTile(BlockPos blockPos, BlockState blockState) {
        super((BasicTileBlock<BlackHoleControllerTile>) ModuleTransportStorage.BLACK_HOLE_CONTROLLER.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_CONTROLLER.getRight().get(), blockPos, blockState);
        addInventory(this.units_storage = new InventoryComponent<BlackHoleControllerTile>("units_storage", 53, 20, 4 * 4)
                .setSlotLimit(1)
                .setInputFilter((itemStack, integer) -> itemStack.getItem() instanceof BlackHoleTankBlock.BlackHoleTankItem || itemStack.getItem() instanceof BlackHoleUnitBlock.BlackHoleUnitItem)
                .setOutputFilter((itemStack, integer) -> false)
                .setRange(4, 4)
        );
        for (int i = 0; i < this.units_storage.getSlots(); i++) {
            this.units_storage.setSlotToColorRender(i, DyeColor.BLUE);
        }
//        this.inventory = new BlackHoleControllerInventory();
//        this.tank = new BlackHoleControllerTank();
    }

    @Nonnull
    @Override
    public BlackHoleControllerTile getSelf() {
        return this;
    }

//    @Override
//    public Storage<ItemVariant> getItemStorage(Direction side) {
//        return this.inventory;
//    }
//
//    @Override
//    public Storage<FluidVariant> getFluidStorage(Direction side) {
//        return this.tank;
//    }

    @Override
    public InteractionResult onActivated(Player playerIn, InteractionHand hand, Direction facing, double hitX, double hitY, double hitZ) {
        if (super.onActivated(playerIn, hand, facing, hitX, hitY, hitZ) == InteractionResult.SUCCESS) {
            return InteractionResult.SUCCESS;
        }
        openGui(playerIn);
        return InteractionResult.SUCCESS;
    }

    public InventoryComponent<BlackHoleControllerTile> getUnitsStorage() {
        return units_storage;
    }

//    private class BlackHoleControllerInventory implements Storage<ItemVariant> { TODO: PORT
//
//        @Override
//        public int getSlots() {
//            return 16;
//        }
//
//        @Nonnull
//        @Override
//        public ItemStack getStackInSlot(int slot) {
//            ItemStack bl = units_storage.getStackInSlot(slot);
//            if (!bl.isEmpty() && bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
//                IItemHandler handler = bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
//                return handler.getStackInSlot(0);
//            }
//            return ItemStack.EMPTY;
//        }
//
//        @Nonnull
//        @Override
//        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
//            ItemStack bl = units_storage.getStackInSlot(slot);
//            if (!bl.isEmpty() && bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
//                IItemHandler handler = bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
//                return handler.insertItem(0, stack, simulate);
//            }
//            return stack;
//        }
//
//        @Nonnull
//        @Override
//        public ItemStack extractItem(int slot, int amount, boolean simulate) {
//            ItemStack bl = units_storage.getStackInSlot(slot);
//            if (!bl.isEmpty() && bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
//                IItemHandler handler = bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
//                return handler.extractItem(0, amount, simulate);
//            }
//            return ItemStack.EMPTY;
//        }
//
//        @Override
//        public int getSlotLimit(int slot) {
//            ItemStack bl = units_storage.getStackInSlot(slot);
//            if (!bl.isEmpty() && bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
//                IItemHandler handler = bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
//                return handler.getSlotLimit(0);
//            }
//            return 0;
//        }
//
//        @Override
//        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
//            ItemStack bl = units_storage.getStackInSlot(slot);
//            if (!bl.isEmpty() && bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
//                IItemHandler handler = bl.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElse(null);
//                return handler.isItemValid(0, stack);
//            }
//            return false;
//        }
//    }

//    private class BlackHoleControllerTank extends CombinedStorage<FluidVariant, SingleSlotStorage<FluidVariant>> {
//
//        public BlackHoleControllerTank(List<SingleSlotStorage<FluidVariant>> parts) {
//            super(parts);
//        }
//
//        public int getTanks() {
//            return 16;
//        }
//
//        @Nonnull
//        @Override
//        public FluidStack getFluidInTank(int tank) {
//            ItemStack bl = units_storage.getStackInSlot(tank);
//            Storage<FluidVariant> handler = FluidStorage.ITEM.find(bl, ContainerItemContext.withInitial(bl));
//            if (!bl.isEmpty() && handler != null) {
//                return handler.getFluidInTank(0);
//            }
//            return FluidStack.EMPTY;
//        }
//
//        @Override
//        public int getTankCapacity(int tank) {
//            ItemStack bl = units_storage.getStackInSlot(tank);
//            if (!bl.isEmpty() && bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
//                IFluidHandler handler = bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
//                return handler.getTankCapacity(0);
//            }
//            return 0;
//        }
//
//        @Override
//        public boolean isFluidValid(int tank, @Nonnull FluidStack stack) {
//            ItemStack bl = units_storage.getStackInSlot(tank);
//            if (!bl.isEmpty() && bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
//                IFluidHandler handler = bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
//                return handler.isFluidValid(0, stack);
//            }
//            return false;
//        }
//
//        @Override
//        public int fill(FluidStack resource, FluidAction action) {
//            for (int i = 0; i < getTanks(); i++) {
//                ItemStack bl = units_storage.getStackInSlot(i);
//                if (!bl.isEmpty() && bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
//                    IFluidHandler handler = bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
//                    int amount = handler.fill(resource, action);
//                    if (amount > 0) return amount;
//                }
//            }
//            return 0;
//        }
//
//        @Nonnull
//        @Override
//        public FluidStack drain(FluidStack resource, FluidAction action) {
//            for (int i = 0; i < getTanks(); i++) {
//                ItemStack bl = units_storage.getStackInSlot(i);
//                if (!bl.isEmpty() && bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
//                    IFluidHandlerItem handler = bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
//                    FluidStack fluid = handler.drain(resource, action);
//                    if (!fluid.isEmpty()) {
//                        if (handler.getFluidInTank(0).isEmpty()) {
//                            units_storage.setStackInSlot(i, handler.getContainer());
//                        }
//                        return fluid;
//                    }
//                }
//            }
//            return FluidStack.EMPTY;
//        }
//
//        @Nonnull
//        @Override
//        public FluidStack drain(int maxDrain, FluidAction action) {
//            for (int i = 0; i < getTanks(); i++) {
//                ItemStack bl = units_storage.getStackInSlot(i);
//                if (!bl.isEmpty() && bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent()) {
//                    IFluidHandlerItem handler = bl.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).orElse(null);
//                    FluidStack fluid = handler.drain(maxDrain, action);
//                    if (!fluid.isEmpty()) {
//                        if (handler.getFluidInTank(0).isEmpty()) {
//                            units_storage.setStackInSlot(i, handler.getContainer());
//                        }
//                        return fluid;
//                    }
//                }
//            }
//            return FluidStack.EMPTY;
//        }
//
//        @Override
//        public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
//            return 0;
//        }
//
//        @Override
//        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
//            return 0;
//        }
//
//        @Override
//        public Iterator<StorageView<FluidVariant>> iterator() {
//            return null;
//        }
//
//        public class BlackHoleControllerIterator implements Iterator<SingleSlotStorage<FluidVariant>> {
//
//        }
//    }
}
