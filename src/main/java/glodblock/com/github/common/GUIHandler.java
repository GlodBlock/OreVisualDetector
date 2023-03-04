package glodblock.com.github.common;

import glodblock.com.github.gui.EmptyContainer;
import glodblock.com.github.gui.ScannerGUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GUIHandler implements IGuiHandler {

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ScannerGUI.GUI_ID) {
            return new EmptyContainer();
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ScannerGUI.GUI_ID) {
            return new ScannerGUI();
        }
        return null;
    }

}
