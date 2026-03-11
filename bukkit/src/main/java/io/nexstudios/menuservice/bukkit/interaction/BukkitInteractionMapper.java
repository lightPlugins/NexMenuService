package io.nexstudios.menuservice.bukkit.interaction;

import io.nexstudios.menuservice.bukkit.adapter.BukkitItemSnapshotAdapter;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.interaction.ClickAction;
import io.nexstudios.menuservice.common.api.interaction.InteractionContext;
import io.nexstudios.menuservice.common.api.interaction.InventoryArea;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.Objects;
import java.util.Optional;

public final class BukkitInteractionMapper {

  private final BukkitItemSnapshotAdapter snapshotAdapter;

  public BukkitInteractionMapper(BukkitItemSnapshotAdapter snapshotAdapter) {
    this.snapshotAdapter = Objects.requireNonNull(snapshotAdapter, "snapshotAdapter must not be null");
  }

  public InteractionContext fromClickEvent(InventoryClickEvent event, Inventory topInventory, ViewerRef viewer) {
    Objects.requireNonNull(event, "event must not be null");
    Objects.requireNonNull(topInventory, "topInventory must not be null");
    Objects.requireNonNull(viewer, "viewer must not be null");

    InventoryArea area = areaOf(event, topInventory);
    ClickAction clickAction = mapClickAction(event);

    int slot = (area == InventoryArea.OUTSIDE) ? -1 : event.getSlot();

    Optional<MenuItem> cursorItem = snapshotAdapter.toMenuItemSnapshot(event.getCursor());
    Optional<MenuItem> clickedItem = snapshotAdapter.toMenuItemSnapshot(event.getCurrentItem());

    Optional<Integer> hotbarButton = Optional.empty();
    if (clickAction == ClickAction.NUMBER_KEY_SWAP) {
      int btn = event.getHotbarButton(); // 0..8 or -1
      if (btn >= 0 && btn <= 8) hotbarButton = Optional.of(btn + 1);
    }

    return new SimpleInteractionContext(
        viewer,
        area,
        clickAction,
        slot,
        cursorItem,
        clickedItem,
        hotbarButton
    );
  }

  private static InventoryArea areaOf(InventoryClickEvent event, Inventory topInventory) {
    if (event.getClickedInventory() == null) return InventoryArea.OUTSIDE;
    if (event.getClickedInventory().equals(topInventory)) return InventoryArea.TOP;
    return InventoryArea.BOTTOM;
  }

  public static ClickAction mapClickAction(InventoryClickEvent e) {
    return switch (e.getClick()) {
      case LEFT -> ClickAction.LEFT_CLICK;
      case RIGHT -> ClickAction.RIGHT_CLICK;
      case SHIFT_LEFT -> ClickAction.SHIFT_LEFT_CLICK;
      case SHIFT_RIGHT -> ClickAction.SHIFT_RIGHT_CLICK;
      case DOUBLE_CLICK -> ClickAction.DOUBLE_CLICK;
      case NUMBER_KEY -> ClickAction.NUMBER_KEY_SWAP;
      case SWAP_OFFHAND -> ClickAction.OFFHAND_SWAP;
      case DROP -> ClickAction.DROP_KEY;
      case CONTROL_DROP -> ClickAction.CONTROL_DROP_KEY;
      case MIDDLE -> ClickAction.MIDDLE_CLICK;
      default -> ClickAction.UNKNOWN;
    };
  }

  public static ViewerRef viewerOf(InventoryClickEvent event) {
    Player p = (Player) event.getWhoClicked();
    return ViewerRef.of(p.getUniqueId(), p.getName());
  }
}