package com.chinaex123.maid_hand_made.mixin;

import com.alben.createhandmade.item.BellowsItem;
import com.alben.createhandmade.recipe.FanType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(BellowsItem.class)
public interface BellowsItemMixin {

    @Invoker("findTargetItems")
    static List<ItemStack> invokeFindTargetItems(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("toFanType")
    static FanType invokeToFanType(FanProcessingType createType) {
        throw new AssertionError();
    }

    @Invoker("applyToTarget")
    static boolean invokeApplyToTarget(Level level, BlockPos pos, FanProcessingType type) {
        throw new AssertionError();
    }

    @Invoker("findL3BellowsRecipe")
    static RecipeHolder<?> invokeFindL3BellowsRecipe(Level level, ItemStack item, FanType fanType) {
        throw new AssertionError();
    }
}