package fuzs.universalenchants.network;

import fuzs.puzzleslib.api.network.v4.message.MessageListener;
import fuzs.puzzleslib.api.network.v4.message.play.ClientboundPlayMessage;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;

import java.util.Objects;

public record ClientboundStopUsingItemMessage() implements ClientboundPlayMessage {
    public static final ClientboundStopUsingItemMessage INSTANCE = new ClientboundStopUsingItemMessage();
    public static final StreamCodec<ByteBuf, ClientboundStopUsingItemMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public MessageListener<Context> getListener() {
        return new MessageListener<Context>() {
            @Override
            public void accept(Context context) {
                LocalPlayer player = context.player();
                if (Objects.equals(player.usingItemHand, InteractionHand.OFF_HAND)) {
                    player.startedUsingItem = false;
                    player.usingItemHand = null;
                }
            }
        };
    }
}
