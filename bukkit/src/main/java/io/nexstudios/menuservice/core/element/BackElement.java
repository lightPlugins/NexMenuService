package io.nexstudios.menuservice.core.element;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuService;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Default navigation element that returns the viewer to the previous menu.
 */
public class BackElement extends StaticMenuElement {

  private static ItemStack createItemStack() {
    ItemStack itemStack = new ItemStack(Material.ARROW);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text("Back"));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }

  public BackElement() {
    super(createItemStack(), BackElement::navigateBack);
  }

  private static void navigateBack(MenuContext context, InventoryClickEvent event) {
    MenuService menuService = context.menuService();
    menuService.back(context.viewer());
  }
}


