package com.chinaex123.maid_hand_made.util;

import com.alben.createhandmade.ModDataComponents;
import com.alben.createhandmade.item.HandSawItem;
import com.alben.createhandmade.recipe.HandMadeCuttingRecipe;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public final class HandSawHelper {

    private HandSawHelper() {
    }

    public static boolean hasCuttingRecipe(Level level, ItemStack input) {
        return !HandSawItem.getCuttingRecipes(level, input).isEmpty();
    }

    public static boolean executeCut(Level level, EntityMaid maid, ItemStack saw) {
        ItemStack off = maid.getOffhandItem();
        if (off.isEmpty()) return false;

        List<RecipeHolder<? extends Recipe<?>>> recipes = HandSawItem.getCuttingRecipes(level, off);
        if (recipes.isEmpty()) return false;

        int index = saw.getOrDefault(ModDataComponents.HAND_SAW_RECIPE_INDEX.get(), 0);
        if (index >= recipes.size()) index = 0;

        Recipe<?> recipe = recipes.get(index).value();

        List<ItemStack> results = new ArrayList<>();
        if (recipe instanceof HandMadeCuttingRecipe exclusive) {
            results = exclusive.rollResults(level.random);
        } else if (recipe instanceof CuttingRecipe cr) {
            results = cr.rollResults(level.random);
        } else if (recipe instanceof SingleItemRecipe sc) {
            results.add(sc.getResultItem(level.registryAccess()).copy());
        } else {
            results.add(recipe.getResultItem(level.registryAccess()).copy());
        }

        ItemStack craftingRemainder = off.getCraftingRemainingItem();

        off.shrink(1);
        if (off.isEmpty()) {
            maid.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            maid.setItemInHand(InteractionHand.OFF_HAND, off);
        }

        if (!craftingRemainder.isEmpty()) {
            results.add(craftingRemainder);
        }

        for (ItemStack result : results) {
            if (result.isEmpty()) continue;
            MaidInventoryHelper.addToBackpack(maid, result.copy());
        }

        saw.hurtAndBreak(1, maid, EquipmentSlot.MAINHAND);

        level.playSound(null, maid.blockPosition(), SoundEvents.WOOD_BREAK,
                SoundSource.PLAYERS, 0.7f, 1.2f);

        return true;
    }
}