package io.nexstudios.menuservice.bukkit.render;

import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuPopulateContext;
import io.nexstudios.menuservice.common.api.MenuSlot;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;
import io.nexstudios.menuservice.common.api.render.RenderPlan;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class RenderPopulateContext implements MenuPopulateContext {

  private final MenuKey key;
  private final ViewerRef viewer;
  private final int size;
  private final long renderToken;

  private final Map<Integer, MenuItemSupplier> items = new HashMap<>();
  private final Set<Integer> cleared = new HashSet<>();
  private final Map<Integer, MenuSlot.MenuClickHandler> clickHandlers = new HashMap<>();
  private final List<PlannedHeadUpdate> plannedHeads = new ArrayList<>();

  public RenderPopulateContext(MenuKey key, ViewerRef viewer, int size, long renderToken) {
    this.key = Objects.requireNonNull(key, "key must not be null");
    this.viewer = Objects.requireNonNull(viewer, "viewer must not be null");
    if (size < 1) throw new IllegalArgumentException("size must be >= 1");
    this.size = size;
    this.renderToken = renderToken;
  }

  @Override
  public MenuKey key() {
    return key;
  }

  @Override
  public ViewerRef viewer() {
    return viewer;
  }

  @Override
  public MenuSlot slot(int slot) {
    if (slot < 0 || slot >= size) {
      throw new IllegalArgumentException("slot must be within inventory bounds: 0.." + (size - 1));
    }

    return new MenuSlot() {
      @Override public int index() { return slot; }

      @Override
      public void setItem(MenuItem item) {
        if (!Bukkit.isPrimaryThread()) {
          throw new IllegalStateException(
              "MenuSlot.setItem(...) is not allowed during async populate. " +
                  "Use slot.setPlannedItem(() -> MenuItem.of(new ItemStack(...))) instead."
          );
        }
        MenuSlot.requireNonNullItem(item);
        items.put(slot, () -> item);
        cleared.remove(slot);
      }

      @Override
      public void setPlannedItem(MenuItemSupplier supplier) {
        MenuSlot.requireNonNullPlannedItem(supplier);
        items.put(slot, supplier);
        cleared.remove(slot);
      }

      @Override
      public void setPlannedHead(CompletableFuture<ItemStack> headFuture) {
        setPlannedHead(MenuItem.of(new ItemStack(Material.PLAYER_HEAD, 1)), headFuture);
      }

      @Override
      public void setPlannedHead(MenuItem placeholder, CompletableFuture<ItemStack> headFuture) {
        MenuSlot.requireNonNullItem(placeholder);
        Objects.requireNonNull(headFuture, "headFuture must not be null");

        setPlannedItem(() -> placeholder);

        try {
          ItemStack resolved = headFuture.getNow(null);
          if (resolved != null) {
            setPlannedItem(() -> mergePlaceholderMeta(placeholder, resolved));
            return;
          }
        } catch (CompletionException ignored) {
          // Fall through to the default placeholder head.
        }

        plannedHeads.add(new PlannedHeadUpdate(slot, placeholder, headFuture, renderToken));
      }

      @Override
      public void clear() {
        items.remove(slot);
        cleared.add(slot);
      }

      @Override
      public void onClick(MenuClickHandler handler) {
        clickHandlers.put(slot, Objects.requireNonNull(handler, "handler must not be null"));
      }
    };
  }

  @Override
  public int size() {
    return size;
  }

  public RenderPlan toRenderPlan() {
    return new RenderPlan(Map.copyOf(items), Set.copyOf(cleared));
  }

  public Map<Integer, MenuSlot.MenuClickHandler> clickHandlers() {
    return Map.copyOf(clickHandlers);
  }

  public List<PlannedHeadUpdate> plannedHeads() {
    return List.copyOf(plannedHeads);
  }

  private static MenuItem mergePlaceholderMeta(MenuItem placeholder, ItemStack resolvedStack) {
    ItemStack merged = resolvedStack.clone();
    ItemMeta placeholderMeta = placeholder.stack().getItemMeta();
    ItemMeta mergedMeta = merged.getItemMeta();

    if (placeholderMeta != null && mergedMeta != null) {
      if (placeholderMeta.hasItemName()) mergedMeta.itemName(placeholderMeta.itemName());
      if (placeholderMeta.hasDisplayName()) mergedMeta.displayName(placeholderMeta.displayName());
      if (placeholderMeta.hasLore()) mergedMeta.lore(placeholderMeta.lore());
      merged.setItemMeta(mergedMeta);
    }

    return MenuItem.of(merged);
  }

  public record PlannedHeadUpdate(int slot, MenuItem placeholder, CompletableFuture<ItemStack> future, long renderToken) {
    public PlannedHeadUpdate {
      Objects.requireNonNull(placeholder, "placeholder must not be null");
      Objects.requireNonNull(future, "future must not be null");
    }
  }
}