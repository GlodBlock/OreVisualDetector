package glodblock.com.github.common;

import glodblock.com.github.handlers.HandlerIEVein;
import glodblock.com.github.handlers.HandlerOilVein;
import glodblock.com.github.network.GuiOpenPacket;
import glodblock.com.github.network.ScannerPacket;
import glodblock.com.github.orevisualdetector.Main;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonProxy {

    public final SimpleNetworkWrapper netHandler = NetworkRegistry.INSTANCE.newSimpleChannel(Main.MODID);

    public void onLoad() {
    }

    public void onPostLoad() {
        NetworkRegistry.INSTANCE.registerGuiHandler(Main.instance, new GUIHandler());
    }


    public void onPreInit() {
        this.netHandler.registerMessage(new ScannerPacket.Handler(), ScannerPacket.class, 0, Side.CLIENT);
        this.netHandler.registerMessage(new GuiOpenPacket.Handler(), GuiOpenPacket.class, 1, Side.SERVER);
    }

    public void onFinish() {
        if (Loader.isModLoaded("immersiveengineering")) {
            HandlerIEVein.init();
        }
        if (Loader.isModLoaded("immersivepetroleum")) {
            HandlerOilVein.init();
        }
    }

}
