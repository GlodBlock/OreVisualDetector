package glodblock.com.github.network;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import glodblock.com.github.gui.ScannerGUI;
import glodblock.com.github.gui.ScannerGUITexture;
import glodblock.com.github.handlers.HandleOreData;
import glodblock.com.github.orevisualdetector.Main;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;

/**
 * Created by wital_000 on 20.03.2016.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public class ScannerPacket implements BasePacket {

    public final int chunkX;
    public final int chunkZ;
    public final int posX;
    public final int posZ;
    public final int size;
    public final int ptype;
    public final HashMap<Byte, Short>[][] map;
    public final HashMap<String, Integer> ores;
    public final HashMap<Short, String> metaMap;

    public int level = -1;

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
        String name;
        short[] rgba;
        String unlocalizedName = HandleOreData.mIDToNameMAp.get(id);
        try {
            if(packet.ptype == 0 || packet.ptype == 1) {
                rgba = new short[]{0, 0, 0};
                if (HandleOreData.mOreDictMap.containsKey(unlocalizedName)) {
                    rgba = HandleOreData.mOreDictMap.get(unlocalizedName);
                }
                else if (HandleOreData.mUnlocalizedMap.containsKey(unlocalizedName)) {
                    rgba = HandleOreData.mUnlocalizedMap.get(unlocalizedName);
                }
                name = HandleOreData.mIDToDisplayNameMAp.get(id);
                HandleOreData.mTranslate.put(unlocalizedName, name);
            } else {
                return;
            }
        } catch (Exception ignored) {
            return;
        }
        //TheDisorder.Logger.info(id + unlocalizedName + ((rgba[0] & 0xFF) << 16) + ((rgba[1] & 0xFF) << 8) + ((rgba[2] & 0xFF)));
        packet.ores.put(unlocalizedName, ((rgba[0] & 0xFF) << 16) + ((rgba[1] & 0xFF) << 8) + ((rgba[2] & 0xFF)));
        packet.metaMap.put(id, unlocalizedName);
    }

    public static Object decode(ByteArrayDataInput aData) {
        ScannerPacket packet = new ScannerPacket(aData.readInt(), aData.readInt(), aData.readInt(), aData.readInt(), aData.readInt(), aData.readInt());
        packet.level = aData.readInt();

        int aSize = (packet.size * 2 + 1) * 16;
        int checkOut = 0;
        for (int i = 0; i < aSize; i++)
            for (int j = 0; j < aSize; j++) {
                byte kSize = aData.readByte();
                if(kSize == 0) continue;
                packet.map[i][j] = new HashMap<>();
                for (int k = 0; k < kSize; k++) {
                    final byte y = aData.readByte();
                    final short meta = aData.readShort();
                    packet.map[i][j].put(y, meta);
                    //if (packet.ptype != 2 || y == 1) addOre(packet, y, i, j, meta);
                    checkOut++;
                }
            }
        int checkOut2 = aData.readInt();
        if(checkOut != checkOut2) return null;
        return packet;
    }

    @Override
    public int getPacketID() {
        return 0;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public byte[] encode() {
        ByteArrayDataOutput tOut = ByteStreams.newDataOutput(1);
        tOut.writeInt(chunkX);
        tOut.writeInt(chunkZ);
        tOut.writeInt(posX);
        tOut.writeInt(posZ);
        tOut.writeInt(size);
        tOut.writeInt(ptype);
        tOut.writeInt(level);

        int aSize = (size*2+1)*16;
        int checkOut = 0;
        for(int i =0; i<aSize; i++)
            for(int j =0; j<aSize; j++) {
                if(map[i][j]==null)
                    tOut.writeByte(0);
                else {
                    tOut.writeByte(map[i][j].keySet().size());
                    for(byte key : map[i][j].keySet()) {
                        tOut.writeByte(key);
                        tOut.writeShort(map[i][j].get(key));
                        checkOut++;
                    }
                }
            }
        tOut.writeInt(checkOut);
        return tOut.toByteArray();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void process() {
        if (Thread.currentThread().getName().equals("Client thread")) {
            ScannerGUI.newMap(new ScannerGUITexture(this));
            Main.proxy.openProspectorGUI();
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

}
