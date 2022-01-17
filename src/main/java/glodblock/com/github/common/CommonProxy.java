package glodblock.com.github.common;

import glodblock.com.github.event.EventHandler;
import glodblock.com.github.gui.ScannerGUI;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class CommonProxy implements IGuiHandler {
    public void onLoad() {
    }

    public void onPostLoad() {
        EventHandler.register();
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == ScannerGUI.GUI_ID) {
            return new ScannerGUI();
        }
        return null;
    }


    public void openProspectorGUI() {
        //just Client code
    }


    public void onPreInit() {
    }

    public void sendPlayerException(String s) {
    }
}
