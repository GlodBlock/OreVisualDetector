package glodblock.com.github.handlers;

import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * Created by fnuecke on 1.6.2018.<p>
 * Modified by GlodBlock on 17.1.2022.
 */
public class GetItemFromBlock {

    public static ItemStack getItemStackFromState(final IBlockState state, @Nullable final World world) {
        try {
            return new ItemStack(state.getBlock(), 1, state.getBlock().getMetaFromState(state));
        } catch (final Throwable t) {
            return ItemStack.EMPTY;
        }
    }

}
