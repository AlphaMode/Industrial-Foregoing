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
package com.buuz135.industrial.block.misc.tile;

import com.buuz135.industrial.block.tile.IndustrialMachineTile;
import com.buuz135.industrial.item.infinity.InfinityEnergyStorage;
import com.buuz135.industrial.module.ModuleMisc;
import com.hrznstudio.titanium.annotation.Save;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.component.inventory.SidedInventoryComponent;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemStackHandlerContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import team.reborn.energy.api.EnergyStorage;

public class InfinityChargerTile extends IndustrialMachineTile<InfinityChargerTile> {

    @Save
    private SidedInventoryComponent<InfinityChargerTile> chargingSlot;

    public InfinityChargerTile(BlockPos blockPos, BlockState blockState) {
        super(ModuleMisc.INFINITY_CHARGER, blockPos, blockState);
        addInventory(chargingSlot = (SidedInventoryComponent<InfinityChargerTile>) new SidedInventoryComponent<InfinityChargerTile>("charging", 80, 40, 1, 0)
                .setColor(DyeColor.BLUE)
                .setSlotLimit(1)
                .setInputFilter((stack, integer) -> ContainerItemContext.withInitial(stack).find(EnergyStorage.ITEM) != null)
        );
    }

    @Override
    public void serverTick(Level level, BlockPos pos, BlockState state, InfinityChargerTile blockEntity) {
        if (!chargingSlot.getStackInSlot(0).isEmpty() && this.getRedstoneManager().getAction().canRun(this.getEnvironmentValue(false, null)) && this.getRedstoneManager().shouldWork()) {
            EnergyStorage iEnergyStorage = new ItemStackHandlerContainerItemContext(chargingSlot, 0).find(EnergyStorage.ITEM);
            if (iEnergyStorage != null) {
                if (this.getEnergyStorage() instanceof InfinityEnergyStorage) {
                    if (iEnergyStorage instanceof InfinityEnergyStorage) {
                        long added = Math.min(Long.MAX_VALUE - ((InfinityEnergyStorage) iEnergyStorage).getAmount(), Math.min(this.getEnergyStorage().getCapacity(), this.getEnergyStorage().getAmount()));
                        ((InfinityEnergyStorage) iEnergyStorage).setEnergyStored(((InfinityEnergyStorage) iEnergyStorage).getAmount() + added);
                        ((InfinityEnergyStorage<InfinityChargerTile>) this.getEnergyStorage()).setEnergyStored(this.getEnergyStorage().getAmount() - added);
                        markForUpdate();
                    } else {
                        long extracted = this.getEnergyStorage().getAmount();
                        try (Transaction t = TransferUtil.getTransaction()) {
                            this.getEnergyStorage().setEnergyStored(this.getEnergyStorage().getAmount() - iEnergyStorage.insert(extracted, t));
                            t.commit();
                        }
                        markForUpdate();
                    }
                }
            }
            this.getRedstoneManager().finish();
        }
    }

    @Override
    public InfinityChargerTile getSelf() {
        return this;
    }

    @Override
    protected EnergyStorageComponent<InfinityChargerTile> createEnergyStorage() {
        return new InfinityEnergyStorage<>(1_000_000_000_000L, 10, 20);
    }
}
