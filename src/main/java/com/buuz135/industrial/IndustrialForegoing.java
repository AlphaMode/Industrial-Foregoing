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
package com.buuz135.industrial;

import com.buuz135.industrial.module.*;
import com.buuz135.industrial.proxy.CommonProxy;
import com.buuz135.industrial.proxy.network.*;
import com.buuz135.industrial.recipe.LaserDrillRarity;
import com.buuz135.industrial.registry.IFRegistries;
import com.buuz135.industrial.utils.*;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.network.locator.PlayerInventoryFinder;
import com.hrznstudio.titanium.reward.Reward;
import com.hrznstudio.titanium.reward.RewardGiver;
import com.hrznstudio.titanium.reward.RewardManager;
import io.github.fabricators_of_create.porting_lib.fake_players.FakePlayer;
import io.github.tropheusj.milk.Milk;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import team.reborn.energy.api.EnergyStorage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.UUID;

public class IndustrialForegoing extends ModuleController implements ModInitializer {

    private static CommonProxy proxy;
    private static HashMap<DimensionType, IFFakePlayer> worldFakePlayer = new HashMap<>();
    public static NetworkHandler NETWORK = new NetworkHandler(Reference.MOD_ID);
    public static Logger LOGGER = LogManager.getLogger(Reference.MOD_ID);
    public static IndustrialForegoing INSTANCE;

    static {
        NETWORK.registerMessage(ConveyorButtonInteractMessage.class);
        NETWORK.registerMessage(ConveyorSplittingSyncEntityMessage.class);
        NETWORK.registerMessage(SpecialParticleMessage.class);
        NETWORK.registerMessage(BackpackSyncMessage.class);
        NETWORK.registerMessage(BackpackOpenMessage.class);
        NETWORK.registerMessage(BackpackOpenedMessage.class);
        NETWORK.registerMessage(TransporterSyncMessage.class);
        NETWORK.registerMessage(TransporterButtonInteractMessage.class);
        NETWORK.registerMessage(PlungerPlayerHitMessage.class);
    }

    public IndustrialForegoing() {
        super(Reference.MOD_ID);
    }

    @Override
    public void onInitialize() {
        proxy = new CommonProxy();
        proxy.run();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> worldFakePlayer.clear());
        IFRegistries.create();
        /*
        EventManager.forge(ItemTooltipEvent.class).process(itemTooltipEvent -> Registry.ITEM.tags().getReverseTag(itemTooltipEvent.getItemStack().getItem()).ifPresent(itemIReverseTag -> {
            itemIReverseTag.getTagKeys().forEach(itemTagKey -> itemTooltipEvent.getToolTip().add(Component.literal(itemTagKey.location().toString())));
        })).subscribe();*/
        RewardGiver giver = RewardManager.get().getGiver(UUID.fromString("d28b7061-fb92-4064-90fb-7e02b95a72a6"), "Buuz135");
        try {
            giver.addReward(new Reward(new ResourceLocation(Reference.MOD_ID, "cat_ears"), new URL("https://raw.githubusercontent.com/Buuz135/Industrial-Foregoing/master/contributors.json"), () -> dist -> {
            }, new String[]{"normal", "cat", "spooky", "snowy"}));
        } catch (MalformedURLException e) {
            LOGGER.catching(e);
        }
        LaserDrillRarity.init();
        PlayerInventoryFinder.init();
        Milk.enableMilkFluid();

        FluidStorage.ITEM.registerFallback((itemStack, context) -> {
            if (itemStack.getItem() instanceof FluidStorageItem storageItem)
                return storageItem.getFluidStorage(itemStack, context);
            return null;
        });
        FabricUtils.ITEM.registerFallback((itemStack, context) -> {
            if (itemStack.getItem() instanceof ItemStorageItem storageItem)
                return storageItem.getItemStorage(itemStack, context);
            return null;
        });
        EnergyStorage.ITEM.registerFallback((itemStack, context) -> {
            if (itemStack.getItem() instanceof EnergyStorageItem storageItem)
                return storageItem.getEnergyStorage(itemStack, context);
            return null;
        });
    }

    public static FakePlayer getFakePlayer(Level world) {
        if (worldFakePlayer.containsKey(world.dimensionType()))
            return worldFakePlayer.get(world.dimensionType());
        if (world instanceof ServerLevel) {
            IFFakePlayer fakePlayer = new IFFakePlayer((ServerLevel) world);
            worldFakePlayer.put(world.dimensionType(), fakePlayer);
            return fakePlayer;
        }
        return null;
    }

    public static FakePlayer getFakePlayer(Level world, BlockPos pos) {
        FakePlayer player = getFakePlayer(world);
        if (player != null) player.absMoveTo(pos.getX(), pos.getY(), pos.getZ(), 90, 90);
        return player;
    }

    @Override
    public void onPreInit() {
        super.onPreInit();
    }

    @Override
    protected void initModules() {
        INSTANCE = this;
        new ModuleCore().generateFeatures(getRegistries());
        new ModuleTool().generateFeatures(getRegistries());
        new ModuleTransportStorage().generateFeatures(getRegistries());
        new ModuleGenerator().generateFeatures(getRegistries());
        new ModuleAgricultureHusbandry().generateFeatures(getRegistries());
        new ModuleResourceProduction().generateFeatures(getRegistries());
        new ModuleMisc().generateFeatures(getRegistries());
    }
}
