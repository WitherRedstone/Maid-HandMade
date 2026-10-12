package com.chinaex123.maid_hand_made.maid.behavior;

import com.chinaex123.maid_hand_made.init.ModDataComponents;
import com.github.tartaricacid.touhoulittlemaid.entity.ai.brain.task.MaidCheckRateTask;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.EntityMaid;
import com.github.tartaricacid.touhoulittlemaid.entity.passive.MaidPathFindingBFS;
import com.github.tartaricacid.touhoulittlemaid.init.InitEntities;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class MaidUseItemOnMachineBehavior extends MaidCheckRateTask {

    protected static final long MAX_VISIT_TICKS = 600;
    protected static final long MAX_IDLE_TICKS = 80;
    protected static final long CLAIM_TIMEOUT = 600;
    protected static final long VISIT_MEMORY_TICKS = 12000;

    protected static final long COOLDOWN_WORKED = 200;
    protected static final long COOLDOWN_FINISHED = 100;
    protected static final long COOLDOWN_INTERRUPTED = 120;
    protected static final long COOLDOWN_UNREACHABLE = 100;

    private static final Map<BlockPos, UUID> CLAIMS = new HashMap<>();
    private static final Map<BlockPos, Long> CLAIM_TIMES = new HashMap<>();
    private static final Map<BlockPos, Long> LAST_VISITS = new HashMap<>();
    private static final Map<BlockPos, UUID> BOUND = new HashMap<>();

    private final float movementSpeed;

    private final Map<BlockPos, Long> cooldowns = new HashMap<>();

    protected BlockPos boundPos;
    protected BlockPos approach;

    private long visitStarted;
    private long lastProgress;

    protected boolean finished;
    protected boolean completedWork;
    protected boolean arrived;

    public MaidUseItemOnMachineBehavior(float movementSpeed) {
        super(ImmutableMap.of(
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                InitEntities.TARGET_POS.get(), MemoryStatus.REGISTERED
        ));
        this.movementSpeed = movementSpeed;
        setMaxCheckRate(20);
    }

    protected abstract boolean isCorrectItem(ItemStack stack);

    protected abstract boolean isMachine(ServerLevel world, BlockPos pos);

    protected abstract boolean isMachineBlock(ServerLevel world, BlockPos pos);

    protected abstract boolean useItemOnMachine(ServerLevel world, EntityMaid maid,
                                                BlockPos pos, ItemStack stack);

    protected boolean useHandsMode() {
        return false;
    }

    protected ItemStack getMainHandStack(EntityMaid maid) {
        return ItemStack.EMPTY;
    }

    protected ItemStack getOffHandStack(EntityMaid maid) {
        return ItemStack.EMPTY;
    }

    protected boolean isMachineFor(ServerLevel world, EntityMaid maid, BlockPos pos) {
        return isMachine(world, pos);
    }

    private static boolean claim(BlockPos pos, EntityMaid maid, long now) {
        UUID owner = CLAIMS.get(pos);
        if (owner != null && !owner.equals(maid.getUUID())) {
            Long t = CLAIM_TIMES.get(pos);
            if (t != null && now - t < CLAIM_TIMEOUT) {
                return false;
            }
        }
        CLAIMS.put(pos, maid.getUUID());
        CLAIM_TIMES.put(pos, now);
        return true;
    }

    private static void release(BlockPos pos, EntityMaid maid) {
        UUID owner = CLAIMS.get(pos);
        if (owner != null && owner.equals(maid.getUUID())) {
            CLAIMS.remove(pos);
            CLAIM_TIMES.remove(pos);
        }
    }

    private static boolean isClaimedByMe(BlockPos pos, EntityMaid maid) {
        UUID owner = CLAIMS.get(pos);
        return owner == null || owner.equals(maid.getUUID());
    }

    private static boolean isBoundByMe(BlockPos pos, EntityMaid maid) {
        UUID owner = BOUND.get(pos);
        return owner == null || owner.equals(maid.getUUID());
    }

    private static void bind(BlockPos pos, EntityMaid maid) {
        BOUND.put(pos, maid.getUUID());
    }

    private static void unbind(BlockPos pos, EntityMaid maid) {
        UUID owner = BOUND.get(pos);
        if (owner != null && owner.equals(maid.getUUID())) {
            BOUND.remove(pos);
        }
    }

    private static void cleanupClaims(long now) {
        CLAIM_TIMES.entrySet().removeIf(e -> now - e.getValue() > CLAIM_TIMEOUT);
        CLAIMS.keySet().removeIf(p -> !CLAIM_TIMES.containsKey(p));
    }

    private static void cleanupVisits(long now) {
        LAST_VISITS.entrySet().removeIf(e -> now - e.getValue() > VISIT_MEMORY_TICKS);
    }

    protected static double horizontalDistance(EntityMaid maid, BlockPos pos) {
        return maid.distanceToSqr(pos.getX() + 0.5, maid.getY(), pos.getZ() + 0.5);
    }

    private static boolean expired(long visitTicks, long idleTicks) {
        return visitTicks >= MAX_VISIT_TICKS || idleTicks >= MAX_IDLE_TICKS;
    }

    protected IItemHandler workInventory(EntityMaid maid) {
        return maid.getCapability(Capabilities.ItemHandler.ENTITY);
    }

    protected int findItemSlot(IItemHandler inventory) {
        if (inventory == null) return -1;
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (isCorrectItem(inventory.getStackInSlot(i))) {
                return i;
            }
        }
        return -1;
    }

    protected void returnToBackpack(EntityMaid maid, IItemHandler inventory, ItemStack itemStack) {
        if (itemStack.isEmpty()) return;
        ItemStack remainder = ItemHandlerHelper.insertItemStacked(inventory, itemStack, false);
        if (!remainder.isEmpty()) maid.spawnAtLocation(remainder);
    }

    protected BlockPos getManualBound(EntityMaid maid) {
        if (useHandsMode()) {
            ItemStack main = getMainHandStack(maid);
            if (!main.isEmpty() && isCorrectItem(main)) {
                return main.get(ModDataComponents.BOUND_POS.get());
            }
            return null;
        }
        IItemHandler inventory = workInventory(maid);
        if (inventory == null) return null;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (isCorrectItem(stack)) {
                BlockPos pos = stack.get(ModDataComponents.BOUND_POS.get());
                if (pos != null) return pos;
            }
        }
        return null;
    }

    protected void clearManualBound(EntityMaid maid) {
        if (useHandsMode()) {
            ItemStack main = getMainHandStack(maid);
            if (!main.isEmpty() && isCorrectItem(main)) {
                main.remove(ModDataComponents.BOUND_POS.get());
                main.remove(ModDataComponents.BOUND_TEXT.get());
            }
            return;
        }
        IItemHandler inventory = workInventory(maid);
        if (inventory == null) return;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (isCorrectItem(stack)) {
                stack.remove(ModDataComponents.BOUND_POS.get());
                stack.remove(ModDataComponents.BOUND_TEXT.get());
            }
        }
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isBoundValid(ServerLevel world, EntityMaid maid) {
        if (boundPos == null) return false;
        if (!world.isLoaded(boundPos)) return false;
        if (getManualBound(maid) != null) {
            return isMachineBlock(world, boundPos)
                    && isBoundByMe(boundPos, maid)
                    && maid.isWithinRestriction(boundPos);
        }
        if (!isMachineFor(world, maid, boundPos)) return false;
        if (!isBoundByMe(boundPos, maid)) return false;
        return maid.isWithinRestriction(boundPos);
    }

    private boolean allowed(ServerLevel world, EntityMaid maid, BlockPos pos) {
        return maid.isWithinRestriction(pos)
                && world.isLoaded(pos)
                && isMachineFor(world, maid, pos)
                && isClaimedByMe(pos, maid)
                && isBoundByMe(pos, maid);
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel world, @NotNull EntityMaid maid) {
        if (!super.checkExtraStartConditions(world, maid)) return false;

        if (useHandsMode()) {
            ItemStack main = getMainHandStack(maid);
            if (main.isEmpty() || !isCorrectItem(main)) return false;
        } else {
            IItemHandler inventory = workInventory(maid);
            if (findItemSlot(inventory) < 0) return false;
        }

        long now = world.getGameTime();
        cooldowns.entrySet().removeIf(e -> e.getValue() <= now);
        cleanupClaims(now);
        cleanupVisits(now);

        BlockPos manual = getManualBound(maid);
        if (manual != null && !isMachineBlock(world, manual)) {
            clearManualBound(maid);
            if (boundPos != null) {
                release(boundPos, maid);
            }
            boundPos = null;
            approach = null;
            manual = null;
        }

        if (manual != null) {
            if (!claim(manual, maid, now)) {
                return false;
            }
            boundPos = manual;
            bind(manual, maid);

            if (approach == null || !isValidStandPos(world, manual, approach)) {
                approach = findApproach(world, maid, manual);
                if (approach == null) {
                    return false;
                }
            }
        } else {
            if (boundPos != null && !isBoundValid(world, maid)) {
                release(boundPos, maid);
                unbind(boundPos, maid);
                cooldowns.put(boundPos, now + COOLDOWN_INTERRUPTED);
                boundPos = null;
                approach = null;
                arrived = false;
                maid.getNavigation().stop();
                maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            }

            if (boundPos == null) {
                BlockPos found = findMachine(world, maid);
                if (found != null && isBoundByMe(found, maid) && claim(found, maid, now)) {
                    boundPos = found;
                    bind(found, maid);
                    arrived = false;
                }
            }
        }

        if (boundPos == null) return false;

        if (horizontalDistance(maid, boundPos) < 1.44
                && Math.abs(maid.getY() - boundPos.getY()) < 1.5) {
            maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            return true;
        }

        BehaviorUtils.setWalkAndLookTargetMemories(
                maid, approach == null ? boundPos : approach, movementSpeed, 0);
        return false;
    }

    @Override
    protected void start(ServerLevel world, EntityMaid maid, long time) {
        maid.getNavigation().stop();
        finished = false;
        completedWork = false;
        arrived = true;
        visitStarted = lastProgress = time;

        if (boundPos != null && isMachineFor(world, maid, boundPos)) {
            onStart(world, maid, boundPos);
        }
    }

    protected void onStart(ServerLevel world, EntityMaid maid, BlockPos pos) {
    }

    @Override
    protected boolean timedOut(long time) {
        return false;
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel world, @NotNull EntityMaid maid, long time) {
        if (finished || boundPos == null) return false;
        if (!isBoundValid(world, maid)) return false;
        if (!isClaimedByMe(boundPos, maid)) return false;
        if (horizontalDistance(maid, boundPos) >= 2.25) return false;
        if (Math.abs(maid.getY() - boundPos.getY()) >= 2.5) return false;

        if (getManualBound(maid) != null) return true;

        return !expired(time - visitStarted, time - lastProgress);
    }

    @Override
    protected void tick(ServerLevel world, @NotNull EntityMaid maid, long time) {
        if (boundPos == null || !isMachineBlock(world, boundPos)) {
            finished = true;
            return;
        }

        if (!isMachineFor(world, maid, boundPos)) {
            if (getManualBound(maid) != null) {
                lastProgress = time;
                return;
            }
            finished = true;
            return;
        }

        ItemStack stack;
        if (useHandsMode()) {
            stack = getMainHandStack(maid);
            if (stack.isEmpty()) {
                finished = true;
                return;
            }
        } else {
            IItemHandler inventory = workInventory(maid);
            int slot = findItemSlot(inventory);
            if (slot < 0) {
                finished = true;
                return;
            }
            stack = inventory.getStackInSlot(slot);
        }

        boolean done = useItemOnMachine(world, maid, boundPos, stack);
        maid.swing(InteractionHand.MAIN_HAND);
        lastProgress = time;

        if (done) {
            finished = true;
        }
    }

    @Override
    protected void stop(@NotNull ServerLevel world, @NotNull EntityMaid maid, long time) {
        super.stop(world, maid, time);
        if (boundPos != null) {
            release(boundPos, maid);
            long cd;
            if (completedWork) {
                cd = COOLDOWN_WORKED;
            } else if (finished) {
                cd = COOLDOWN_FINISHED;
            } else {
                cd = COOLDOWN_INTERRUPTED;
            }
            cooldowns.put(boundPos, time + cd);
            LAST_VISITS.put(boundPos, time);
        }
        approach = null;
        arrived = false;
        maid.getNavigation().stop();
        maid.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
    }

    private BlockPos findMachine(ServerLevel world, EntityMaid maid) {
        BlockPos center = maid.hasRestriction() ? maid.getRestrictCenter() : maid.blockPosition();
        int range = maid.hasRestriction() ? (int) maid.getRestrictRadius() : 16;

        List<BlockPos> candidates = new ArrayList<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-range, -4, -range),
                center.offset(range, 4, range))) {
            if (cooldowns.containsKey(pos)) continue;
            if (!allowed(world, maid, pos)) continue;
            candidates.add(pos.immutable());
        }

        Comparator<BlockPos> nearest = Comparator
                .comparingLong((BlockPos p) -> LAST_VISITS.getOrDefault(p, Long.MIN_VALUE))
                .thenComparingDouble(p -> p.distToCenterSqr(maid.position()));

        candidates.sort(nearest);

        for (BlockPos pos : candidates) {
            BlockPos foot = findApproach(world, maid, pos);
            if (foot != null) {
                approach = foot;
                return pos;
            }
            cooldowns.put(pos, world.getGameTime() + COOLDOWN_UNREACHABLE);
        }
        return null;
    }

    private BlockPos findApproach(ServerLevel world, EntityMaid maid, BlockPos pos) {
        List<BlockPos> feet = new ArrayList<>();
        feet.add(pos.north());
        feet.add(pos.south());
        feet.add(pos.east());
        feet.add(pos.west());
        feet.add(pos.above());
        feet.add(pos);
        feet.sort(Comparator.comparingDouble(p -> p.distToCenterSqr(maid.position())));

        MaidPathFindingBFS paths = new MaidPathFindingBFS(
                maid.getNavigation().getNodeEvaluator(), world, maid);
        try {
            for (BlockPos foot : feet) {
                if (!isValidStandPos(world, pos, foot)) continue;
                if (maid.isWithinRestriction(foot) && paths.canPathReach(foot)) {
                    return foot;
                }
            }
        } finally {
            paths.finish();
        }
        return null;
    }

    protected boolean isValidStandPos(ServerLevel world, BlockPos machinePos, BlockPos standPos) {
        return true;
    }
}