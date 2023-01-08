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

package com.buuz135.industrial.plugin.jei;

import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import mezz.jei.api.helpers.IPlatformFluidHelper;
import mezz.jei.common.platform.Services;
import mezz.jei.fabric.ingredients.fluid.JeiFluidIngredient;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class JEIHelper {

    public static boolean isInstalled() {
        return false;
    }

    public static void openBlockUses(ItemStack stack) {
        if (isInstalled()) JEICustomPlugin.showUses(stack);
    }

    public static JeiFluidIngredient toIngredient(FluidStack stack) {
        return new JeiFluidIngredient(stack.getFluid(), stack.getAmount(), stack.getTag());
    }

    public static List<IJeiFluidIngredient> toIngredients(List<FluidStack> stack) {
        return stack.stream().map(fluidStack -> ((IPlatformFluidHelper<IJeiFluidIngredient>)Services.PLATFORM.getFluidHelper()).create(fluidStack.getFluid(), fluidStack.getAmount(), fluidStack.getTag())).toList();
    }
}
