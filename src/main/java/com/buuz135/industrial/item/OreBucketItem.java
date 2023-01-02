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


import com.buuz135.industrial.utils.FluidStorageItem;
import io.github.fabricators_of_create.porting_lib.mixin.common.accessor.BucketItemAccessor;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.item.FluidBucketWrapper;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class OreBucketItem extends BucketItem implements FluidStorageItem {

    private static final String NBT_TAG = "Tag";

    public OreBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
        super(supplier.get(), builder);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.hasTag() && stack.getTag().contains(NBT_TAG)) return InteractionResultHolder.pass(stack);
        return super.use(world, player, hand);
    }

    @Override
    public Storage<FluidVariant> getFluidStorage(ItemStack stack, ContainerItemContext context) {
        return new FluidBucketWrapper(context) {
            @Nonnull
            @Override
            public FluidStack getFluid() {
                FluidStack stack = new FluidStack(((BucketItemAccessor)OreBucketItem.this).port_lib$getContent(), FluidConstants.BUCKET);
                if (context.getItemVariant().copyOrCreateNbt().contains(NBT_TAG)) {
                    String tag = context.getItemVariant().copyOrCreateNbt().getString(NBT_TAG);
                    stack.getOrCreateTag().putString(NBT_TAG, tag);
                }
                return stack;
            }
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent displayName = Component.translatable(this.getDescriptionId(stack));
        // TODO: 26/07/2021 Fix
//        if (stack.hasTag() && stack.getTag().contains(NBT_TAG)) {
//            String tag = stack.getTag().getString(NBT_TAG);
//            List<Item> items = SerializationTags.getInstance().getItems().getTagOrEmpty(new ResourceLocation(tag)).getValues();
//            if (items.size() > 0) {
//                TranslatableComponent oreDisplayName = Component.translatable(items.get(0).getDescriptionId());
//                return displayName.append(" (").append(oreDisplayName).append(")");
//            }
//        }
        return displayName;
    }

    /*
    This will not work  due to problems with how forge tags interact with the creative search initialization.
    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));
            ITagCollection<Item> tags = TagCollectionManager.getManager().getItemTags();
            for (ResourceLocation loc : tags.getIDTagMap().keySet()) {
                String tagName = loc.toString();
                if (!tagName.startsWith("c:ores/"))
                    continue;
                ItemStack stack = new ItemStack(this);
                stack.getOrCreateTag().putString(NBT_TAG, tagName);
                items.add(stack);
            }
        }
    }
     */
}
