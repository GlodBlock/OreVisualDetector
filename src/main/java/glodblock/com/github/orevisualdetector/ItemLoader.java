package glodblock.com.github.orevisualdetector;

import glodblock.com.github.items.Scanner;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Objects;

/**
 * Created by GlodBlock on 17.1.2022.
 */
@Mod.EventBusSubscriber(modid = Main.MODID)
public class ItemLoader {

    private static final Scanner Scanner1 = new Scanner("Scanner");

    public static final CreativeTabs myTab = new CreativeTabs("Scanner") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Scanner1);
        }
        @Override
        public boolean hasSearchBar() {
            return false;
        }
    };

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        ModelLoader.setCustomModelResourceLocation(Scanner1, 0, new ModelResourceLocation(Objects.requireNonNull(Scanner1.getRegistryName()), "inventory"));
        Scanner1.initModel();
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        event.getRegistry().register(Scanner1);
        Scanner1.setCreativeTab(myTab);
        Scanner1.setTranslationKey(Main.MODID + ".scanner");
    }
}
