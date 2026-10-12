package com.chinaex123.maid_hand_made.mixin;

import com.alben.createhandmade.item.CrusherMortarItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrusherMortarItem.class)
public interface CrusherMortarItemMixin {

    @Invoker("findRecipe")
    static RecipeHolder<?> invokeFindRecipe(Level level, ItemStack input) {
        throw new AssertionError();
    }
}