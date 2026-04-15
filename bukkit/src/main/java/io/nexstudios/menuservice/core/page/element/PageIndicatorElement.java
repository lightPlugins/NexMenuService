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
 * Displays the current page number of a paged menu.
 */
public final class PageIndicatorElement implements MenuElement {

  @Override
  public ItemStack render(MenuContext context) {
    return context.menuService().currentView(context.viewer().getUniqueId())
        .filter(PagedMenuView.class::isInstance)
        .map(PagedMenuView.class::cast)
        .map(this::createItemStack)
        .orElseGet(PageIndicatorElement::createFallbackItemStack);
  }

  @Override
  public void onClick(MenuContext context, InventoryClickEvent event) {
    context.menuService().refresh(context.viewer());
  }

  private ItemStack createItemStack(PagedMenuView<?> view) {
    ItemStack itemStack = new ItemStack(Material.PAPER);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Page %d/%d".formatted(view.page() + 1, view.pageCount())));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }

  private static ItemStack createFallbackItemStack() {
    ItemStack itemStack = new ItemStack(Material.PAPER);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Page 1/1"));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }
}



