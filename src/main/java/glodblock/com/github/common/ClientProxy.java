package glodblock.com.github.common;

import glodblock.com.github.gui.ScannerGUI;
import glodblock.com.github.orevisualdetector.Main;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ClientProxy extends CommonProxy {

    public ClientProxy() {

    }

    @Override
    public void onPostLoad() {
        super.onPostLoad();
    }

    @Override
    public void onLoad() {
        super.onLoad();
    }

    public void openProspectorGUI() {
        EntityPlayer player = Minecraft.getMinecraft().player;
        player.openGui(Main.instance, ScannerGUI.GUI_ID, player.world, (int)player.posX, (int)player.posY, (int)player.posZ);
    }
    @Override
    public void onPreInit() {
        super.onPreInit();
    }

    @Override
    public void sendPlayerException(String s) {
        Minecraft.getMinecraft().player.sendChatMessage("Ore Visual Detector: " + s);
    }

}
