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
package com.buuz135.industrial.block.transportstorage.transporter;

import com.buuz135.industrial.api.IBlockContainer;
import com.buuz135.industrial.api.transporter.FilteredTransporterType;
import com.buuz135.industrial.api.transporter.TransporterType;
import com.buuz135.industrial.api.transporter.TransporterTypeFactory;
import com.buuz135.industrial.block.transportstorage.tile.TransporterTile;
import com.buuz135.industrial.proxy.block.filter.IFilter;
import com.buuz135.industrial.proxy.block.filter.RegulatorFilter;
import com.buuz135.industrial.utils.FabricUtils;
import com.buuz135.industrial.utils.IndustrialTags;
import com.buuz135.industrial.utils.Reference;
import com.google.common.collect.Sets;
import com.hrznstudio.titanium.recipe.generator.TitaniumShapedRecipeBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import me.alphamode.forgetags.Tags;

import javax.annotation.Nonnull;
import java.util.Set;
import java.util.function.Consumer;

public class TransporterWorldType extends FilteredTransporterType<ResourceAmount<ItemVariant>, Storage<ItemVariant>> {

    private StorageView<ItemVariant> extractSlot;

    public TransporterWorldType(IBlockContainer container, TransporterTypeFactory factory, Direction side, TransporterTypeFactory.TransporterAction action) {
        super(container, factory, side, action);
        this.extractSlot = null;
    }

    @Override
    public RegulatorFilter<ResourceAmount<ItemVariant>, Storage<ItemVariant>> createFilter() {
        return new RegulatorFilter<ResourceAmount<ItemVariant>, Storage<ItemVariant>>(20, 20, 5, 3, 16, 64, 1024 * 8, "") {
            @Override
            public long matches(ResourceAmount<ItemVariant> stack, Storage<ItemVariant> itemHandler, boolean isRegulated) {
                if (isEmpty()) return stack.amount();
                long amount = 0;
                if (isRegulated) {
                    for (StorageView<ItemVariant> view : itemHandler) {
                        if (view.getResource().toStack().sameItem(stack.resource().toStack())) {
                            amount += view.getAmount();
                        }
                    }
                }

                for (IFilter.GhostSlot slot : this.getFilter()) {
                    if (stack.resource().toStack().sameItem(slot.getStack())) {
                        int maxAmount = isRegulated ? slot.getAmount() : Integer.MAX_VALUE;
                        long returnAmount = Math.min(stack.amount(), maxAmount - amount);
                        if (returnAmount > 0) return returnAmount;
                    }
                }
                return 0;
            }
        };
    }

    @Override
    public void update() {
        super.update();
        float speed = getSpeed();
        if (!getLevel().isClientSide && getLevel().getGameTime() % (Math.max(1, 4 - speed)) == 0) {
            IBlockContainer container = getContainer();
            if (getAction() == TransporterTypeFactory.TransporterAction.EXTRACT && container instanceof TransporterTile) {
                FabricUtils.getStorage(ItemStorage.SIDED, getLevel(), getPos().relative(this.getSide()), getSide().getOpposite()).ifPresent(origin -> {
                    if (extractSlot == null || !filter(this.getFilter(), this.isWhitelist(), new ResourceAmount<>(extractSlot.getResource(), extractSlot.getAmount()), origin, false))
                        findSlot(origin);
                    if (extractSlot == null)
                        return;
                    if (!extractSlot.isResourceBlank() && filter(this.getFilter(), this.isWhitelist(), new ResourceAmount<>(extractSlot.getResource(), extractSlot.getAmount()), origin, false)) {
                        int amount = (int) (1 * getEfficiency());
                        ItemStack extracted = FabricUtils.extractItemView(extractSlot, amount, false);
                        if (extracted.isEmpty()) return;
                        ItemEntity item = new ItemEntity(getLevel(), getPos().getX() + 0.5, getPos().getY() + 0.2, getPos().getZ() + 0.5, extracted);
                        item.setDeltaMovement(0, 0, 0);
                        item.setPickUpDelay(4);
                        item.setItem(extracted);
                        getLevel().addFreshEntity(item);
                    }
                });
            }
            if (getAction() == TransporterTypeFactory.TransporterAction.INSERT && container instanceof TransporterTile) {
                FabricUtils.getStorage(ItemStorage.SIDED, getLevel(), getPos().relative(this.getSide()), getSide().getOpposite()).ifPresent(origin -> {
                    for (ItemEntity item : this.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(), this.getPos().getX() + 1, this.getPos().getY() + 1, this.getPos().getZ() + 1))) {
                        if (item.isAlive()) {
                            ItemStack stack = item.getItem().copy();
                            ResourceAmount<ItemVariant> resource = new ResourceAmount<>(ItemVariant.of(stack), stack.getCount());
                            long amount = Math.min(stack.getCount(), (int) (1 * getEfficiency()));
                            stack.setCount((int) amount);
                            amount = this.getFilter().matches(resource, origin, this.isRegulated());
                            if (amount > 0) {
                                stack.setCount((int) amount);
                                if (!stack.isEmpty() && filter(this.getFilter(), this.isWhitelist(), resource, origin, this.isRegulated())) {
                                    while (!stack.isEmpty()) {
                                        stack = FabricUtils.insertItem(origin, stack, false);
                                        ItemStack originStack = item.getItem().copy();
                                        originStack.shrink((int) amount - stack.getCount());
                                        if (originStack.isEmpty()) {
                                            item.setItem(ItemStack.EMPTY);
                                            item.remove(Entity.RemovalReason.KILLED);
                                            return;
                                        } else {
                                            item.setItem(originStack);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    private void findSlot(Storage<ItemVariant> itemHandler) {
        for (StorageView<ItemVariant> slot : itemHandler) {
            if (slot.getUnderlyingView() == extractSlot.getUnderlyingView())
                continue;
            if (!slot.isResourceBlank() && filter(this.getFilter(), this.isWhitelist(), new ResourceAmount<>(slot.getResource(), slot.getAmount()), itemHandler, false)) {
                this.extractSlot = slot;
                return;
            }
        }
        this.extractSlot = null;
    }

    private boolean filter(RegulatorFilter<ResourceAmount<ItemVariant>, Storage<ItemVariant>> filter, boolean whitelist, ResourceAmount<ItemVariant> stack, Storage<ItemVariant> handler, boolean isRegulated) {
        long accepts = filter.matches(stack, handler, isRegulated);
        if (whitelist && filter.isEmpty()) {
            return false;
        }
        return filter.isEmpty() != (whitelist == (accepts > 0));
    }

    @Override
    public void updateClient() {
        super.updateClient();
    }

    @Override
    public void handleRenderSync(Direction origin, CompoundTag compoundNBT) {

    }

    @Environment(EnvType.CLIENT)
    @Override
    public void renderTransfer(Vector3f pos, Direction direction, int step, PoseStack stack, int combinedOverlayIn, MultiBufferSource buffer, float frame) {
        super.renderTransfer(pos, direction, step, stack, combinedOverlayIn, buffer, frame);

    }

    public static class Factory extends TransporterTypeFactory {

        public Factory() {
            super("world");
        }

        @Override
        public TransporterType create(IBlockContainer container, Direction face, TransporterAction action) {
            return new TransporterWorldType(container, this, face, action);
        }

        @Override
        @Nonnull
        public ResourceLocation getModel(Direction upgradeSide, TransporterAction action) {
            return new ResourceLocation(Reference.MOD_ID, "block/transporters/world_transporter_" + action.name().toLowerCase() + "_" + upgradeSide.getSerializedName().toLowerCase());
        }

        @Override
        public Set<ResourceLocation> getTextures() {
            return Sets.newHashSet(new ResourceLocation("industrialforegoing:blocks/transporters/world"), new ResourceLocation("industrialforegoing:blocks/base/bottom"));
        }

        @Override
        public boolean canBeAttachedAgainst(Level world, BlockPos pos, Direction face) {
            return TransferUtil.getItemStorage(world, pos, face) != null;
        }

        @Nonnull
        @Override
        public ResourceLocation getItemModel() {
            return new ResourceLocation(Reference.MOD_ID, "block/transporters/world_transporter_" + TransporterAction.EXTRACT.name().toLowerCase() + "_" + Direction.NORTH.getSerializedName().toLowerCase());
        }

        @Override
        public void registerRecipe(Consumer<FinishedRecipe> consumer) {
            TitaniumShapedRecipeBuilder.shapedRecipe(getUpgradeItem(), 2)
                    .pattern("IPI").pattern("GMG").pattern("ICI")
                    .define('I', Tags.Items.DUSTS_REDSTONE)
                    .define('P', Items.ENDER_PEARL)
                    .define('G', Items.HOPPER)
                    .define('M', IndustrialTags.Items.MACHINE_FRAME_PITY)
                    .define('C', Items.DROPPER)
                    .save(consumer);
        }
    }
}
