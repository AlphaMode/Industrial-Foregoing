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

import com.buuz135.industrial.api.straw.StrawHandler;
import com.buuz135.industrial.utils.IndustrialTags;
import com.buuz135.industrial.utils.StrawUtils;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

;

public class ItemStraw extends IFCustomItem {

    public ItemStraw(CreativeModeTab group) {
        super("straw", group, new Properties().stacksTo(1));
    }

    @Override
    @Nonnull
    public ItemStack finishUsingItem(@Nonnull ItemStack heldStack, Level world, LivingEntity entity) {
        if (!world.isClientSide && entity instanceof Player) {
            Player player = (Player) entity;
            HitResult result = getPlayerPOVHitResult(world, player, ClipContext.Fluid.SOURCE_ONLY);
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockRayTraceResult = (BlockHitResult) result;
                BlockPos pos = blockRayTraceResult.getBlockPos();
                BlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                FluidState fluidState = state.getFluidState();
                if (fluidState != Fluids.EMPTY.defaultFluidState() && block instanceof BucketPickup && fluidState.isSource()) {
                    StrawUtils.getStrawHandler(fluidState.getType()).ifPresent(handler -> {
                        ItemStack stack = ((BucketPickup) block).pickupBlock(world, pos, state);
                        TransferUtil.getFluidContained(stack).ifPresent(fluidStack -> {
                            if (!fluidStack.isEmpty()) {
                                handler.onDrink(world, pos, fluidStack.getFluid(), player, false);
                            }
                        });

                    });
                    return heldStack;
                }
                BlockEntity tile = world.getBlockEntity(pos);
                Storage<FluidVariant> handler = tile != null ? TransferUtil.getFluidStorage(tile) : TransferUtil.getFluidStorage(world, pos);
                if (handler != null) {
                    for (StorageView<FluidVariant> storageView : handler) {
                        FluidStack stack = new FluidStack(storageView);
                        if (!stack.isEmpty()) {
                            Fluid fluidInstance = stack.getFluid();
                            Optional<StrawHandler> strawHandler = StrawUtils.getStrawHandler(fluidInstance);
                            if (strawHandler.isPresent() && stack.getAmount() >= FluidConstants.BUCKET) {
                                FluidStack out = TransferUtil.simulateExtractAnyFluid(handler, FluidConstants.BUCKET);
                                if (!out.isEmpty() && out.getAmount() >= FluidConstants.BUCKET) {
                                    strawHandler.ifPresent(straw -> straw.onDrink(world, pos, out.getFluid(), player, true));
                                    TransferUtil.extractAnyFluid(handler, FluidConstants.BUCKET);
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.finishUsingItem(heldStack, world, entity);
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, @Nonnull InteractionHand handIn) {
        HitResult result = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.SOURCE_ONLY);
        if (result != null && result.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) result;
            BlockPos pos = blockRayTraceResult.getBlockPos();
            BlockState state = worldIn.getBlockState(pos);
            Block block = state.getBlock();
            FluidState fluid = state.getFluidState();//FluidRegistry.lookupFluidForBlock(block);
            if (fluid != null) {
                Optional<StrawHandler> handler = StrawUtils.getStrawHandler(fluid.getType());
                if (handler.isPresent()) {
                    playerIn.startUsingItem(handIn);
                    return InteractionResultHolder.success(playerIn.getItemInHand(handIn)); //success accepted
                }
            }
            BlockEntity tile = worldIn.getBlockEntity(pos);
            Storage<FluidVariant> handler = tile == null ? TransferUtil.getFluidStorage(worldIn, pos) : TransferUtil.getFluidStorage(tile);
            if (handler != null) {
                for (StorageView<FluidVariant> storageView : handler) {
                    FluidStack stack = new FluidStack(storageView);
                    if (!stack.isEmpty()) {
                        Fluid fluidInstance = stack.getFluid();
                        Optional<StrawHandler> strawHandler = StrawUtils.getStrawHandler(fluidInstance);
                        if (strawHandler.isPresent() && stack.getAmount() >= FluidConstants.BUCKET) {
                            try (Transaction tx = TransferUtil.getTransaction()) {
                                long out = handler.simulateExtract(stack.getType(), stack.getAmount(), tx);
                                if (out >= FluidConstants.BUCKET) {
                                    playerIn.startUsingItem(handIn);
                                    return new InteractionResultHolder(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
                                }
                            }
                        }
                    }
                }
            }
        }
        return super.use(worldIn, playerIn, handIn);
    }

    @Override
    public int getUseDuration(ItemStack p_77626_1_) {
        return 30;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack p_77661_1_) {
        return UseAnim.DRINK;
    }

    @Override
    public boolean hasTooltipDetails(@Nullable Key key) {
        return key == null;
    }

    @Override
    public void addTooltipDetails(@Nullable Key key, ItemStack stack, List<Component> tooltip, boolean advanced) {
        super.addTooltipDetails(key, stack, tooltip, advanced);
        tooltip.add(Component.literal(ChatFormatting.GRAY + "\"The One Who Codes\""));
    }

    @Override
    public void registerRecipe(Consumer<FinishedRecipe> consumer) {
        TitaniumShapedRecipeBuilder.shapedRecipe(this)
                .pattern("PP ").pattern(" P ").pattern(" P ")
                .define('P', IndustrialTags.Items.PLASTIC)
                .save(consumer);
    }
}
