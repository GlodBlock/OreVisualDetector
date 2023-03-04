package glodblock.com.github.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class EmptyContainer extends Container {

    @Override
    public boolean canInteractWith(@Nonnull EntityPlayer playerIn) {
        return true;
    }

    @Nonnull
    @Override
    public ItemStack slotClick(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull EntityPlayer player) {
        return ItemStack.EMPTY;
    }

    @Nonnull
    @Override
    public ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, int index)
    {
        return ItemStack.EMPTY;
    }

}
