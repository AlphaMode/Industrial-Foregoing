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

package com.buuz135.industrial.utils;

import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRenderHandler;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluid;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import java.util.concurrent.ConcurrentHashMap;

public class FluidUtils {
    // TODO
    // should item colors from ItemStackUtils also be cached?
    // invalidate cache on resource reload
    public static ConcurrentHashMap<ResourceLocation, Integer> colorCache = new ConcurrentHashMap<>();

    public static int getFluidColor(FluidStack stack) {
        FluidVariantRenderHandler renderProperties = FluidVariantRendering.getHandlerOrDefault(stack.getFluid());
        TextureAtlasSprite location = renderProperties.getSprites(stack.getType())[0];
        int tint = renderProperties.getColor(stack.getType(), null, null);
        int textureColor = colorCache.computeIfAbsent(location.getName(), ColorUtils::getColorFrom);
        return FastColor.ARGB32.multiply(textureColor, tint);
    }

    public static int getFluidColor(Fluid fluid) {
        FluidVariantRenderHandler renderProperties = FluidVariantRendering.getHandlerOrDefault(fluid);
        ResourceLocation location = renderProperties.getSprites(FluidVariant.of(fluid))[0].getName();
        int tint = renderProperties.getColor(FluidVariant.of(fluid), null, null);
        int textureColor = colorCache.computeIfAbsent(location, ColorUtils::getColorFrom);
        return FastColor.ARGB32.multiply(textureColor, tint);
    }
}