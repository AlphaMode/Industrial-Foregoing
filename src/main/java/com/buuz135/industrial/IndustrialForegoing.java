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
import com.buuz135.industrial.proxy.client.ClientProxy;
import com.buuz135.industrial.proxy.client.render.TransporterTESR;
import com.buuz135.industrial.proxy.network.*;
import com.buuz135.industrial.recipe.LaserDrillRarity;
import com.buuz135.industrial.recipe.provider.IndustrialRecipeProvider;
import com.buuz135.industrial.recipe.provider.IndustrialSerializableProvider;
import com.buuz135.industrial.recipe.provider.IndustrialTagsProvider;
import com.buuz135.industrial.registry.IFRegistries;
import com.buuz135.industrial.utils.IFFakePlayer;
import com.buuz135.industrial.utils.Reference;
import com.buuz135.industrial.utils.data.IndustrialBlockstateProvider;
import com.buuz135.industrial.utils.data.IndustrialModelProvider;
import com.hrznstudio.titanium.datagenerator.loot.TitaniumLootTableProvider;
import com.hrznstudio.titanium.datagenerator.model.BlockItemModelGeneratorProvider;
import com.hrznstudio.titanium.event.handler.EventManager;
import com.hrznstudio.titanium.module.ModuleController;
import com.hrznstudio.titanium.network.NetworkHandler;
import com.hrznstudio.titanium.network.locator.PlayerInventoryFinder;
import com.hrznstudio.titanium.reward.Reward;
import com.hrznstudio.titanium.reward.RewardGiver;
import com.hrznstudio.titanium.reward.RewardManager;
import dev.cafeteria.fakeplayerapi.server.FakeServerPlayer;
import io.github.fabricators_of_create.porting_lib.fake_players.FakePlayer;
import io.github.tropheusj.milk.Milk;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.dimension.DimensionType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.NonNullLazy;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public void onInitialize() {
        proxy = new CommonProxy();
        EventManager.mod(FMLCommonSetupEvent.class).process(fmlCommonSetupEvent -> proxy.run()).subscribe();
        ServerLifecycleEvents.SERVER_STARTING.register(server -> worldFakePlayer.clear());
        EventManager.mod(NewRegistryEvent.class).process(IFRegistries::create).subscribe();
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
        DistExecutor.unsafeRunWhenOn(EnvType.CLIENT, () -> this::initClient);
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
