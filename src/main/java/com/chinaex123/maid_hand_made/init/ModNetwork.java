package com.chinaex123.maid_hand_made.init;

import com.chinaex123.maid_hand_made.MaidHandMade;
import com.chinaex123.maid_hand_made.client.BoundHighlightHandler;
import com.chinaex123.maid_hand_made.network.BoundHighlightPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 本模组网络包注册中心。
 * <p>
 * 负责向 NeoForge PayloadRegistrar 注册所有自定义网络包，
 * 并绑定各自的服务端/客户端处理逻辑。
 */
public final class ModNetwork {

    /**
     * 私有构造函数，防止实例化。
     */
    private ModNetwork() {}

    /**
     * 向模组事件总线注册所有 Payload。
     *
     * @param eventBus 模组事件总线
     */
    public static void register(IEventBus eventBus) {
        eventBus.addListener(RegisterPayloadHandlersEvent.class, event ->
                event.registrar(MaidHandMade.MODID)
                        .playToClient(BoundHighlightPacket.TYPE, BoundHighlightPacket.CODEC,
                                ModNetwork::handleBoundHighlight)
        );
    }

    /**
     * 处理高亮包（客户端侧）。
     * <p>
     * 主线程中将 BlockPos 交给 BoundHighlightHandler，
     * 由其在渲染事件中用 Catnip Outliner 画出彩色 AABB 框。
     *
     * @param packet  高亮包
     * @param context Payload 上下文
     */
    private static void handleBoundHighlight(BoundHighlightPacket packet, IPayloadContext context) {
        context.enqueueWork(() ->
                BoundHighlightHandler.highlight(packet.pos(), packet.durationTicks()));
    }
}