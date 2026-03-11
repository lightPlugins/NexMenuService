package io.nexstudios.menuservice.bukkit.interaction;

import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.interaction.ClickAction;
import io.nexstudios.menuservice.common.api.interaction.DragAction;
import io.nexstudios.menuservice.common.api.interaction.InteractionContext;
import io.nexstudios.menuservice.common.api.interaction.InventoryArea;
import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record SimpleInteractionContext(
    ViewerRef viewer,
    InventoryArea area,
    ClickAction clickAction,
    int slot,
    Optional<MenuItem> cursorItem,
    Optional<MenuItem> clickedItem,
    Optional<Integer> hotbarButton,
    DragAction dragAction,
    Optional<Set<Integer>> draggedSlots
) implements InteractionContext {

  public SimpleInteractionContext {
    Objects.requireNonNull(viewer, "viewer must not be null");
    Objects.requireNonNull(area, "area must not be null");
    Objects.requireNonNull(clickAction, "clickAction must not be null");
    Objects.requireNonNull(cursorItem, "cursorItem must not be null");
    Objects.requireNonNull(clickedItem, "clickedItem must not be null");
    Objects.requireNonNull(hotbarButton, "hotbarButton must not be null");
    Objects.requireNonNull(dragAction, "dragAction must not be null");
    Objects.requireNonNull(draggedSlots, "draggedSlots must not be null");
  }

  // Convenience factory for click interactions (non-drag)
  public static SimpleInteractionContext click(
      ViewerRef viewer,
      InventoryArea area,
      ClickAction clickAction,
      int slot,
      Optional<MenuItem> cursorItem,
      Optional<MenuItem> clickedItem,
      Optional<Integer> hotbarButton
  ) {
    return new SimpleInteractionContext(
        viewer,
        area,
        clickAction,
        slot,
        cursorItem,
        clickedItem,
        hotbarButton,
        DragAction.UNKNOWN,
        Optional.empty()
    );
  }

  // Convenience factory for drag interactions
  public static SimpleInteractionContext drag(
      ViewerRef viewer,
      InventoryArea area,
      DragAction dragAction,
      Optional<MenuItem> cursorItem,
      Set<Integer> draggedSlots
  ) {
    return new SimpleInteractionContext(
        viewer,
        area,
        ClickAction.UNKNOWN,
        -1,
        cursorItem,
        Optional.empty(),
        Optional.empty(),
        dragAction,
        Optional.of(Set.copyOf(draggedSlots))
    );
  }
}