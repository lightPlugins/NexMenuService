package io.nexstudios.menuservice.paper;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuElement;
import io.nexstudios.menuservice.api.MenuView;
import io.nexstudios.menuservice.paper.holder.PaperMenuHolder;
import java.util.Map;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Renders menu views into Bukkit inventories.
 */
public final class PaperMenuRenderer {

  public PaperMenuRenderer(JavaPlugin plugin) {
    Objects.requireNonNull(plugin, "plugin");
  }

  public Inventory render(MenuContext context, MenuView view, PaperMenuHolder holder) {
    Objects.requireNonNull(context, "context");
    Objects.requireNonNull(view, "view");
    Objects.requireNonNull(holder, "holder");

    Component title = view.title(context);
    Inventory inventory = Bukkit.createInventory(holder, view.size(), title);
    holder.attach(inventory);
    holder.menuKey(view.key());

    for (Map.Entry<Integer, MenuElement> entry : view.elements(context).entrySet()) {
      int slot = entry.getKey();
      MenuElement element = entry.getValue();
      if (slot < 0 || slot >= inventory.getSize() || element == null) {
        continue;
      }
      inventory.setItem(slot, element.render(context));
    }

    return inventory;
  }
}


