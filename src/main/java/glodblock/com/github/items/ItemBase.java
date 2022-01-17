package glodblock.com.github.items;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Objects;

import static glodblock.com.github.orevisualdetector.ItemLoader.myTab;

/**
 * Created by GlodBlock on 17.1.2022.
 */
public class ItemBase extends Item{

    public ItemBase(String name) {
        super();
        setRegistryName(name);
        setTranslationKey(Objects.requireNonNull(getRegistryName()).toString());
        setCreativeTab(myTab);
    }

    @SideOnly(Side.CLIENT)
    public void initModel(){
        ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory"));
    }

}
