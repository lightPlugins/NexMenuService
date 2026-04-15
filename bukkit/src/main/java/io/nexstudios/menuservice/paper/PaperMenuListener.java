package io.nexstudios.menuservice.paper;

import io.nexstudios.menuservice.api.MenuContext;
import io.nexstudios.menuservice.api.MenuElement;
import io.nexstudios.menuservice.paper.holder.PaperMenuHolder;
import java.util.Map;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

/**
 * Handles all Paper inventory events for active menu sessions.
 */
public final class PaperMenuListener implements Listener {

  private final PaperMenuService menuService;

  public PaperMenuListener(PaperMenuService menuService) {
    this.menuService = Objects.requireNonNull(menuService, "menuService");
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player player)) {
      return;
    }

    if (!(event.getView().getTopInventory().getHolder() instanceof PaperMenuHolder holder)) {
      return;
    }

    event.setCancelled(true);
    PaperMenuSession session = menuService.session(holder.viewerId());
    if (session == null || session.currentMenu() == null) {
      return;
    }

    int rawSlot = event.getRawSlot();
    if (rawSlot < 0 || rawSlot >= event.getView().getTopInventory().getSize()) {
      return;
    }

    MenuContext context = menuService.createContext(player);
    Map<Integer, MenuElement> elements = session.currentMenu().elements(context);
    MenuElement element = elements.get(rawSlot);
    if (element == null || !element.isClickable()) {
      return;
    }

    element.onClick(context, event);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryDrag(InventoryDragEvent event) {
    if (event.getView().getTopInventory().getHolder() instanceof PaperMenuHolder) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onInventoryClose(InventoryCloseEvent event) {
    if (!(event.getPlayer() instanceof Player player)) {
      return;
    }

    if (!(event.getView().getTopInventory().getHolder() instanceof PaperMenuHolder holder)) {
      return;
    }

    PaperMenuSession session = menuService.session(holder.viewerId());
    if (session == null || session.currentMenu() == null) {
      menuService.clearSession(holder.viewerId());
      return;
    }

    if (session.skipNextCloseCleanup()) {
      session.skipNextCloseCleanup(false);
      return;
    }

    session.currentMenu().onClose(menuService.createContext(player));
    menuService.clearSession(holder.viewerId());
  }
}


