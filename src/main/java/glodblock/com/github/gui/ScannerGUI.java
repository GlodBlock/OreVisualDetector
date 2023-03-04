package glodblock.com.github.gui;

import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import glodblock.com.github.handlers.HandlerIEVein;
import glodblock.com.github.handlers.HandlerOilVein;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by wital_000 on 21.03.2016.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public class ScannerGUI extends GuiScreen {

    public static final int GUI_ID = 20;
    private static ScannerGUITexture map = null;
    OreList oresList = null;

    private final static int minHeight = 128;
    private final static int minWidth = 128;
    private int prevW;
    private int prevH;

    private static final ResourceLocation back = new ResourceLocation("orevisualdetector:textures/gui/propick.png");

    public ScannerGUI() {

    }

    @SideOnly(Side.CLIENT)
    public static void newMap(ScannerGUITexture aMap) {
        if (map != null) {
            map.deleteGlTexture();
        }
        map = aMap;
        map.loadTexture(null);
    }

    @Override
    public void drawScreen(int x, int y, float f) {
        this.drawDefaultBackground();
        if(map == null) return;
        int currentWidth = Math.max(map.width, minWidth);
        int currentHeight = Math.max(map.height, minHeight);
        int aX = (this.width - currentWidth-100)/2;
        int aY = (this.height - currentHeight)/2;

        if(oresList == null || (prevW != width || prevH != height)) {
            oresList = new OreList(
                    this, 100, currentHeight, aY, aY+currentHeight, aX+currentWidth, 10, map.packet.ores,
                    ((name, invert) -> {
                        if (map != null) map.loadTexture(null, name, invert);
                    }), map.packet.ptype
            );
            prevW = width;
            prevH = height;
        }

        // draw back for ores
        drawRect(aX,aY,aX+currentWidth+100,aY+currentHeight,0xFFC6C6C6);

        map.glBindTexture();
        map.draw(aX,aY);
        oresList.drawScreen(x, y, f);
        mc.getTextureManager().bindTexture(back);
        GL11.glColor4f(0xFF, 0xFF, 0xFF, 0xFF);

        // draw corners
        drawTexturedModalRect(aX-5,aY-5,0,0,5,5);//leftTop
        drawTexturedModalRect(aX+currentWidth+100,aY-5,171,0,5,5);//RightTop
        drawTexturedModalRect(aX-5,aY+currentHeight,0,161,5,5);//leftDown
        drawTexturedModalRect(aX+currentWidth+100,aY+currentHeight,171,161,5,5);//RightDown

        // draw edges
        for(int i = aX ; i <aX + currentWidth + 100 ; i += 128) drawTexturedModalRect(i,aY-5,5,0,Math.min(128,aX+currentWidth+100-i),5); //top
        for(int i = aX ; i <aX + currentWidth + 100 ; i += 128) drawTexturedModalRect(i,aY+currentHeight,5,161,Math.min(128,aX+currentWidth+100-i),5); //down
        for(int i = aY ; i <aY + currentHeight ; i += 128) drawTexturedModalRect(aX-5,i,0,5,5,Math.min(128,aY + currentHeight-i)); //left
        for(int i = aY ; i <aY + currentHeight ; i += 128) drawTexturedModalRect(aX+currentWidth+100,i,171,5,5,Math.min(128,aY+currentHeight-i)); //right

        HashMap<Byte, Short>[][] veinInfo = map.packet.map;
        int tX = x - aX;
        int tY = y - aY;
        if (map.packet.ptype == 1) {
            if (tX >= 0 && tY >= 0 && tX < veinInfo.length && tY < veinInfo[0].length) {
                List<String> info = new ArrayList<>();
                if (veinInfo[tX][tY] != null && veinInfo[tX][tY].containsKey((byte) 255)) {
                    short veinID = veinInfo[tX][tY].get((byte) 255);
                    if (map.selected.equals("All") || map.selected.equals(HandlerIEVein.IDToVeinMap.get(veinID))) {
                        ExcavatorHandler.MineralMix vein = HandlerIEVein.IDToMaterialMap.get(veinID);
                        String displayString;
                        if (I18n.hasKey("desc.immersiveengineering.info.mineral." + vein.name)) {
                            displayString = I18n.format("desc.immersiveengineering.info.mineral." + vein.name);
                        } else {
                            displayString = vein.name;
                        }
                        info.add(TextFormatting.AQUA + displayString);
                        for (Pair<String, Float> p : HandlerIEVein.getMaterialList(vein)) {
                            info.add(String.format("%s %.2f%%", p.getKey(), p.getValue() * 100));
                        }
                    }
                }
                this.drawHoveringText(info, x, y);
            }
        } else if (map.packet.ptype == 2) {
            if (tX >= 0 && tY >= 0 && tX < veinInfo.length && tY < veinInfo[0].length) {
                List<String> info = new ArrayList<>();
                if (veinInfo[tX][tY] != null && veinInfo[tX][tY].containsKey((byte) 0)) {
                    short fluidID = veinInfo[tX][tY].get((byte) 0);
                    ByteBuf buf = Unpooled.buffer(64);
                    buf.writeShort(veinInfo[tX][tY].get((byte) 1));
                    buf.writeShort(veinInfo[tX][tY].get((byte) 2));
                    int amount = buf.readInt();
                    buf.writeShort(veinInfo[tX][tY].get((byte) 3));
                    buf.writeShort(veinInfo[tX][tY].get((byte) 4));
                    int recover = buf.readInt();
                    Fluid fluid = HandlerOilVein.getFluid(fluidID);
                    if (map.selected.equals("All") || map.selected.equals(fluid.getName())) {
                        String displayString = fluid.getLocalizedName(new FluidStack(fluid, 1));
                        info.add(TextFormatting.AQUA + displayString);
                        info.add(I18n.format("scanner.gui.fluid.amount", amount));
                        info.add(I18n.format("scanner.gui.fluid.recover", recover));
                    }
                }
                this.drawHoveringText(info, x, y);
            }
        }
    }

}
