package com.buuz135.industrial.utils;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.PlayerInventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TransferUtil2 {

    // ItemHandlerHelper.insertItem
    public static long insertItem(Storage<ItemVariant> storage, ItemStack stack, boolean simulate) {
        try (var transaction = TransferUtil.getTransaction()) {
            if (simulate) {
                return storage.simulateInsert(ItemVariant.of(stack), stack.getCount(), transaction);
            } else {
                return storage.insert(ItemVariant.of(stack), stack.getCount(), transaction);
            }
        }
    }

    // ItemHandlerHelper.giveItemToPlayer
    public static void giveItemToPlayer(Player player, ItemStack stack) {
        try (var transaction = TransferUtil.getTransaction()) {
            var storage = PlayerInventoryStorage.of(player);
            var variant = ItemVariant.of(stack);
            var inserted = storage.insert(variant, stack.getCount(), transaction);
            storage.drop(variant, stack.getCount() - inserted, transaction);
        }
    }
}
