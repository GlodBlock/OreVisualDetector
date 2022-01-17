package glodblock.com.github.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.relauncher.FMLInjectionData;

import java.io.File;

/**
 * Created by GlodBlock on 17.1.2022.
 */
public class ConfigLoader {

    public static final Configuration ScannerConfig = new Configuration(new File(new File(new File((File) FMLInjectionData.data()[6], "config"), "OreVisualDetector"), "Scanner.cfg"));

    public static int ScannerSize = 5;

    public static void run() {
        loadCategory();
        loadProperty();
    }

    private static void loadCategory() {
        ScannerConfig.addCustomCategoryComment("Scanner", "Set the Property of Scanner.");
    }

    private static void loadProperty() {
        ScannerSize = ScannerConfig.getInt( "Scan Radius", "Scanner", ScannerSize, 4, 12, "The radius of scan area. Think twice before you try to set it > 7 if you have a potato PC. It may cause huge lag or make you lost connection in sever.");
        if (ScannerConfig.hasChanged())
            ScannerConfig.save();
    }

}
