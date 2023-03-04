package glodblock.com.github.network;

import glodblock.com.github.gui.ScannerGUI;
import glodblock.com.github.orevisualdetector.Main;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class GuiOpenPacket implements IMessage {

    public GuiOpenPacket() {
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }

    public static class Handler implements IMessageHandler<GuiOpenPacket, IMessage> {
        @Override
        public IMessage onMessage(GuiOpenPacket message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().player;
            player.openGui(Main.instance, ScannerGUI.GUI_ID, player.world,
                    (int) player.posX, (int) player.posY, (int) player.posZ);
            return null;
        }
    }

}
