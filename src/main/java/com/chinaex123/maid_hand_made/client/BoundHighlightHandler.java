package com.chinaex123.maid_hand_made.client;

import net.createmod.catnip.outliner.Outliner;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class BoundHighlightHandler {

    public static final int DEFAULT_DURATION = 80;

    private static final int HIGHLIGHT_COLOR = 0x66CCFF;
    private static final float LINE_WIDTH = 1 / 16f;

    private static final Object OUTLINER_SLOT = BoundHighlightHandler.class;

    private static BlockPos highlightPos;
    private static int remainingTicks;

    public static void highlight(BlockPos pos, int durationTicks) {
        highlightPos = pos.immutable();
        remainingTicks = Math.max(durationTicks, 0);
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) return;
        if (highlightPos == null) return;

        if (remainingTicks <= 0) {
            highlightPos = null;
            return;
        }
        remainingTicks--;

        AABB box = new AABB(highlightPos);
        Outliner.getInstance()
                .showAABB(OUTLINER_SLOT, box)
                .colored(HIGHLIGHT_COLOR)
                .lineWidth(LINE_WIDTH)
                .disableLineNormals();
    }
}