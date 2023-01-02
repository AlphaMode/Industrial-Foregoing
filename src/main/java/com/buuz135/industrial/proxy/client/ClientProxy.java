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

package com.buuz135.industrial.proxy.client;

import com.buuz135.industrial.IndustrialForegoing;
import com.buuz135.industrial.block.generator.tile.MycelialReactorTile;
import com.buuz135.industrial.block.tile.IndustrialAreaWorkingTile;
import com.buuz135.industrial.block.transportstorage.tile.BHTile;
import com.buuz135.industrial.block.transportstorage.tile.BlackHoleTankTile;
import com.buuz135.industrial.block.transportstorage.tile.ConveyorTile;
import com.buuz135.industrial.block.transportstorage.tile.TransporterTile;
import com.buuz135.industrial.entity.InfinityLauncherProjectileEntity;
import com.buuz135.industrial.entity.InfinityNukeEntity;
import com.buuz135.industrial.entity.InfinityTridentEntity;
import com.buuz135.industrial.entity.client.*;
import com.buuz135.industrial.item.MobImprisonmentToolItem;
import com.buuz135.industrial.item.infinity.InfinityTier;
import com.buuz135.industrial.item.infinity.ItemInfinity;
import com.buuz135.industrial.item.infinity.OneThreeFiveHandler;
import com.buuz135.industrial.module.*;
import com.buuz135.industrial.proxy.CommonProxy;
import com.buuz135.industrial.proxy.client.event.IFClientEvents;
import com.buuz135.industrial.proxy.client.render.*;
import com.buuz135.industrial.proxy.network.BackpackOpenMessage;
import com.buuz135.industrial.utils.FluidUtils;
import com.buuz135.industrial.utils.Reference;
import io.github.fabricators_of_create.porting_lib.event.client.ModelsBakedCallback;
import io.github.fabricators_of_create.porting_lib.event.client.TextureStitchCallback;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.ModelLoadingRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityFeatureRendererRegistrationCallback;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Calendar;
import java.util.Optional;

public class ClientProxy extends CommonProxy implements ClientModInitializer {

    public static ResourceLocation GUI = new ResourceLocation(Reference.MOD_ID, "textures/gui/machines.png");
    public static BakedModel ears_baked;

    public KeyMapping OPEN_BACKPACK;

    @Override
    public void onInitializeClient() {
        TextureStitchCallback.PRE.register((atlas, spriteAdder) -> {
            if (atlas.location().equals(InventoryMenu.BLOCK_ATLAS)) {
                spriteAdder.accept(TransporterTESR.TEXTURE);
            }
        });
        ModelsBakedCallback.EVENT.register((manager, models, loader) -> {
            ClientProxy.ears_baked = models.get(new ResourceLocation(Reference.MOD_ID, "block/catears"));
        });
        ModelLoadingRegistry.INSTANCE.registerModelProvider((manager, out) -> {
            out.accept(new ResourceLocation(Reference.MOD_ID, "block/catears"));
        });
        run();

        ClientTickEvents.START_CLIENT_TICK.register(client -> OneThreeFiveHandler.onClientTick());
        ClientTickEvents.END_CLIENT_TICK.register(client -> OneThreeFiveHandler.onClientTick());
    }

    @Override
    public void run() {
        OPEN_BACKPACK = new KeyMapping("key.industrialforegoing.backpack.desc", -1, "key.industrialforegoing.category");
        KeyBindingHelper.registerKeyBinding(OPEN_BACKPACK);
        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            if (OPEN_BACKPACK.consumeClick()) {
                IndustrialForegoing.NETWORK.get().sendToServer(new BackpackOpenMessage(Screen.hasControlDown()));
            }
        });

        IFClientEvents.init();

        //((IReloadableResourceManager) Minecraft.getInstance().getResourceManager()).addReloadListener(resourceManager -> FluidUtils.colorCache.clear());

        BlockRenderLayerMap.INSTANCE.putBlock(ModuleTransportStorage.CONVEYOR.getLeft().get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModuleTransportStorage.BLACK_HOLE_TANK_COMMON.getLeft().get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModuleTransportStorage.BLACK_HOLE_TANK_PITY.getLeft().get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModuleTransportStorage.BLACK_HOLE_TANK_SIMPLE.getLeft().get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModuleTransportStorage.BLACK_HOLE_TANK_ADVANCED.getLeft().get(), RenderType.cutout());
        BlockRenderLayerMap.INSTANCE.putBlock(ModuleTransportStorage.BLACK_HOLE_TANK_SUPREME.getLeft().get(), RenderType.cutout());

        Minecraft.getInstance().getBlockColors().register((state, worldIn, pos, tintIndex) -> {
            if (tintIndex == 0 && worldIn != null && pos != null) {
                BlockEntity entity = worldIn.getBlockEntity(pos);
                if (entity instanceof ConveyorTile) {
                    return ((ConveyorTile) entity).getColor();
                }
            }
            return 0xFFFFFFF;
        }, ModuleTransportStorage.CONVEYOR.getLeft().get());
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex == 1 || tintIndex == 2 || tintIndex == 3) {
                SpawnEggItem info = null;
                if (stack.hasTag() && stack.getTag().contains("entity")) {
                    ResourceLocation id = new ResourceLocation(stack.getTag().getString("entity"));
                    info = SpawnEggItem.byId(Registry.ENTITY_TYPE.get(id));
                }
                return info == null ? 0x636363 : tintIndex == 3 ? ((MobImprisonmentToolItem) ModuleTool.MOB_IMPRISONMENT_TOOL.get()).isBlacklisted(info.getType(new CompoundTag())) ? 0xDB201A : 0x636363 : info.getColor(tintIndex - 1);
            }
            return 0xFFFFFF;
        }, ModuleTool.MOB_IMPRISONMENT_TOOL.get());
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            if (tintIndex == 0) {
                return InfinityTier.getTierBraquet(ItemInfinity.getPowerFromStack(stack)).getLeft().getTextureColor();
            }
            return 0xFFFFFF;
        }, ModuleTool.INFINITY_BACKPACK.get(), ModuleTool.INFINITY_LAUNCHER.get(), ModuleTool.INFINITY_NUKE.get(), ModuleTool.INFINITY_TRIDENT.get(), ModuleTool.INFINITY_HAMMER.get(), ModuleTool.INFINITY_SAW.get(), ModuleTool.INFINITY_DRILL.get());
        Minecraft.getInstance().getBlockColors().register((state, worldIn, pos, tintIndex) -> {
            if (tintIndex == 0 && worldIn != null && pos != null && worldIn.getBlockEntity(pos) instanceof BlackHoleTankTile) {
                BlackHoleTankTile tank = (BlackHoleTankTile) worldIn.getBlockEntity(pos);
                if (tank != null && tank.getTank().getFluidAmount() > 0) {
                    int color = FluidUtils.getFluidColor(tank.getTank().getFluid());
                    if (color != -1) return color;
                }
            }
            return 0xFFFFFF;
        }, ModuleTransportStorage.BLACK_HOLE_TANK_COMMON.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_TANK_PITY.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_TANK_SIMPLE.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_TANK_ADVANCED.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_TANK_SUPREME.getLeft().get());
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            Optional<FluidStack> fluid = TransferUtil.getFluidContained(stack);
            if (tintIndex == 0 && fluid.isPresent()) {
                if (fluid.get().getAmount() > 0) {
                    int color = FluidUtils.getFluidColor(fluid.get());
                    if (color != -1) return color;
                }
            }
            return 0xFFFFFF;
        }, ModuleTransportStorage.BLACK_HOLE_TANK_COMMON.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_TANK_PITY.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_TANK_SIMPLE.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_TANK_ADVANCED.getLeft().get(), ModuleTransportStorage.BLACK_HOLE_TANK_SUPREME.getLeft().get());
        ColorProviderRegistry.ITEM.register((stack, tintIndex) -> {
            Optional<FluidStack> fluid = TransferUtil.getFluidContained(stack);
            if (tintIndex == 1 && fluid.isPresent()) {
                if (fluid.get().getAmount() > 0) {
                    int color = FluidUtils.getFluidColor(fluid.get());
                    if (color != -1) return color;
                }
            }
            return 0xFFFFFF;
        }, ModuleCore.RAW_ORE_MEAT.getBucketFluid(), ModuleCore.FERMENTED_ORE_MEAT.getBucketFluid());

        ItemTooltipCallback.EVENT.register((stack, context, lines) -> {
            if (Registry.ITEM.getKey(stack.getItem()).getNamespace().equals(Reference.MOD_ID)) {
                if (Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1 && Calendar.getInstance().get(Calendar.MONTH) == Calendar.APRIL) {
                    lines.add(Component.literal("Press Alt + F4 to cheat this item").withStyle(ChatFormatting.DARK_AQUA));
                }
            }
        });

        Minecraft instance = Minecraft.getInstance();
        EntityRenderDispatcher manager = instance.getEntityRenderDispatcher();

        ItemProperties.register(ModuleTool.INFINITY_LAUNCHER.get(), new ResourceLocation(Reference.MOD_ID, "cooldown"), (stack, world, entity, number) -> {
            if (entity instanceof Player) {
                return ((Player) entity).getCooldowns().isOnCooldown(stack.getItem()) ? 1 : 2;
            }
            return 2f;
        });

        layerDefinitions();
        onRegisterRenderers();

        LivingEntityFeatureRendererRegistrationCallback.EVENT.register(ClientProxy::addLayers);
    }

    public static void onRegisterRenderers() {
        registerAreaRender(ModuleCore.FLUID_EXTRACTOR);
        registerAreaRender(ModuleAgricultureHusbandry.PLANT_GATHERER);
        registerAreaRender(ModuleAgricultureHusbandry.PLANT_SOWER);
        registerAreaRender(ModuleAgricultureHusbandry.SEWER);
        registerAreaRender(ModuleAgricultureHusbandry.PLANT_FERTILIZER);
        registerAreaRender(ModuleAgricultureHusbandry.SLAUGHTER_FACTORY);
        registerAreaRender(ModuleAgricultureHusbandry.ANIMAL_RANCHER);
        registerAreaRender(ModuleAgricultureHusbandry.ANIMAL_FEEDER);
        registerAreaRender(ModuleAgricultureHusbandry.ANIMAL_BABY_SEPARATOR);
        registerAreaRender(ModuleAgricultureHusbandry.MOB_CRUSHER);
        registerAreaRender(ModuleAgricultureHusbandry.WITHER_BUILDER);
        registerAreaRender(ModuleMisc.STASIS_CHAMBER);
        registerAreaRender(ModuleResourceProduction.LASER_DRILL);


        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_UNIT_COMMON.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_UNIT_PITY.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_UNIT_SIMPLE.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_UNIT_ADVANCED.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_UNIT_SUPREME.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_TANK_COMMON.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_TANK_PITY.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_TANK_SIMPLE.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_TANK_ADVANCED.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_UNIT_COMMON.getRight().get(), BlackHoleUnitTESR::new);
        BlockEntityRenderers.register((BlockEntityType<? extends BHTile>) ModuleTransportStorage.BLACK_HOLE_TANK_SUPREME.getRight().get(), BlackHoleUnitTESR::new);

        BlockEntityRenderers.register((BlockEntityType<? extends MycelialReactorTile>) ModuleGenerator.MYCELIAL_REACTOR.getRight().get(), MycelialReactorTESR::new);

        EntityRendererRegistry.register((EntityType<? extends InfinityTridentEntity>) ModuleTool.TRIDENT_ENTITY_TYPE.get(), InfinityTridentRenderer::new);
        EntityRendererRegistry.register((EntityType<? extends InfinityNukeEntity>) ModuleTool.INFINITY_NUKE_ENTITY_TYPE.get(), InfinityNukeRenderer::new);
        EntityRendererRegistry.register((EntityType<? extends InfinityLauncherProjectileEntity>) ModuleTool.INFINITY_LAUNCHER_PROJECTILE_ENTITY_TYPE.get(), InfinityLauncherProjectileRenderer::new);

        BlockEntityRenderers.register(((BlockEntityType<? extends TransporterTile>) ModuleTransportStorage.TRANSPORTER.getRight().get()), TransporterTESR::new);

        BlockEntityRenderers.register(((BlockEntityType<? extends ConveyorTile>) ModuleTransportStorage.CONVEYOR.getRight().get()), FluidConveyorTESR::new);
    }

    private static void registerAreaRender(Pair<RegistryObject<Block>, RegistryObject<BlockEntityType<?>>> pair) {
        BlockEntityRenderers.register((BlockEntityType<? extends IndustrialAreaWorkingTile>) pair.getRight().get(), WorkingAreaTESR::new);
    }

    public static void layerDefinitions() {
        EntityModelLayerRegistry.registerModelLayer(InfinityTridentRenderer.TRIDENT_LAYER, InfinityTridentModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(InfinityNukeRenderer.NUKE_LAYER, InfinityNukeModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(InfinityNukeRenderer.NUKE_ARMED_LAYER, () -> InfinityNukeModelArmed.createBodyLayer(new CubeDeformation(0f)));
        EntityModelLayerRegistry.registerModelLayer(InfinityNukeRenderer.NUKE_ARMED_BIG_LAYER, () -> InfinityNukeModelArmed.createBodyLayer(new CubeDeformation(0.2f)));
        EntityModelLayerRegistry.registerModelLayer(InfinityLauncherProjectileRenderer.PROJECTILE_LAYER, InfinityLauncherProjectileModel::createBodyLayer);
    }

    public static void addLayers(EntityType<? extends LivingEntity> entityType, LivingEntityRenderer<?, ?> entityRenderer, LivingEntityFeatureRendererRegistrationCallback.RegistrationHelper registrationHelper, EntityRendererProvider.Context context) {
        if (entityRenderer instanceof PlayerRenderer renderer) {
            registrationHelper.register(new ContributorsCatEarsRender(renderer));
            registrationHelper.register(new InfinityLauncherProjectileArmorLayer(renderer));
        }
    }

}
