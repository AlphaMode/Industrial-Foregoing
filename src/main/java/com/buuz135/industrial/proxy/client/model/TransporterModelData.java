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
package com.buuz135.industrial.proxy.client.model;

import com.buuz135.industrial.api.transporter.TransporterType;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Map;

public class TransporterModelData {

    public static final ModelProperty<TransporterModelData> UPGRADE_PROPERTY = new ModelProperty<TransporterModelData>();
    private Map<Direction, TransporterType> upgrades;

    public TransporterModelData(Map<Direction, TransporterType> upgrades) {
        this.upgrades = upgrades;
    }

    public Map<Direction, TransporterType> getUpgrades() {
        return upgrades;
    }
}
