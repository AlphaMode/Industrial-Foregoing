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

package com.buuz135.industrial.worlddata;

import io.github.fabricators_of_create.porting_lib.extensions.INBTSerializable;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedIterator;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BackpackDataManager extends SavedData {
    public static HashMap<String, BackpackItemHandler> CLIENT_SIDE_BACKPACKS = new HashMap<>();

    public static final String NAME = "IFBackpack";
    public static final int SLOT_AMOUNT = 4 * 8;
    public HashMap<String, BackpackItemHandler> itemHandlers;

    public BackpackDataManager(String name) {
        itemHandlers = new HashMap<>();
    }

    public BackpackDataManager() {
        itemHandlers = new HashMap<>();
    }

    public void createBackPack(UUID uuid) {
        this.itemHandlers.put(uuid.toString(), new BackpackItemHandler(this));
        setDirty();
    }

    public BackpackItemHandler getBackpack(String id) {
        return itemHandlers.get(id);
    }

    public static BackpackDataManager load(CompoundTag nbt) {
        BackpackDataManager manager = new BackpackDataManager();
        manager.itemHandlers = new HashMap<>();
        CompoundTag backpacks = nbt.getCompound("Backpacks");
        for (String s : backpacks.getAllKeys()) {
            BackpackItemHandler hander = new BackpackItemHandler(manager);
            hander.deserializeNBT(backpacks.getCompound(s));
            manager.itemHandlers.put(s, hander);
        }

        return manager;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        CompoundTag nbt = new CompoundTag();
        itemHandlers.forEach((s, iItemHandler) -> nbt.put(s, iItemHandler.serializeNBT()));
        compound.put("Backpacks", nbt);
        return compound;
    }

    @Nullable
    public static BackpackDataManager getData(LevelAccessor world) {
        if (world instanceof ServerLevel) {
            ServerLevel serverWorld = ((ServerLevel) world).getServer().getLevel(Level.OVERWORLD);
            BackpackDataManager data = serverWorld.getDataStorage().computeIfAbsent(BackpackDataManager::load, BackpackDataManager::new, NAME);
            return data;
        }
        return null;
    }

    public static class BackpackItemHandler extends SnapshotParticipant<List<SlotDefinition>> implements SlotExposedStorage, INBTSerializable<CompoundTag> {

        private List<SlotDefinition> definitionList;
        private int maxAmount;
        private BackpackDataManager dataManager;

        public BackpackItemHandler(BackpackDataManager manager) {
            definitionList = new ArrayList<>();
            for (int i = 0; i < SLOT_AMOUNT; i++) {
                definitionList.add(new SlotDefinition(this));
            }
            this.maxAmount = 2048;
            this.dataManager = manager;
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag slots = new CompoundTag();
            for (int i = 0; i < definitionList.size(); i++) {
                slots.put(i + "", definitionList.get(i).serializeNBT());
            }
            CompoundTag output = new CompoundTag();
            output.put("Slots", slots);
            output.putInt("MaxAmount", this.maxAmount);
            return output;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            CompoundTag slots = nbt.getCompound("Slots");
            for (String s : slots.getAllKeys()) {
                definitionList.get(Integer.parseInt(s)).deserializeNBT(slots.getCompound(s));
            }
            this.maxAmount = nbt.getInt("MaxAmount");
        }

        @Override
        public int getSlots() {
            return definitionList.size();
        }

        @Nonnull
        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemHandlerHelper.copyStackWithSize(definitionList.get(slot).getStack(), (int) definitionList.get(slot).getAmount());
        }

        @Override
        public long insert(ItemVariant variant, long amount, TransactionContext transaction) {
            for (int slot = 0; slot < definitionList.size(); slot++) {
                if (isItemValid(slot, variant, amount)) {
                    SlotDefinition definition = definitionList.get(slot);
                    long inserted = Math.min(maxAmount - definition.getAmount(), amount);
                    if (definition.isVoidItems()) inserted = amount; //Void
                    updateSnapshots(transaction);
                    definition.setStack(variant.toStack((int) amount));
                    definition.setAmount(Math.min(definition.getAmount() + inserted, maxAmount));
                    if (inserted == amount) return 0;
                    return amount - inserted;
                }
            }
            return 0;
        }

        @Nonnull
        @Override
        public long insertSlot(int slot, @Nonnull ItemVariant variant, long amount, TransactionContext tx) {
            if (isItemValid(slot, variant, amount)) {
                SlotDefinition definition = definitionList.get(slot);
                long inserted = Math.min(maxAmount - definition.getAmount(), amount);
                if (definition.isVoidItems()) inserted = amount; //Void
                updateSnapshots(tx);
                definition.setStack(variant.toStack((int) amount));
                definition.setAmount(Math.min(definition.getAmount() + inserted, maxAmount));
                if (inserted == amount) return 0;
                return amount - inserted;
            }
            return 0;
        }

        @Nonnull
        @Override
        public long extractSlot(int slot, ItemVariant variant, long amount, TransactionContext tx) {
            if (amount == 0) return 0;
            SlotDefinition definition = definitionList.get(slot);
            if (definition.getStack().isEmpty()) return 0;
            if (definition.getAmount() <= amount) {
                long newAmount = definition.getAmount();
                updateSnapshots(tx);
                definition.setAmount(0);
                return newAmount;
            } else {
                updateSnapshots(tx);
                definition.setAmount(definition.getAmount() - amount);
                return amount;
            }
        }

        @Override
        public long extract(ItemVariant resource, long amount, TransactionContext tx) {
            for (int slot = 0; slot < definitionList.size(); slot++) {
                if (amount == 0) return 0;
                SlotDefinition definition = definitionList.get(slot);
                if (definition.getStack().isEmpty()) return 0;
                if (definition.getAmount() <= amount) {
                    long newAmount = definition.getAmount();
                    updateSnapshots(tx);
                    definition.setAmount(0);
                    return newAmount;
                } else {
                    updateSnapshots(tx);
                    definition.setAmount(definition.getAmount() - amount);
                    return amount;
                }
            }
            return 0;
        }

        @Override
        protected void onFinalCommit() {
            markDirty();
        }

        @Override
        public int getSlotLimit(int slot) {
            return maxAmount;
        }

        @Override
        public boolean isItemValid(int slot, @Nonnull ItemVariant variant, long amount) {
            SlotDefinition def = definitionList.get(slot);
            return def.getStack().isEmpty() || (def.getStack().sameItem(variant.toStack((int) amount)) && ItemStack.tagMatches(def.getStack(), variant.toStack((int) amount)));
        }

        public void setMaxAmount(int maxAmount) {
            this.maxAmount = maxAmount;
            markDirty();
        }

        public void markDirty() {
            if (dataManager != null) dataManager.setDirty();
        }

        public SlotDefinition getSlotDefinition(int slot) {
            return definitionList.get(slot);
        }

        @Override
        protected List<SlotDefinition> createSnapshot() {
            return definitionList;
        }

        @Override
        protected void readSnapshot(List<SlotDefinition> snapshot) {
            definitionList = snapshot;
        }

        @Override
        public Iterator<StorageView<ItemVariant>> iterator() {
            return new SlotExposedIterator(this);
        }
    }

    public static class SlotDefinition implements INBTSerializable<CompoundTag> {

        private ItemStack stack;
        private long amount;
        private boolean voidItems;
        private boolean refillItems;
        private BackpackItemHandler parent;

        public SlotDefinition(BackpackItemHandler parent) {
            this.stack = ItemStack.EMPTY;
            this.amount = 0;
            this.voidItems = true;
            this.refillItems = false;
            this.parent = parent;
        }

        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            this.stack = stack;
            parent.markDirty();
        }

        public long getAmount() {
            return amount;
        }

        public void setAmount(long amount) {
            this.amount = amount;
            parent.markDirty();
        }

        @Override
        public CompoundTag serializeNBT() {
            CompoundTag compoundNBT = new CompoundTag();
            compoundNBT.put("Stack", NBTSerializer.serializeNBT(stack));
            compoundNBT.putLong("Amount", amount);
            compoundNBT.putBoolean("Void", voidItems);
            compoundNBT.putBoolean("Refill", refillItems);
            return compoundNBT;
        }

        @Override
        public void deserializeNBT(CompoundTag nbt) {
            this.stack = ItemStack.of(nbt.getCompound("Stack"));
            this.amount = nbt.getLong("Amount");
            this.voidItems = nbt.getBoolean("Void");
            this.refillItems = nbt.getBoolean("Refill");
        }

        public boolean isVoidItems() {
            return voidItems;
        }

        public void setVoidItems(boolean voidItems) {
            this.voidItems = voidItems;
            parent.markDirty();
        }

        public boolean isRefillItems() {
            return refillItems;
        }

        public void setRefillItems(boolean refillItems) {
            this.refillItems = refillItems;
            parent.markDirty();
        }
    }
}
