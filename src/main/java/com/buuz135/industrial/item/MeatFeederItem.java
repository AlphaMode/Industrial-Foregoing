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

package com.buuz135.industrial.item;

import com.buuz135.industrial.module.ModuleCore;
import com.buuz135.industrial.module.ModuleTool;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import me.alphamode.forgetags.Tags;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class MeatFeederItem extends IFCustomItem {

    public MeatFeederItem(CreativeModeTab group) {
        super("meat_feeder", group, new Properties().stacksTo(1));
    }

    @Override
    public boolean hasTooltipDetails(@Nullable Key key) {
        return key == null;
    }

    @Override
    public void addTooltipDetails(@Nullable Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        if (stack.getTag() != null && key == null) {
            tooltip.add(Component.literal(NumberFormat.getNumberInstance(Locale.ROOT).format(stack.getTag().getCompound("Fluid").getInt("Amount")) + ChatFormatting.GOLD + "/" + ChatFormatting.WHITE + NumberFormat.getNumberInstance(Locale.ROOT).format(512000) + ChatFormatting.GOLD + "mb of Meat"));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
        if (!worldIn.isClientSide && entityIn instanceof Player) {
            Player player = (Player) entityIn;
            if (player.getFoodData().needsFood() || player.getFoodData().getSaturationLevel() < 10) {
                if (stack.getItem().equals(ModuleTool.MEAT_FEEDER.get())) {
                    meatTick(ContainerItemContext.ofPlayerSlot(player, PlayerInventoryStorage.of(player).getSlot(itemSlot)), player);
                }
            }
        }
    }

    public static void meatTick(ContainerItemContext stack, Player player) {
        if (player.getFoodData().getSaturationLevel() < 20 || player.getFoodData().getFoodLevel() < 20) {
            return;
        }

        var storage = stack.find(FluidStorage.ITEM);

        try (var transaction = TransferUtil.getTransaction()) {
            storage.extract(FluidVariant.of(ModuleCore.MEAT.getSourceFluid().get()), 400, transaction);
            transaction.addOuterCloseCallback(result -> {
                if (result.wasCommitted()) {
                    player.getFoodData().eat(1, 1);
                }
            });
        }
    }

    @Override
    public void registerRecipe(Consumer<FinishedRecipe> consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("pip").pattern("gig").pattern(" i ")
                .define('p', ModuleCore.PLASTIC.get())
                .define('i', Tags.Items.INGOTS_IRON)
                .define('g', Items.GLASS_BOTTLE)
                .save(consumer);
    }
}
