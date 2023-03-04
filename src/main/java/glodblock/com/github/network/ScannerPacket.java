package glodblock.com.github.network;

import glodblock.com.github.gui.ScannerGUI;
import glodblock.com.github.gui.ScannerGUITexture;
import glodblock.com.github.handlers.HandleOreData;
import glodblock.com.github.handlers.HandlerIEVein;
import glodblock.com.github.orevisualdetector.Main;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wital_000 on 20.03.2016.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public class ScannerPacket implements IMessage {

    public int chunkX;
    public int chunkZ;
    public int posX;
    public int posZ;
    public int size;
    public int ptype;
    public HashMap<Byte, Short>[][] map;
    public HashMap<String, Integer> ores;
    public HashMap<Short, String> metaMap;

    public int level = -1;

    public ScannerPacket() {
    }

    @SuppressWarnings("unchecked")
    public ScannerPacket(int chunkX, int chunkZ, int posX, int posZ, int size, int ptype) {
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.posX = posX;
        this.posZ = posZ;
        this.size = size;
        this.ptype = ptype;
        this.map = new HashMap[(size*2+1)*16][(size*2+1)*16];
        this.ores = new HashMap<>();
        this.metaMap = new HashMap<>();
    }

    public static void addOre(ScannerPacket packet, short id) {
        short[] rgba;
        try {
            if(packet.ptype == 0) {
                String unlocalizedName = HandleOreData.mIDToNameMap.get(id);
                rgba = new short[]{0, 0, 0};
                if (HandleOreData.mOreDictMap.containsKey(unlocalizedName)) {
                    rgba = HandleOreData.mOreDictMap.get(unlocalizedName);
                } else if (HandleOreData.mUnlocalizedMap.containsKey(unlocalizedName)) {
                    rgba = HandleOreData.mUnlocalizedMap.get(unlocalizedName);
                }
                packet.ores.put(unlocalizedName, ((rgba[0] & 0xFF) << 16) + ((rgba[1] & 0xFF) << 8) + ((rgba[2] & 0xFF)));
                packet.metaMap.put(id, unlocalizedName);
            } else if (packet.ptype == 1) {
                if (HandlerIEVein.IDToVeinMap.containsKey(id)) {
                    String veinName = HandlerIEVein.IDToVeinMap.get(id);
                    rgba = HandlerIEVein.veinMap.get(veinName);
                    packet.ores.put(veinName, ((rgba[0] & 0xFF) << 16) + ((rgba[1] & 0xFF) << 8) + ((rgba[2] & 0xFF)));
                    packet.metaMap.put(id, veinName);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public void addBlock(int x, int y, int z, short id) {
        int aX = x - (chunkX-size)*16;
        int aZ = z - (chunkZ-size)*16;
        if(map[aX][aZ] == null) map[aX][aZ] = new HashMap<>();
        map[aX][aZ].put((byte) y, id);
    }

    public int getSize() {
        return (size * 2 + 1) * 16;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void fromBytes(ByteBuf buf) {
        this.chunkX = buf.readInt();
        this.chunkZ = buf.readInt();
        this.posX = buf.readInt();
        this.posZ = buf.readInt();
        this.size = buf.readInt();
        this.ptype = buf.readInt();
        this.map = new HashMap[(size*2+1)*16][(size*2+1)*16];
        this.ores = new HashMap<>();
        this.metaMap = new HashMap<>();
        this.level = buf.readInt();
        int aSize = (this.size * 2 + 1) * 16;
        for (int i = 0; i < aSize; i++)
            for (int j = 0; j < aSize; j++) {
                byte kSize = buf.readByte();
                if(kSize == 0) continue;
                this.map[i][j] = new HashMap<>();
                for (int k = 0; k < kSize; k++) {
                    final byte y = buf.readByte();
                    final short meta = buf.readShort();
                    this.map[i][j].put(y, meta);
                }
            }
        int size = buf.readShort();
        for (int i = 0; i < size; i ++) {
            short id = buf.readShort();
            int color = buf.readInt();
            if (this.ptype == 0) {
                this.ores.put(HandleOreData.mIDToNameMap.get(id), color);
                this.metaMap.put(id, HandleOreData.mIDToNameMap.get(id));
            } else if (this.ptype == 1) {
                this.ores.put(HandlerIEVein.IDToVeinMap.get(id), color);
                this.metaMap.put(id, HandlerIEVein.IDToVeinMap.get(id));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(chunkX);
        buf.writeInt(chunkZ);
        buf.writeInt(posX);
        buf.writeInt(posZ);
        buf.writeInt(size);
        buf.writeInt(ptype);
        buf.writeInt(level);
        int aSize = (size*2+1)*16;
        for(int i =0; i<aSize; i++)
            for(int j =0; j<aSize; j++) {
                if(map[i][j]==null)
                    buf.writeByte(0);
                else {
                    buf.writeByte(map[i][j].keySet().size());
                    for(byte key : map[i][j].keySet()) {
                        buf.writeByte(key);
                        buf.writeShort(map[i][j].get(key));
                    }
                }
            }
        buf.writeShort(ores.size());
        for (Map.Entry<String, Integer> p : ores.entrySet()) {
            String name = p.getKey();
            int color = p.getValue();
            if (this.ptype == 0) {
                buf.writeShort(HandleOreData.mNameToIDMap.get(name));
                buf.writeInt(color);
            } else if (this.ptype == 1) {
                buf.writeShort(HandlerIEVein.veinToIDMap.get(name));
                buf.writeInt(color);
            }
        }
    }

    public static class Handler implements IMessageHandler<ScannerPacket, IMessage> {

        @Nullable
        @Override
        public IMessage onMessage(ScannerPacket message, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                ScannerGUI.newMap(new ScannerGUITexture(message));
                Main.proxy.netHandler.sendToServer(new GuiOpenPacket());
            });
            return null;
        }

    }

}
