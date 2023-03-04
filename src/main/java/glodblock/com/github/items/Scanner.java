package glodblock.com.github.items;

import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import flaxbeard.immersivepetroleum.api.crafting.PumpjackHandler;
import glodblock.com.github.config.ConfigLoader;
import glodblock.com.github.handlers.GetItemFromBlock;
import glodblock.com.github.handlers.HandlerIEVein;
import glodblock.com.github.handlers.HandlerOilVein;
import glodblock.com.github.handlers.HandlerOreData;
import glodblock.com.github.network.ScannerPacket;
import glodblock.com.github.orevisualdetector.Main;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.oredict.OreDictionary;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static glodblock.com.github.network.ScannerPacket.addOre;

/**
 * Created by GlodBlock on 17.1.2022.
 */
public class Scanner extends ItemBase {

    private static final int MODES = 3;

    public Scanner(String name) {
        super(name);
    }

    @Nonnull
    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull final World aWorld, @Nonnull final EntityPlayer aPlayer, @Nonnull final EnumHand hand) {
        if (!aWorld.isRemote) {
            if (aPlayer.isSneaking()) {
                nextMode(aPlayer.getHeldItem(hand));
                aPlayer.sendMessage(new TextComponentTranslation("scanner.mode." + getMode(aPlayer.getHeldItem(hand))));
                return new ActionResult<>(EnumActionResult.SUCCESS, aPlayer.getHeldItem(hand));
            }
            final int cX = ((int) aPlayer.posX) >> 4;
            final int cZ = ((int) aPlayer.posZ) >> 4;
            int size = ConfigLoader.ScannerSize + 1;
            final List<Chunk> chunks = new ArrayList<>();

            for (int i = -size; i <= size; i++)
                for (int j = -size; j <= size; j++)
                    if (i != -size && i != size && j != -size && j != size)
                        chunks.add(aWorld.getChunk(cX + i, cZ + j));
            size = size - 1;
            int scannerMode = getMode(aPlayer.getHeldItem(hand));
            if (scannerMode < 0) {
                return new ActionResult<>(EnumActionResult.FAIL, aPlayer.getHeldItem(hand));
            }

            final ScannerPacket packet = new ScannerPacket(cX, cZ, (int) aPlayer.posX, (int) aPlayer.posZ, size, scannerMode);
            boolean sus = false;
            if (scannerMode == 0) {
                this.runInWorldOreScan(chunks, aWorld, packet);
                sus = true;
            } else if (scannerMode == 1) {
                if (Loader.isModLoaded("immersiveengineering")) {
                    this.runIEVeinScan(chunks, aWorld, packet);
                    sus = true;
                } else {
                    aPlayer.sendMessage(new TextComponentTranslation("scanner.error.ie"));
                }
            } else if (scannerMode == 2) {
                if (Loader.isModLoaded("immersivepetroleum")) {
                    this.runIPReservoirScan(chunks, aWorld, packet);
                    sus = true;
                } else {
                    aPlayer.sendMessage(new TextComponentTranslation("scanner.error.ip"));
                }
            }

            if (sus) {
                Main.proxy.netHandler.sendTo(packet, (EntityPlayerMP) aPlayer);
            } else {
                return new ActionResult<>(EnumActionResult.FAIL, aPlayer.getHeldItem(hand));
            }
        } else {
            if (!aPlayer.isSneaking()) {
                int scannerMode = getMode(aPlayer.getHeldItem(hand));
                if (scannerMode == 0) {
                    final int cX = ((int) aPlayer.posX) >> 4;
                    final int cZ = ((int) aPlayer.posZ) >> 4;
                    int size = ConfigLoader.ScannerSize + 1;
                    final List<Chunk> chunks = new ArrayList<>();
                    for (int i = -size; i <= size; i++)
                        for (int j = -size; j <= size; j++)
                            if (i != -size && i != size && j != -size && j != size)
                                chunks.add(aWorld.getChunk(cX + i, cZ + j));
                    size = size - 1;
                    final ScannerPacket packet = new ScannerPacket(cX, cZ, (int) aPlayer.posX, (int) aPlayer.posZ, size, scannerMode);
                    this.runInWorldOreScan(chunks, aWorld, packet);
                }
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, aPlayer.getHeldItem(hand));
    }

    public int getMode(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof Scanner) {
            NBTTagCompound tag = stack.getTagCompound();
            if (tag != null) {
                if (tag.hasKey("mode")) {
                    return tag.getByte("mode");
                } else {
                    tag.setByte("mode", (byte) 0);
                }
            } else {
                tag = new NBTTagCompound();
                tag.setByte("mode", (byte) 0);
                stack.setTagCompound(tag);
            }
            return 0;
        }
        return -1;
    }

    public void nextMode(ItemStack stack) {
        if (stack != null && stack.getItem() instanceof Scanner) {
            int mode = (getMode(stack) + 1) % MODES;
            NBTTagCompound tag = stack.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
            }
            tag.setByte("mode", (byte) mode);
            stack.setTagCompound(tag);
        }
    }

    private void runInWorldOreScan(List<Chunk> area, World world, ScannerPacket packet) {
        for (Chunk c : area) {
            for (int x = 0; x < 16; x++)
                for (int z = 0; z < 16; z++) {
                    final int ySize = c.getHeightValue(x, z);
                    for (int y = 1; y < ySize; y++) {
                        ItemStack tItem = GetItemFromBlock.getItemStackFromState(c.getBlockState(x, y, z), world);
                        if (tItem == null || tItem.isEmpty()) continue;
                        for (int ID : OreDictionary.getOreIDs(tItem)) {
                            String tOreDictName = OreDictionary.getOreName(ID);
                            if (HandlerOreData.mOreDictMap.containsKey(tOreDictName)) {
                                packet.addBlock(c.getPos().x * 16 + x, y, c.getPos().z * 16 + z, HandlerOreData.mNameToIDMap.get(tOreDictName));
                                addOre(packet, HandlerOreData.mNameToIDMap.get(tOreDictName));
                                HandlerOreData.mIDToDisplayNameMap.put(HandlerOreData.mNameToIDMap.get(tOreDictName), tItem.getTranslationKey());
                            }
                        }
                        String unName = tItem.getTranslationKey();
                        if (HandlerOreData.mUnlocalizedMap.containsKey(unName)) {
                            packet.addBlock(c.getPos().x * 16 + x, y, c.getPos().z * 16 + z, HandlerOreData.mNameToIDMap.get(unName));
                            addOre(packet, HandlerOreData.mNameToIDMap.get(unName));
                            HandlerOreData.mIDToDisplayNameMap.put(HandlerOreData.mNameToIDMap.get(unName), tItem.getTranslationKey());
                        }
                    }
                }
        }
    }

    private void runIEVeinScan(List<Chunk> area, World world, ScannerPacket packet) {
        for (Chunk c : area) {
            ExcavatorHandler.MineralMix vein = ExcavatorHandler.getRandomMineral(world, c.x, c.z);
            if (vein != null && HandlerIEVein.veinToIDMap.containsKey(vein.name)) {
                for (int x = 1; x < 16; x++)
                    for (int z = 1; z < 16; z++) {
                        packet.addBlock(c.getPos().x * 16 + x, 255, c.getPos().z * 16 + z, HandlerIEVein.veinToIDMap.get(vein.name));
                        addOre(packet, HandlerIEVein.veinToIDMap.get(vein.name));
                    }
            }
        }
    }

    private void runIPReservoirScan(List<Chunk> area, World world, ScannerPacket packet) {
        for (Chunk c : area) {
            PumpjackHandler.OilWorldInfo veinInfo = PumpjackHandler.getOilWorldInfo(world, c.x, c.z);
            PumpjackHandler.ReservoirType vein = veinInfo.getType();
            if (vein != null) {
                for (int x = 1; x < 16; x++)
                    for (int z = 1; z < 16; z++) {
                        packet.addBlock(c.getPos().x * 16 + x, 0, c.getPos().z * 16 + z, (short) HandlerOilVein.getFluidID(vein.getFluid()));
                        packet.addBlock(c.getPos().x * 16 + x, 1, c.getPos().z * 16 + z, (short) (veinInfo.current >> 16));
                        packet.addBlock(c.getPos().x * 16 + x, 2, c.getPos().z * 16 + z, (short) (veinInfo.current & 0xFFFF));
                        packet.addBlock(c.getPos().x * 16 + x, 3, c.getPos().z * 16 + z, (short) (vein.replenishRate >> 16));
                        packet.addBlock(c.getPos().x * 16 + x, 4, c.getPos().z * 16 + z, (short) (vein.replenishRate & 0xFFFF));
                        addOre(packet, (short) HandlerOilVein.getFluidID(vein.getFluid()));
                    }
            }
        }
    }
}
