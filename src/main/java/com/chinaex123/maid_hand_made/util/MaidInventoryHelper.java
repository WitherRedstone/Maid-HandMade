package com.chinaex123.maid_hand_made.util;

import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.CombinedInvWrapper;

import java.util.function.Predicate;

public final class MaidInventoryHelper {

    private MaidInventoryHelper() {
    }

    public static IItemHandler getBackpack(EntityMaid maid) {
        return maid.getAvailableBackpackInv();
    }

    public static IItemHandler getWideBackpack(EntityMaid maid) {
        return new CombinedInvWrapper(
                maid.getHandsInvWrapper(),
                maid.getMaidInv(),
                maid.getMaidBauble()
        );
    }

    public static void addToBackpack(EntityMaid maid, ItemStack stack) {
        if (stack.isEmpty()) return;
        IItemHandler inv = getBackpack(maid);
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(inv, stack, false);
        if (!remainder.isEmpty()) {
            maid.spawnAtLocation(remainder);
        }
    }

    public static void addToWideBackpack(EntityMaid maid, ItemStack stack) {
        if (stack.isEmpty()) return;
        IItemHandler inv = getWideBackpack(maid);
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(inv, stack, false);
        if (!remainder.isEmpty()) {
            maid.spawnAtLocation(remainder);
        }
    }

    public static int findSlot(EntityMaid maid, Predicate<ItemStack> predicate) {
        IItemHandler inv = getBackpack(maid);
        for (int i = 0; i < inv.getSlots(); i++) {
            if (predicate.test(inv.getStackInSlot(i))) {
                return i;
            }
        }
        return -1;
    }
}