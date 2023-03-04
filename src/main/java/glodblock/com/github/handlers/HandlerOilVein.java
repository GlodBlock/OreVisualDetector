package glodblock.com.github.handlers;

import com.google.common.collect.BiMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.HashMap;

public class HandlerOilVein {

    public static HashMap<String, short[]> fluidMap = new HashMap<>();

    public static void init() {
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            if (fluid != null) {
                int color = fluid.getColor();
                if (color == 0xFFFFFFFF) {
                    fluidMap.put(fluid.getName(), getColour(fluid));
                } else {
                    fluidMap.put(fluid.getName(),new short[]{
                            (short) ((color >> 16) & 0xFF),
                            (short) ((color >> 8) & 0xFF),
                            (short) (color & 0xFF)});
                }
            }
        }
    }

    public static int getFluidID(Fluid fluid) {
        return FluidRegistry.getRegisteredFluidIDs().get(fluid);
    }

    public static Fluid getFluid(int id) {
        return ((BiMap<Fluid, Integer>) FluidRegistry.getRegisteredFluidIDs()).inverse().get(id);
    }

    public static int getFluidID(String name) {
        Fluid fluid = FluidRegistry.getFluid(name);
        return getFluidID(fluid);
    }

    private static short[] getColour(Fluid fluid) {
        short[] rgb = new short[3];
        try {
            TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks()
                    .getTextureExtry(fluid.getStill().toString());
            if (sprite != null && sprite.getFrameCount() > 0) {
                int[][] image = sprite.getFrameTextureData(0);
                int r = 0, g = 0, b = 0, count = 0;
                for (int[] row : image) {
                    for (int pixel : row) {
                        if (((pixel >> 24) & 0xFF) > 127) {
                            r += (pixel >> 16) & 0xFF;
                            g += (pixel >> 8) & 0xFF;
                            b += pixel & 0xFF;
                            ++count;
                        }
                    }
                }
                if (count > 0) {
                    rgb[0] = (short) (r / count);
                    rgb[1] = (short) (g / count);
                    rgb[2] = (short) (b / count);
                }
            }
        } catch (Throwable t) {
            return rgb;
        }
        return rgb;
    }

}
