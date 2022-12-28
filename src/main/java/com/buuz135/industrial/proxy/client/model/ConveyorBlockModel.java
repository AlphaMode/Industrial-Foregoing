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

import com.buuz135.industrial.api.conveyor.ConveyorUpgrade;
import com.buuz135.industrial.block.transportstorage.ConveyorBlock;
import com.buuz135.industrial.module.ModuleTransportStorage;
import io.github.fabricators_of_create.porting_lib.model.data.ModelData;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class ConveyorBlockModel extends ForwardingBakedModel {

    public ConveyorBlockModel(BakedModel previousConveyor) {
        wrapped = previousConveyor;
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        if (state == null || !(blockView instanceof RenderAttachedBlockView view && view.getBlockEntityRenderAttachment(pos) instanceof ModelData extraData && extraData.has(ConveyorModelData.UPGRADE_PROPERTY))) return;
        for (ConveyorUpgrade upgrade : extraData.get(ConveyorModelData.UPGRADE_PROPERTY).getUpgrades().values()) {
            if (upgrade == null)
                continue;
            BakedModel model = ModuleTransportStorage.CONVEYOR_UPGRADES_CACHE.get(upgrade.getFactory().getModel(upgrade.getSide(), state.getValue(ConveyorBlock.FACING)));
            ((FabricBakedModel)model).emitBlockQuads(blockView, state, pos, randomSupplier, context);
        }
        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }
}