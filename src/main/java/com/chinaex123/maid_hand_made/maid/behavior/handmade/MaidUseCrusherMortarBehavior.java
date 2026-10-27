package com.chinaex123.maid_hand_made.maid.behavior.handmade;

import com.alben.createhandmade.api.CreateHandMadeApi;
import com.alben.createhandmade.item.CrusherMortarItem;
import com.alben.createhandmade.item.MortarContents;
import com.chinaex123.maid_hand_made.util.MaidInventoryHelper;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * 碾钵。
 */
public class MaidUseCrusherMortarBehavior extends MaidCheckRateTask {

    private static final int FORCED_DURATION = 100;
    private static final int WINDUP_TICKS = FORCED_DURATION / 5;

    private int grindTicks;
    private boolean finished;

    public MaidUseCrusherMortarBehavior() {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                InitEntities.TARGET_POS.get(), MemoryStatus.REGISTERED
        ));
        setMaxCheckRate(20);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel world, @NotNull EntityMaid maid) {
        if (!super.checkExtraStartConditions(world, maid)) return false;

        // 主手是碾钵，或者背包里有碾钵
        boolean hasMortar = maid.getMainHandItem().getItem() instanceof CrusherMortarItem
                || MaidInventoryHelper.findSlot(maid, s -> s.getItem() instanceof CrusherMortarItem) >= 0;
        if (!hasMortar) return false;

        // 副手有原料，或者背包里有可补的原料
        return hasIngredientInOffhand(maid)
                || MaidInventoryHelper.findSlot(maid, s -> findRecipe(world, s) != null) >= 0;
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long time) {
        // 锁住移动
        maid.getNavigation().stop();
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        // 把碾钵移到主手
        if (!ensureMortarInMainHand(maid)) {
            finished = true;
            return;
        }

        // 副手没原料就从背包补
        if (!hasIngredientInOffhand(maid)) {
            refillOffhand(world, maid);
        }
        if (!hasIngredientInOffhand(maid)) {
            finished = true;
            return;
        }

        // 把副手原料写进碾钵组件
        ItemStack input = maid.getOffhandItem();
        ItemStack mortar = maid.getMainHandItem();
        if (!(mortar.getItem() instanceof CrusherMortarItem)) {
            finished = true;
            return;
        }

        ItemStack toGrind = input.copyWithCount(1);
        CreateHandMadeApi.setMortarContents(mortar, new MortarContents(toGrind, 0));

        // 消耗副手 1 个原料
        input.shrink(1);
        if (input.isEmpty()) {
            maid.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        } else {
            maid.setItemInHand(InteractionHand.OFF_HAND, input);
        }

        grindTicks = 0;
        finished = false;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel world, @NotNull EntityMaid maid, long time) {
        if (finished) return false;

        ItemStack mortar = maid.getMainHandItem();
        return mortar.getItem() instanceof CrusherMortarItem
                && CreateHandMadeApi.mortarContents(mortar) != null;
    }

    @Override
    protected boolean timedOut(long time) {
        return false;
    }

    @Override
    protected void tick(ServerLevel world, @NotNull EntityMaid maid, long time) {
        // 每 tick 锁住移动
        maid.getNavigation().stop();
        maid.setDeltaMovement(0, maid.getDeltaMovement().y, 0);
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);

        grindTicks++;

        if (grindTicks < FORCED_DURATION) {
            if (grindTicks >= WINDUP_TICKS && grindTicks % 4 == 0) {
                playGrindEffects(world, maid);
            }
            return;
        }

        // 完成
        finishGrinding(world, maid);
        finished = true;
    }

    @Override
    protected void stop(@NotNull ServerLevel world, @NotNull EntityMaid maid, long time) {
        super.stop(world, maid, time);

        // 中断时退还碾钵里未完成的原料
        ItemStack mortar = maid.getMainHandItem();
        if (mortar.getItem() instanceof CrusherMortarItem) {
            MortarContents contents = CreateHandMadeApi.mortarContents(mortar);
            if (contents != null && !contents.stack().isEmpty()) {
                MaidInventoryHelper.addToBackpack(maid, contents.stack());
            }
            CreateHandMadeApi.setMortarContents(mortar, null);
        }

        grindTicks = 0;
        finished = false;
    }

    private boolean ensureMortarInMainHand(EntityMaid maid) {
        if (maid.getMainHandItem().getItem() instanceof CrusherMortarItem) return true;

        IItemHandler inventory = MaidInventoryHelper.getBackpack(maid);
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (inventory.getStackInSlot(i).getItem() instanceof CrusherMortarItem) {
                ItemStack taken = inventory.extractItem(i, 1, false);
                if (taken.isEmpty()) continue;

                ItemStack main = maid.getMainHandItem();
                if (!main.isEmpty()) {
                    MaidInventoryHelper.addToBackpack(maid, main);
                }
                maid.setItemInHand(InteractionHand.MAIN_HAND, taken);
                return true;
            }
        }
        return false;
    }

    private boolean hasIngredientInOffhand(EntityMaid maid) {
        ItemStack offhand = maid.getOffhandItem();
        return !offhand.isEmpty() && !(offhand.getItem() instanceof CrusherMortarItem);
    }

    private void refillOffhand(ServerLevel world, EntityMaid maid) {
        if (!maid.getOffhandItem().isEmpty()) return;

        IItemHandler inventory = MaidInventoryHelper.getBackpack(maid);
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof CrusherMortarItem) continue;
            if (findRecipe(world, stack) == null) continue;

            ItemStack taken = inventory.extractItem(i, 1, false);
            if (taken.isEmpty()) continue;
            maid.setItemInHand(InteractionHand.OFF_HAND, taken);
            return;
        }
    }

    private RecipeHolder<?> findRecipe(ServerLevel world, ItemStack input) {
        return CrusherMortarItem.findRecipe(world, input);
    }

    private void finishGrinding(ServerLevel world, EntityMaid maid) {
        ItemStack mortar = maid.getMainHandItem();
        if (!(mortar.getItem() instanceof CrusherMortarItem)) return;

        MortarContents contents = CreateHandMadeApi.mortarContents(mortar);
        if (contents == null || contents.stack().isEmpty()) return;

        RecipeHolder<?> recipe = findRecipe(world, contents.stack());
        if (recipe != null && recipe.value() instanceof ProcessingRecipe<?, ?> processing) {
            List<ItemStack> results = processing.rollResults(world.random);
            for (ItemStack result : results) {
                if (!result.isEmpty()) {
                    MaidInventoryHelper.addToBackpack(maid, result);
                }
            }
        }

        CreateHandMadeApi.setMortarContents(mortar, null);
        mortar.hurtAndBreak(1, maid, EquipmentSlot.MAINHAND);
    }

    private void playGrindEffects(ServerLevel world, EntityMaid maid) {
        world.playSound(null, maid.blockPosition(),
                SoundEvents.GRINDSTONE_USE,
                SoundSource.PLAYERS, 1f,
                2.0f + (world.random.nextFloat() - 0.5f) * 0.2f);

        ItemStack mortar = maid.getMainHandItem();
        MortarContents contents = CreateHandMadeApi.mortarContents(mortar);
        if (contents == null) return;

        Vec3 eye = maid.getEyePosition(1f);
        Vec3 look = maid.getLookAngle();
        Vec3 spawnPos = eye.add(look.scale(0.5)).add(0, -0.3, 0);

        for (int i = 0; i < 2; i++) {
            double angle = world.random.nextDouble() * Math.PI * 2;
            double horizSpeed = 0.05 + world.random.nextDouble() * 0.1;
            double vx = Math.cos(angle) * horizSpeed;
            double vz = Math.sin(angle) * horizSpeed;
            double vy = 0.15 + world.random.nextDouble() * 0.15;

            world.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, contents.stack()),
                    spawnPos.x, spawnPos.y, spawnPos.z,
                    1, vx, vy, vz, 0.0
            );
        }
    }
}