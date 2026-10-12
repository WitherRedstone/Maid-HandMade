package com.chinaex123.maid_hand_made.network;

import com.chinaex123.maid_hand_made.MaidHandMade;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record BoundHighlightPacket(BlockPos pos, int durationTicks) implements CustomPacketPayload {

    public static final Type<BoundHighlightPacket> TYPE = new Type<>(MaidHandMade.id("bound_highlight"));

    public static final StreamCodec<FriendlyByteBuf, BoundHighlightPacket> CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, BoundHighlightPacket::pos,
                    ByteBufCodecs.VAR_INT, BoundHighlightPacket::durationTicks,
                    BoundHighlightPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}