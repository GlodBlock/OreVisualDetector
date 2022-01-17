package glodblock.com.github.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;

/**
 * Created by wital_000 on 18.04.2016.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public class EventHandler {

    static boolean inited = false;

    public static void register() {
        if (!inited) {
            inited = true;
            EventHandler handler = new EventHandler();
            MinecraftForge.EVENT_BUS.register(handler);
            FMLCommonHandler.instance().bus().register(handler);
        }
    }

}
