package com.chinaex123.maid_hand_made.mixin;

import com.alben.createhandmade.item.BellowsItem;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;
import java.util.List;

@Mixin(BellowsItem.class)
public interface BellowsItemMixin {

    @Invoker("findTargetItems")
    static List<ItemStack> invokeFindTargetItems(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("applyToTarget")
    static boolean invokeApplyToTarget(Level level, BlockPos pos, List<ResourceLocation> candidateIds, @Nullable ResourceLocation preferred, FanProcessingType[] pickedOut) {
        throw new AssertionError();
    }

    @Invoker("findL3BellowsRecipe")
    static RecipeHolder<?> invokeFindL3BellowsRecipe(Level level, ItemStack item, ResourceLocation fanTypeId) {
        throw new AssertionError();
    }
}