package com.chinaex123.maid_hand_made.init;

import com.chinaex123.maid_hand_made.MaidHandMade;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MaidHandMade.MODID);

    /** 女仆手动绑定的机器坐标，null 表示未手动绑定 */
    public static final Supplier<AttachmentType<BlockPos>> MANUAL_BOUND = ATTACHMENT_TYPES.register("manual_bound",
            () -> AttachmentType.<BlockPos>builder(() -> null)
                    .serialize(BlockPos.CODEC).build());
}
