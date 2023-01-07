package com.buuz135.industrial.utils;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TransferUtil2 {

    // ItemHandlerHelper.insertItem
    public static ItemStack insertItem(Storage<ItemVariant> storage, ItemStack stack, boolean simulate) {
        try (var transaction = TransferUtil.getTransaction()) {
            ItemStack newStack = stack.copy();
            long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            newStack.setCount(stack.getCount() - (int) inserted);
            if (!simulate)
                transaction.commit();
            return newStack;
        }
    }

    // ItemHandlerHelper.giveItemToPlayer
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        try (var transaction = TransferUtil.getTransaction()) {
            var storage = PlayerInventoryStorage.of(player);
            var variant = ItemVariant.of(stack);
            storage.offerOrDrop(variant, stack.getCount(), transaction);
        }
    }
}
