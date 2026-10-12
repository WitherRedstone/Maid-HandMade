package com.chinaex123.maid_hand_made.mixin;

import com.alben.createhandmade.item.MortarItem;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MortarItem.class)
public interface MortarItemMixin {

    @Invoker("findMillingRecipe")
    static RecipeHolder<?> invokeFindMillingRecipe(Level level, SingleRecipeInput recipeInput) {
        throw new AssertionError();
    }
}