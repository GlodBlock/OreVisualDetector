package glodblock.com.github.orevisualdetector;

import glodblock.com.github.common.CommonProxy;
import glodblock.com.github.config.ConfigLoader;
import glodblock.com.github.handlers.HandlerOreData;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;

@Mod(
        modid = Main.MODID,
        name = Main.NAME,
        version = Main.VERSION,
        acceptedMinecraftVersions = "1.12.2",
        dependencies = "after:immersiveengineering;after:immersivepetroleum"
)
public class Main {
    public static final String MODID="orevisualdetector";
    public static final String NAME="Ore Visual Detector";
    public static final String VERSION="2.0.1";
    @SidedProxy(clientSide = "glodblock.com.github.common.ClientProxy", serverSide = "glodblock.com.github.common.CommonProxy")
    public static CommonProxy proxy;
    @Instance(Main.MODID)
    public static Main instance;
    public static final org.apache.logging.log4j.Logger Logger = LogManager.getLogger("Scanner");

    public Main() {

    }

    @EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        ConfigLoader.run();
        HandlerOreData.run();
        proxy.onPreInit();
    }
    @EventHandler
    public static void init(FMLInitializationEvent event) {
        proxy.onLoad();
    }

    @EventHandler
    public static void postInit(FMLPostInitializationEvent event){
        proxy.onPostLoad();
    }

    @EventHandler
    public static void postInit(FMLLoadCompleteEvent event){
        proxy.onFinish();
    }

}
