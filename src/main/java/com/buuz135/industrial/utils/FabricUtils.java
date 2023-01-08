package com.buuz135.industrial.utils;

import com.hrznstudio.titanium.util.TileUtil;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlotExposedStorage;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class FabricUtils {
    public static final ItemApiLookup<Storage<ItemVariant>, ContainerItemContext> ITEM =
            ItemApiLookup.get(new ResourceLocation("fabric:item_storage"), Storage.asClass(), ContainerItemContext.class);

    public static int getBurnTime(ItemStack stack, RecipeType<?> type) {
        if (stack.isEmpty())
            return 0;
        Integer time = FuelRegistry.INSTANCE.get(stack.getItem());
        return time != null ? time : 0;
    }

    public static <V> Optional<Holder<V>> getReverseTag(Registry<V> registry, @NotNull V value) {
        return registry.getHolder(registry.getResourceKey(value).get());
    }

    public static ItemStack insertItem(Storage<ItemVariant> storage, ItemStack stack, boolean simulate) {
        try (Transaction tx = TransferUtil.getTransaction()) {
            ItemStack newStack = stack.copy();
            long inserted = storage.insert(ItemVariant.of(stack), stack.getCount(), tx);
            newStack.setCount(stack.getCount() - (int) inserted);
            if (!simulate)
                tx.commit();
            return newStack;
        }
    }

    public static ItemStack insertSlot(SlotExposedStorage storage, int slot, ItemStack stack, boolean simulate) {
        try (Transaction tx = TransferUtil.getTransaction()) {
            ItemStack newStack = stack.copy();
            long inserted = storage.insertSlot(slot, ItemVariant.of(stack), stack.getCount(), tx);
            newStack.setCount(stack.getCount() - (int) inserted);
            if (!simulate)
                tx.commit();
            return newStack;
        }
    }

    public static ItemStack extractSlot(SlotExposedStorage storage, int slot, long amount, boolean simulate) {
        try (Transaction tx = TransferUtil.getTransaction()) {
            ItemStack newStack = storage.getStackInSlot(slot).copy();
            long extracted = storage.extractSlot(slot, ItemVariant.of(newStack), amount, tx);
            newStack.setCount((int) extracted);
            if (!simulate)
                tx.commit();
            return newStack;
        }
    }

    public static ItemStack extractItemView(StorageView<ItemVariant> view, long amount, boolean simulate) {
        try (Transaction tx =TransferUtil.getTransaction()) {
            long extracted = view.extract(view.getResource(), amount, tx);
            ItemStack stack = view.getResource().toStack((int) Math.min(view.getResource().getItem().getMaxStackSize(), extracted));
            if (!simulate)
                tx.commit();
            return stack;
        }
    }

    public static <T> Optional<Storage<T>> getStorage(BlockApiLookup<Storage<T>, Direction> lookup, Level level, BlockPos pos, @Nullable Direction side) {
        List<Storage<T>> storages = new ArrayList<>();
        Optional<BlockEntity> blockEntity = TileUtil.getTileEntity(level, pos);
        if (side != null) {
            return Optional.ofNullable(lookup.find(level, pos, null, blockEntity.orElse(null), side));
        }
        for (Direction direction : Direction.values()) {
            Storage<T> tStorage = lookup.find(level, pos, null, blockEntity.orElse(null), direction);

            if (tStorage != null) {
                if (storages.size() == 0) {
                    storages.add(tStorage);
                    continue;
                }

                for (Storage<T> storage : storages) {
                    if (!Objects.equals(tStorage, storage)) {
                        storages.add(tStorage);
                        break;
                    }
                }
            }
        }

        if (storages.isEmpty()) return Optional.empty();
        if (storages.size() == 1) return Optional.of(storages.get(0));
        return Optional.of(new CombinedStorage<>(storages));
    }


    public static ItemStack getFilledBucket(FluidStack stack) {
        ContainerItemContext context = ContainerItemContext.withInitial(Items.BUCKET.getDefaultInstance());
        try (Transaction tx = TransferUtil.getTransaction()) {
            context.find(FluidStorage.ITEM).insert(stack.getType(), FluidConstants.BUCKET, tx);
            tx.commit();
        }
        return context.getItemVariant().toStack();
    }
}
