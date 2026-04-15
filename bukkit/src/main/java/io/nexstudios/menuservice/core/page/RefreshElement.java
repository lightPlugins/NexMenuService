package io.nexstudios.menuservice.core.page;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuElement;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Button that refreshes the currently open menu.
 */
public final class RefreshElement implements MenuElement {

  @Override
  public ItemStack render(MenuContext context) {
    return createItemStack();
  }

  @Override
  public void onClick(MenuContext context, InventoryClickEvent event) {
    context.menuService().refresh(context.viewer());
  }

  private static ItemStack createItemStack() {
    ItemStack itemStack = new ItemStack(Material.CLOCK);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Refresh"));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }
}

