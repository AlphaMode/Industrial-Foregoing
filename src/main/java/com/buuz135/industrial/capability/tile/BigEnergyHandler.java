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
package com.buuz135.industrial.capability.tile;

import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.component.energy.EnergyStorageComponent;
import com.hrznstudio.titanium.container.addon.IContainerAddon;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;

public abstract class BigEnergyHandler<T extends IComponentHarness> extends EnergyStorageComponent<T> {

    public BigEnergyHandler(long maxCapacity, int xPos, int yPos) {
        super(maxCapacity, xPos, yPos);
    }

    public BigEnergyHandler(long maxCapacity, long maxIO, int xPos, int yPos) {
        super(maxCapacity, maxIO, xPos, yPos);
    }

    public BigEnergyHandler(long maxCapacity, long maxReceive, long maxExtract, int xPos, int yPos) {
        super(maxCapacity, maxReceive, maxExtract, xPos, yPos);
    }

    @Nonnull
    @Override
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        return Collections.emptyList();
    }

    @Override
    protected void onFinalCommit() {
        super.onFinalCommit();
        if (amount > 0) {
            this.sync();
        }
    }

    public void setEnergyStored(long energy) {
        // TODO: is this safe without a transaction?
        if (energy > this.getCapacity()) {
            this.amount = this.getCapacity();
        } else {
            this.amount = Math.max(energy, 0);
        }
        this.sync();
    }

    public abstract void sync();

}
