package glodblock.com.github.gui;

import glodblock.com.github.network.ScannerPacket;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResourceManager;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Created by wital_000 on 21.03.2016.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public class ScannerGUITexture extends AbstractTexture {

    public final ScannerPacket packet;
    public String selected = "All";
    public int width = -1;
    public int height = -1;
    public boolean invert = false;

    public ScannerGUITexture(ScannerPacket aPacket) {
        packet = aPacket;
    }

    private BufferedImage getImage() {
        final int backgroundColor = invert ? Color.GRAY.getRGB() : Color.WHITE.getRGB();
        final int wh = (packet.size * 2 + 1) * 16;

        BufferedImage image = new BufferedImage(wh, wh, BufferedImage.TYPE_INT_ARGB);
        WritableRaster raster = image.getRaster();

        int playerI = packet.posX - (packet.chunkX - packet.size) * 16 - 1; // Correct player offset
        int playerJ = packet.posZ - (packet.chunkZ - packet.size) * 16 - 1;
        for (int i = 0; i < wh; i++) {
            for (int j = 0; j < wh; j++) {
                image.setRGB(i, j, backgroundColor);
                if (packet.map[i][j] != null) {
                    if (packet.ptype == 0 || packet.ptype == 1) {
                        for (short meta : packet.map[i][j].values()) {
                            final String name = packet.metaMap.get(meta);
                            if (!selected.equals("All") && !selected.equals(name)) continue;
                            image.setRGB(i, j, packet.ores.getOrDefault(name, Color.BLACK.getRGB()) | 0XFF000000);
                            break;
                        }
                    }
                }
                // draw player pos
                if (i == playerI || j == playerJ) {
                    raster.setSample(i, j, 0, (raster.getSample(i, j, 0) + 255) / 2);
                    raster.setSample(i, j, 1, raster.getSample(i, j, 1) / 2);
                    raster.setSample(i, j, 2, raster.getSample(i, j, 2) / 2);
                }
                // draw grid
                if (i % 16 == 0 || j % 16 == 0) {
                    raster.setSample(i, j, 0, raster.getSample(i, j, 0) / 2);
                    raster.setSample(i, j, 1, raster.getSample(i, j, 1) / 2);
                    raster.setSample(i, j, 2, raster.getSample(i, j, 2) / 2);
                }
            }
        }
        return image;
    }

    @Override
    public void loadTexture(@Nullable IResourceManager resourceManager) {
        this.deleteGlTexture();
        if(packet != null) {
            int tId = getGlTextureId();
            if(tId < 0) return;
            TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), getImage(), false, false);
            width = packet.getSize();
            height = packet.getSize();
        }
    }

    public void loadTexture(IResourceManager resourceManager, boolean invert){
        this.invert = invert;
        loadTexture(resourceManager);
    }

    public void loadTexture(IResourceManager resourceManager, String selected, boolean invert){
        this.selected = selected;
        loadTexture(resourceManager, invert);
    }

    public void glBindTexture() {
        if (this.glTextureId < 0) return;
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.getGlTextureId());
    }

    public void draw(int x, int y) {
        float f = 1F / (float)width;
        float f1 = 1F / (float)height;
        int u = 0, v = 0;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0).tex((float)(u) * f, (float)(v + height) * f1).endVertex();
        bufferbuilder.pos((x + width), (y + height), 0).tex((float)(u + width) * f, (float)(v + height) * f1).endVertex();
        bufferbuilder.pos(x + width, y, 0).tex((float)(u + width) * f, (float)(v) * f1).endVertex();
        bufferbuilder.pos(x, y, 0).tex((float)(u) * f, (float)(v) * f1).endVertex();
        tessellator.draw();
    }

}
