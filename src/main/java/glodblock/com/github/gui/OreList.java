package glodblock.com.github.gui;

import glodblock.com.github.handlers.HandlerOreData;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.client.GuiScrollingList;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Created by mitchej123 on 24.1.2021.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public class OreList extends GuiScrollingList {

    private final HashMap<String, Integer> ores;
    private final List<String> keys;
    private final GuiScreen parent;
    private final BiConsumer<String, Boolean> onSelected;
    private boolean invert = false;

    private int selected;
    private final int mode;

    @SuppressWarnings("deprecation")
    public OreList(GuiScreen parent, int width, int height, int top, int bottom, int left, int entryHeight, HashMap<String, Integer> aOres, BiConsumer<String, Boolean> onSelected, int mode) {
        super(parent.mc, width, height, top, bottom, left, entryHeight);
        this.parent = parent;
        this.onSelected = onSelected;
        this.ores = aOres;
        this.keys = new ArrayList<>(this.ores.keySet());
        Collections.sort(this.keys);
        if(this.keys.size() > 1) this.keys.add(0, "All");
        this.selected = 0;
        this.mode = mode;
    }

    @Override
    protected int getSize() {
        return keys.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selected = index;
        if (doubleClick) this.invert = !this.invert;
        if(onSelected != null) onSelected.accept(keys.get(index), this.invert);
    }

    @Override
    protected boolean isSelected(int index) {
        return selected == index;
    }

    @Override
    protected void drawBackground() {}

    @Override
    protected void drawSlot(int slotIdx, int entryRight, int slotTop, int slotBuffer, Tessellator tess) {
        String displayString = "Unknown";
        if (Objects.equals(keys.get(slotIdx), "All")) {
            displayString = I18n.format("scanner.gui.all");
        } else {
            if (this.mode == 0) {
                short id = -1;
                if (HandlerOreData.mNameToIDMap.containsKey(keys.get(slotIdx))) {
                    id = HandlerOreData.mNameToIDMap.get(keys.get(slotIdx));
                }
                if (id == -1) {
                    displayString = "Unknown";
                } else {
                    String unName = HandlerOreData.mIDToDisplayNameMap.get(id);
                    if (I18n.hasKey(unName + ".name")) {
                        displayString = I18n.format(unName + ".name");
                    } else if (I18n.hasKey(unName)) {
                        displayString = I18n.format(unName);
                    } else {
                        displayString = "Unknown";
                    }
                }
            } else if (this.mode == 1) {
                if (I18n.hasKey("desc.immersiveengineering.info.mineral." + keys.get(slotIdx))) {
                    displayString = I18n.format("desc.immersiveengineering.info.mineral." + keys.get(slotIdx));
                } else {
                    displayString = keys.get(slotIdx);
                }
            } else if (this.mode == 2) {
                Fluid fluid = FluidRegistry.getFluid(keys.get(slotIdx));
                displayString = fluid.getLocalizedName(new FluidStack(fluid, 1));
            }
        }
        parent.drawString(
                parent.mc.fontRenderer,
                parent.mc.fontRenderer.trimStringToWidth(displayString, listWidth - 10),
                this.left + 3,
                slotTop - 1,
                ores.getOrDefault(keys.get(slotIdx), 0x7d7b76)
        );
    }

}
