package io.nexstudios.menuservice.core.page.element;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuElement;
import io.nexstudios.menuservice.core.page.PagedMenuView;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Button that moves the active paged menu to the previous page.
 */
public final class PreviousPageElement implements MenuElement {

  @Override
  public ItemStack render(MenuContext context) {
    return createItemStack();
  }

  @Override
  public void onClick(MenuContext context, InventoryClickEvent event) {
    context.menuService().currentView(context.viewer().getUniqueId())
        .filter(PagedMenuView.class::isInstance)
        .map(PagedMenuView.class::cast)
        .ifPresent(view -> {
          view.previousPage();
          context.menuService().refresh(context.viewer());
        });
  }

  private static ItemStack createItemStack() {
    ItemStack itemStack = new ItemStack(Material.ARROW);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Previous Page"));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }
}


