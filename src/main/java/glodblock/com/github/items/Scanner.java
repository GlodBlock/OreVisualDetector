package glodblock.com.github.items;

import glodblock.com.github.config.ConfigLoader;
import glodblock.com.github.gui.ScannerGUI;
import glodblock.com.github.gui.ScannerGUITexture;
import glodblock.com.github.handlers.GetItemFromBlock;
import glodblock.com.github.handlers.HandleOreData;
import glodblock.com.github.network.ScannerPacket;
import glodblock.com.github.orevisualdetector.Main;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static glodblock.com.github.network.ScannerPacket.addOre;

/**
 * Created by GlodBlock on 17.1.2022.
 */
public class Scanner extends ItemBase {

    public Scanner(String name) {
        super(name);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(final World aWorld, @Nonnull final EntityPlayer aPlayer, @Nonnull final EnumHand hand) {
        if (aWorld.isRemote) {
            final int cX = ((int) aPlayer.posX) >> 4;
            final int cZ = ((int) aPlayer.posZ) >> 4;
            int size = ConfigLoader.ScannerSize + 1;
            final List<Chunk> chunks = new ArrayList<>();

            for (int i = -size; i <= size; i++)
                for (int j = -size; j <= size; j++)
                    if (i != -size && i != size && j != -size && j != size)
                        chunks.add(aWorld.getChunk(cX + i, cZ + j));
            size = size - 1;

            final ScannerPacket packet = new ScannerPacket(cX, cZ, (int) aPlayer.posX, (int) aPlayer.posZ, size, 1);

            for (Chunk c : chunks) {
                for (int x = 0; x < 16; x++)
                    for (int z = 0; z < 16; z++) {
                        final int ySize = c.getHeightValue(x, z);
                        for (int y = 1; y < ySize; y++) {
                            ItemStack tItem = GetItemFromBlock.getItemStackFromState(c.getBlockState(x, y, z), aWorld);
                            if (tItem == null || tItem.isEmpty()) continue;
                            for (int ID : OreDictionary.getOreIDs(tItem)) {
                                String tOreDictName = OreDictionary.getOreName(ID);
                                if (HandleOreData.mOreDictMap.containsKey(tOreDictName)) {
                                    packet.addBlock(c.getPos().x * 16 + x, y, c.getPos().z * 16 + z, HandleOreData.mNameToIDMap.get(tOreDictName));
                                    addOre(packet, HandleOreData.mNameToIDMap.get(tOreDictName));
                                    HandleOreData.mIDToDisplayNameMap.put(HandleOreData.mNameToIDMap.get(tOreDictName), tItem.getDisplayName());
                                }
                            }
                            String unName = tItem.getTranslationKey();
                            if (HandleOreData.mUnlocalizedMap.containsKey(unName)) {
                                packet.addBlock(c.getPos().x * 16 + x, y, c.getPos().z * 16 + z, HandleOreData.mNameToIDMap.get(unName));
                                addOre(packet, HandleOreData.mNameToIDMap.get(unName));
                                HandleOreData.mIDToDisplayNameMap.put(HandleOreData.mNameToIDMap.get(unName), tItem.getDisplayName());
                            }
                        }
                    }
            }
            ScannerGUI.newMap(new ScannerGUITexture(packet));
            Main.proxy.openProspectorGUI();
            return new ActionResult<>(EnumActionResult.SUCCESS, aPlayer.getHeldItem(hand));
        }
        return new ActionResult<>(EnumActionResult.FAIL, aPlayer.getHeldItem(hand));
    }
}
