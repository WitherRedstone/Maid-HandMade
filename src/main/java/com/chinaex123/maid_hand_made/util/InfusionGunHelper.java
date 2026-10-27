package com.chinaex123.maid_hand_made.util;

import com.alben.createhandmade.api.CreateHandMadeApi;
import com.alben.createhandmade.item.InfusionGunContents;
import com.alben.createhandmade.item.InfusionGunItem;
import com.simibubi.create.content.fluids.transfer.GenericItemFilling;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.logistics.depot.DepotBlockEntity;
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public final class InfusionGunHelper {

    public static final int FLUID_BLOCK_AMOUNT = 1000;

    private InfusionGunHelper() {
    }

    public static boolean tryExtractMax(Level level, BlockPos pos, LivingEntity user, ItemStack gun) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler == null) return false;

        InfusionGunContents contents = CreateHandMadeApi.infusionGunContents(gun);
        if (contents.remaining() <= 0) return false;

        FluidStack source = FluidStack.EMPTY;
        for (int i = 0; i < handler.getTanks(); i++) {
            FluidStack inTank = handler.getFluidInTank(i);
            if (inTank.isEmpty()) continue;
            if (!contents.isEmpty()
                    && !FluidStack.isSameFluidSameComponents(contents.fluid(), inTank)) {
                continue;
            }
            source = inTank;
            break;
        }
        if (source.isEmpty()) return false;

        int want = Math.min(contents.remaining(), source.getAmount());
        if (want <= 0) return false;

        FluidStack actual = handler.drain(source.copyWithAmount(want), IFluidHandler.FluidAction.EXECUTE);
        if (actual.isEmpty()) return false;

        CreateHandMadeApi.setInfusionGunContents(gun, contents.withFill(actual));
        damageGun(gun, user);

        level.playSound(null, pos, FluidHelper.getFillSound(actual),
                SoundSource.PLAYERS, 0.7f, 1.0f + level.random.nextFloat() * 0.2f);

        return true;
    }

    public static boolean tryPickupFluidBlock(Level level, BlockPos pos, LivingEntity user, ItemStack gun) {
        FluidStack source = CreateHandMadeApi.fluidSourceAt(level, pos);
        if (source.isEmpty()) return false;

        InfusionGunContents contents = CreateHandMadeApi.infusionGunContents(gun);

        if (!contents.isEmpty() && !FluidStack.isSameFluidSameComponents(contents.fluid(), source)) {
            return false;
        }
        if (contents.remaining() < FLUID_BLOCK_AMOUNT) return false;

        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        level.playSound(null, pos, FluidHelper.getFillSound(source),
                SoundSource.PLAYERS, 1.0f, 1.0f);

        CreateHandMadeApi.setInfusionGunContents(gun, contents.withFill(source));
        damageGun(gun, user);
        return true;
    }

    public static boolean tryInjectItems(Level level, BlockPos pos, LivingEntity user, ItemStack gun) {
        TransportedItemStackHandlerBehaviour handler =
                BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) return false;

        InfusionGunContents contents = CreateHandMadeApi.infusionGunContents(gun);
        if (contents.isEmpty()) return false;

        boolean[] success = {false};

        handler.handleProcessingOnAllItems(item -> {
            if (success[0]) return TransportedResult.doNothing();
            ItemStack stack = item.stack;
            if (stack.isEmpty()) return TransportedResult.doNothing();

            RecipeHolder<? extends StandardProcessingRecipe<?>> recipe =
                    InfusionGunItem.findFillingRecipe(level, stack, contents.fluid());
            if (recipe != null) {
                int required = InfusionGunItem.requiredFluidOf(recipe.value()).amount();
                if (contents.fluid().getAmount() < required) return TransportedResult.doNothing();

                List<ItemStack> results = recipe.value().rollResults(level.random);

                success[0] = true;
                CreateHandMadeApi.setInfusionGunContents(gun, contents.withDrain(required));
                InfusionGunItem.broadcastFluidParticles(level, pos, contents.fluid());

                return InfusionGunItem.finishInject(item, stack, results);
            }

            if (GenericItemFilling.canItemBeFilled(level, stack)) {
                int required = GenericItemFilling.getRequiredAmountForItem(level, stack, contents.fluid());
                if (required <= 0) return TransportedResult.doNothing();
                if (contents.fluid().getAmount() < required) return TransportedResult.doNothing();

                ItemStack simulatedResult = GenericItemFilling.fillItem(
                        level, required, stack.copy(), contents.fluid().copy());
                if (simulatedResult.isEmpty()) return TransportedResult.doNothing();

                success[0] = true;
                CreateHandMadeApi.setInfusionGunContents(gun, contents.withDrain(required));
                InfusionGunItem.broadcastFluidParticles(level, pos, contents.fluid());

                List<ItemStack> results = new ArrayList<>();
                results.add(simulatedResult);
                return InfusionGunItem.finishInject(item, stack, results);
            }

            return TransportedResult.doNothing();
        });

        if (success[0]) {
            damageGun(gun, user);
            level.playSound(null, pos, FluidHelper.getEmptySound(contents.fluid()),
                    SoundSource.PLAYERS, 0.8f, 1.0f + level.random.nextFloat() * 0.2f);
        }
        return success[0];
    }

    public static boolean tryStoreFluid(Level level, BlockPos pos, LivingEntity user, ItemStack gun) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler == null) return false;

        InfusionGunContents contents = CreateHandMadeApi.infusionGunContents(gun);
        if (contents.isEmpty()) return false;

        FluidStack fluid = contents.fluid();
        int actual = handler.fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
        if (actual <= 0) return false;

        CreateHandMadeApi.setInfusionGunContents(gun, contents.withDrain(actual));
        damageGun(gun, user);

        level.playSound(null, pos, FluidHelper.getEmptySound(fluid),
                SoundSource.PLAYERS, 1f, 1.0f + level.random.nextFloat() * 0.2f);
        return true;
    }

    public static boolean isFluidSource(Level level, BlockPos pos) {
        return !CreateHandMadeApi.fluidSourceAt(level, pos).isEmpty();
    }

    public static boolean containerHasFluid(Level level, BlockPos pos) {
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
        if (handler == null) return false;
        for (int i = 0; i < handler.getTanks(); i++) {
            if (!handler.getFluidInTank(i).isEmpty()) return true;
        }
        return false;
    }

    public static boolean beltHasInjectable(Level level, BlockPos pos, ItemStack gun) {
        TransportedItemStackHandlerBehaviour handler =
                BlockEntityBehaviour.get(level, pos, TransportedItemStackHandlerBehaviour.TYPE);
        if (handler == null) return false;

        InfusionGunContents contents = CreateHandMadeApi.infusionGunContents(gun);
        if (contents.isEmpty()) return false;

        return InfusionGunItem.canInjectItemsAt(level, pos, contents);
    }

    public static boolean canFillItem(Level level, ItemStack target, FluidStack fluid) {
        if (target.isEmpty() || fluid.isEmpty()) return false;

        RecipeHolder<? extends StandardProcessingRecipe<?>> recipe =
                InfusionGunItem.findFillingRecipe(level, target, fluid);
        if (recipe != null) {
            int required = InfusionGunItem.requiredFluidOf(recipe.value()).amount();
            return fluid.getAmount() >= required;
        }

        if (GenericItemFilling.canItemBeFilled(level, target)) {
            int required = GenericItemFilling.getRequiredAmountForItem(level, target, fluid);
            return required > 0 && fluid.getAmount() >= required;
        }

        return false;
    }

    public static boolean tryInjectDepot(Level level, BlockPos pos, LivingEntity user, ItemStack gun) {
        if (!(level.getBlockEntity(pos) instanceof DepotBlockEntity depot)) return false;

        ItemStack held = depot.getHeldItem();
        if (held.isEmpty()) return false;

        InfusionGunContents contents = CreateHandMadeApi.infusionGunContents(gun);
        if (contents.isEmpty()) return false;

        RecipeHolder<? extends StandardProcessingRecipe<?>> recipe =
                InfusionGunItem.findFillingRecipe(level, held, contents.fluid());
        if (recipe != null) {
            int required = InfusionGunItem.requiredFluidOf(recipe.value()).amount();
            if (contents.fluid().getAmount() < required) return false;

            List<ItemStack> results = recipe.value().rollResults(level.random);

            CreateHandMadeApi.setInfusionGunContents(gun, contents.withDrain(required));
            InfusionGunItem.broadcastFluidParticles(level, pos, contents.fluid());
            damageGun(gun, user);

            depot.setHeldItem(results.isEmpty() ? ItemStack.EMPTY : results.getFirst());
            depot.notifyUpdate();
            depot.sendData();

            return true;
        }

        if (GenericItemFilling.canItemBeFilled(level, held)) {
            int required = GenericItemFilling.getRequiredAmountForItem(level, held, contents.fluid());
            if (required <= 0) return false;
            if (contents.fluid().getAmount() < required) return false;

            ItemStack simulatedResult = GenericItemFilling.fillItem(
                    level, required, held.copy(), contents.fluid().copy());
            if (simulatedResult.isEmpty()) return false;

            CreateHandMadeApi.setInfusionGunContents(gun, contents.withDrain(required));
            InfusionGunItem.broadcastFluidParticles(level, pos, contents.fluid());
            damageGun(gun, user);

            depot.setHeldItem(simulatedResult);
            depot.notifyUpdate();
            depot.sendData();

            return true;
        }

        return false;
    }

    private static void damageGun(ItemStack gun, LivingEntity user) {
        gun.hurtAndBreak(1, user, EquipmentSlot.MAINHAND);
    }
}