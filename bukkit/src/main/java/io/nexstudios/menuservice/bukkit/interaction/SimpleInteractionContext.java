package io.nexstudios.menuservice.bukkit.interaction;

import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.interaction.ClickAction;
import io.nexstudios.menuservice.common.api.interaction.InteractionContext;
import io.nexstudios.menuservice.common.api.interaction.InventoryArea;
import io.nexstudios.menuservice.common.api.item.MenuItem;

import java.util.Objects;
import java.util.Optional;

public record SimpleInteractionContext(
    ViewerRef viewer,
    InventoryArea area,
    ClickAction clickAction,
    int slot,
    Optional<MenuItem> cursorItem,
    Optional<MenuItem> clickedItem,
    Optional<Integer> hotbarButton
) implements InteractionContext {

  public SimpleInteractionContext {
    Objects.requireNonNull(viewer, "viewer must not be null");
    Objects.requireNonNull(area, "area must not be null");
    Objects.requireNonNull(clickAction, "clickAction must not be null");
    Objects.requireNonNull(cursorItem, "cursorItem must not be null");
    Objects.requireNonNull(clickedItem, "clickedItem must not be null");
    Objects.requireNonNull(hotbarButton, "hotbarButton must not be null");
  }
}