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

package com.buuz135.industrial.proxy.client.event;

import com.buuz135.industrial.item.infinity.InfinityTier;
import com.buuz135.industrial.item.infinity.item.ItemInfinityDrill;
import com.buuz135.industrial.module.ModuleTool;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import io.github.fabricators_of_create.porting_lib.event.client.RenderPlayerEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

public class IFClientEvents {

    public static boolean blockOverlayEvent(WorldRenderContext context, @Nullable HitResult hit) {
        if (hit != null && hit.getType() == HitResult.Type.BLOCK && Minecraft.getInstance().player.getMainHandItem().getItem().equals(ModuleTool.INFINITY_DRILL)) {
            BlockHitResult blockRayTraceResult = (BlockHitResult) hit;
            ItemStack hand = Minecraft.getInstance().player.getMainHandItem();
            InfinityTier tier = ((ItemInfinityDrill) ModuleTool.INFINITY_DRILL.get()).getSelectedTier(hand);
            Level world = Minecraft.getInstance().player.level;
            Pair<BlockPos, BlockPos> area = ((ItemInfinityDrill) ModuleTool.INFINITY_DRILL.get()).getArea(blockRayTraceResult.getBlockPos(), blockRayTraceResult.getDirection(), tier, false);
            PoseStack stack = new PoseStack();
            stack.pushPose();
            Camera info = context.camera();
            stack.mulPose(Vector3f.XP.rotationDegrees(info.getXRot()));
            stack.mulPose(Vector3f.YP.rotationDegrees(info.getYRot() + 180));
            double d0 = info.getPosition().x();
            double d1 = info.getPosition().y();
            double d2 = info.getPosition().z();
            VertexConsumer builder = Minecraft.getInstance().renderBuffers().outlineBufferSource().getBuffer(RenderType.lines());
            BlockPos.betweenClosed(area.getLeft(), area.getRight()).forEach(blockPos -> {
                VoxelShape shape = world.getBlockState(blockPos).getShape(world, blockPos);
                if (shape != null && !shape.isEmpty() && !world.isEmptyBlock(blockPos) && world.getBlockState(blockPos).getDestroySpeed(world, blockPos) >= 0 && !(world.getBlockState(blockPos).getBlock() instanceof LiquidBlock)) {
                    LevelRenderer.renderLineBox(stack, builder, shape.bounds().move(blockPos.getX() - d0, blockPos.getY() - d1, blockPos.getZ() - d2), 0, 0, 0, 0.35F);
                }
            });
            stack.popPose();
            return false;
        }
        return true;
    }

    public static boolean onRenderPre(Player player, PlayerRenderer renderer, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // todo: test if rewards are rendering.
        //event.getRenderer().addLayer(new ContributorsCatEarsRender(event.getRenderer()));

        if (player.getUUID().equals(Minecraft.getInstance().player.getUUID()) && Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON)
            return false;
        if (player.getItemInHand(InteractionHand.MAIN_HAND).getItem().equals(ModuleTool.INFINITY_DRILL))
            player.startUsingItem(InteractionHand.MAIN_HAND);
        else if (player.getItemInHand(InteractionHand.OFF_HAND).getItem().equals(ModuleTool.INFINITY_DRILL))
            player.startUsingItem(InteractionHand.OFF_HAND);
        return false;
    }

    public static void init() {
        WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(IFClientEvents::blockOverlayEvent);
        RenderPlayerEvents.PRE.register(IFClientEvents::onRenderPre);
    }
}
