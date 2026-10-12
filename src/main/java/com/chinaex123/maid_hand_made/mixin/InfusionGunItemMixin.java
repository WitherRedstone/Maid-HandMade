package com.chinaex123.maid_hand_made.mixin;

import com.alben.createhandmade.item.InfusionGunContents;
import com.alben.createhandmade.item.InfusionGunItem;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import java.util.List;

@Mixin(InfusionGunItem.class)
public interface InfusionGunItemMixin {

    @Invoker("getFluidFromSource")
    static FluidStack invokeGetFluidFromSource(Level level, BlockPos pos) {
        throw new AssertionError();
    }

    @Invoker("findFillingRecipe")
    static RecipeHolder<? extends StandardProcessingRecipe<?>> invokeFindFillingRecipe(
            Level level, ItemStack target, FluidStack fluid) {
        throw new AssertionError();
    }

    @Invoker("requiredFluidOf")
    static SizedFluidIngredient invokeRequiredFluidOf(StandardProcessingRecipe<?> recipe) {
        throw new AssertionError();
    }

    @Invoker("finishInject")
    static TransportedResult invokeFinishInject(TransportedItemStack item, ItemStack stack,
                                                List<ItemStack> results) {
        throw new AssertionError();
    }

    @Invoker("broadcastFluidParticles")
    static void invokeBroadcastFluidParticles(Level level, BlockPos pos, FluidStack fluid) {
        throw new AssertionError();
    }

    @Invoker("canInjectItemsAt")
    static boolean invokeCanInjectItemsAt(Level level, BlockPos pos, InfusionGunContents contents) {
        throw new AssertionError();
    }
}