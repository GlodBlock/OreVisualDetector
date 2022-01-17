package glodblock.com.github.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by fnuecke on 1.6.2018.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public class GetItemFromBlock {

    public static ItemStack getItemStackFromState(final IBlockState state, @Nullable final World world) {
        final Block block = state.getBlock();
        try {
            return block.getPickBlock(state, null, world, BlockPos.ORIGIN, null);
        } catch (final Throwable t) {
            try {
                final Item item = Item.getItemFromBlock(block);
                final int damage = block.damageDropped(state);
                return new ItemStack(item, 1, damage);
            } catch (final Throwable ignore) {
            }
        }
        return ItemStack.EMPTY;
    }

}
