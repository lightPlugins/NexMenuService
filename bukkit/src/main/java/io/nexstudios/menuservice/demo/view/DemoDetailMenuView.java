package io.nexstudios.menuservice.demo.view;

import io.nexstudios.menuservice.api.MenuKey;
import io.nexstudios.menuservice.core.element.BackElement;
import io.nexstudios.menuservice.core.element.StaticMenuElement;
import io.nexstudios.menuservice.core.view.AbstractMenuView;
import java.util.List;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Detail view opened from the demo list.
 */
public final class DemoDetailMenuView extends AbstractMenuView {

  private static final int ROWS = 3;

  public DemoDetailMenuView(String itemName, int index) {
    super(MenuKey.of("demo-detail-" + index), rowsToSize(ROWS));

    setTitle(Component.text("Demo Detail: " + itemName));

    addElement(4, 1, new StaticMenuElement(createInfoItem(itemName, index)));
    addElement(4, 2, new BackElement());
  }

  private ItemStack createInfoItem(String itemName, int index) {
    ItemStack itemStack = new ItemStack(Material.BOOK);
    ItemMeta meta = itemStack.getItemMeta();
    if (meta != null) {
      meta.displayName(Component.text(itemName));
      meta.lore(List.of(
          Component.text("Selected index: %d".formatted(index)),
          Component.text("Use the back button to return")
      ));
      itemStack.setItemMeta(meta);
    }
    return itemStack;
  }
}

