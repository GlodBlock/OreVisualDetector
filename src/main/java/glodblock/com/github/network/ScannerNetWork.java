package glodblock.com.github.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.relauncher.Side;

import java.util.EnumMap;
import java.util.List;

/**
 * Created by wital_000 on 20.03.2016.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
@ChannelHandler.Sharable
public class ScannerNetWork extends MessageToMessageCodec<FMLProxyPacket, ScannerPacket> {

    static public ScannerNetWork INSTANCE;
    private final EnumMap<Side, FMLEmbeddedChannel> mChannel;

    public ScannerNetWork() {
        INSTANCE = this;
        this.mChannel = NetworkRegistry.INSTANCE.newChannel("Scanner", this, new HandlerShared());
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ScannerPacket msg, List<Object> out) {
        out.add(new FMLProxyPacket(new PacketBuffer(Unpooled.buffer().writeByte(msg.getPacketID()).writeBytes(msg.encode()).copy()), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get()));
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) {
        ByteArrayDataInput aData = ByteStreams.newDataInput(msg.payload().array());
        aData.readByte();
        out.add(ScannerPacket.decode(aData));
    }

    public void sendToPlayer(BasePacket aPacket, EntityPlayerMP aPlayer) {
        this.mChannel.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        this.mChannel.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(aPlayer);
        this.mChannel.get(Side.SERVER).writeAndFlush(aPacket);
    }

    @ChannelHandler.Sharable
    static final class HandlerShared extends SimpleChannelInboundHandler<BasePacket> {
        protected void channelRead0(ChannelHandlerContext ctx, BasePacket aPacket) {
            aPacket.process();
        }
    }

}
