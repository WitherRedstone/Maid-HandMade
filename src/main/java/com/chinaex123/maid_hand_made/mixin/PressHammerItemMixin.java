package com.chinaex123.maid_hand_made.mixin;

import com.alben.createhandmade.item.PressHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(PressHammerItem.class)
public interface PressHammerItemMixin {

    @Invoker("tryPressBasin")
    static boolean invokeTryPressBasin(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("tryPressTransported")
    static boolean invokeTryPressTransported(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("findTargetItems")
    static List<ItemStack> invokeFindTargetItems(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("broadcastParticles")
    static void invokeBroadcastParticles(Level level, BlockPos pos, List<ItemStack> stacks) {
        throw new AssertionError();
    }

    @Invoker("findPressingRecipe")
    static RecipeHolder<?> invokeFindPressingRecipe(Level level, ItemStack stack) {
        throw new AssertionError();
    }
}