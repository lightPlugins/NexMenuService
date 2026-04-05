package io.nexstudios.menuservice.bukkit.listener;

import io.nexstudios.menuservice.bukkit.adapter.BukkitItemSnapshotAdapter;
import io.nexstudios.menuservice.bukkit.interaction.BukkitInteractionMapper;
import io.nexstudios.menuservice.bukkit.interaction.SimpleInteractionContext;
import io.nexstudios.menuservice.bukkit.inventory.PaperMenuHolder;
import io.nexstudios.menuservice.bukkit.service.menu.BukkitMenuService;
import io.nexstudios.menuservice.bukkit.service.menu.BukkitMenuView;
import io.nexstudios.menuservice.bukkit.service.menu.ClickHandlerStore;
import io.nexstudios.menuservice.common.api.CloseReason;
import io.nexstudios.menuservice.common.api.MenuSlot;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.deposit.DepositHandler;
import io.nexstudios.menuservice.common.api.deposit.DepositPolicy;
import io.nexstudios.menuservice.common.api.interaction.ClickAction;
import io.nexstudios.menuservice.common.api.interaction.DragAction;
import io.nexstudios.menuservice.common.api.interaction.InventoryArea;
import io.nexstudios.menuservice.common.api.interaction.InteractionContext;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MenuInventoryListeners implements Listener {

  private final BukkitMenuService service;

  private final BukkitItemSnapshotAdapter snapshotAdapter = new BukkitItemSnapshotAdapter();
  private final BukkitInteractionMapper interactionMapper = new BukkitInteractionMapper(snapshotAdapter);

  public MenuInventoryListeners(BukkitMenuService service) {
    this.service = Objects.requireNonNull(service, "service must not be null");
  }

  @EventHandler
  public void onClick(InventoryClickEvent event) {
    ResolvedMenuContext resolved = resolveActiveMenuContext(event.getView(), event, event.getWhoClicked());
    if (resolved == null) return;

    Inventory top = resolved.top();
    BukkitMenuView view = resolved.view();

    ViewerRef viewer = BukkitInteractionMapper.viewerOf(event);
    var interaction = interactionMapper.fromClickEvent(event, top, viewer);

    boolean clickedTop = event.getClickedInventory() != null && event.getClickedInventory().equals(top);

    Optional<DepositPolicy> depositPolicyOpt = view.definition().interactionPolicy().depositPolicy();
    Optional<DepositHandler> depositHandlerOpt = view.definition().depositHandler();

    // --- Bottom / outside click routing ---
    if (!clickedTop) {
      // Shift-click deposits: bottom -> top into allowed slots (stacking + partial)
      if ((interaction.clickAction() == ClickAction.SHIFT_LEFT_CLICK || interaction.clickAction() == ClickAction.SHIFT_RIGHT_CLICK)
          && depositPolicyOpt.isPresent()
          && depositHandlerOpt.isPresent()
          && interaction.clickedItem().isPresent()
          && interaction.area() == InventoryArea.BOTTOM) {

        event.setCancelled(true);

        DepositPolicy depositPolicy = depositPolicyOpt.get();
        DepositHandler depositHandler = depositHandlerOpt.get();

        ItemStack source = event.getCurrentItem();
        if (isEmpty(source)) return;

        // Try stacking first, then empty slots
        int remaining = source.getAmount();

        // 1) stack into similar existing deposits
        for (int slot : depositPolicy.allowedTopSlots()) {
          if (remaining <= 0) break;
          if (inBounds(top, slot)) {
            ItemStack target = top.getItem(slot);
            if (!isEmpty(target) && canStack(target, source)) {
              int moved = tryDepositIntoSlot(view, depositHandler, depositPolicy, top, viewer, slot, source, remaining, interaction);
              remaining -= moved;
            }
          }
        }

        // 2) fill empty slots
        for (int slot : depositPolicy.allowedTopSlots()) {
          if (remaining <= 0) break;
          if (inBounds(top, slot)) {
            ItemStack target = top.getItem(slot);
            if (isEmpty(target)) {
              int moved = tryDepositIntoSlot(view, depositHandler, depositPolicy, top, viewer, slot, source, remaining, interaction);
              remaining -= moved;
            }
          }
        }

        // Update source stack amount (partial)
        if (remaining <= 0) {
          event.setCurrentItem(new ItemStack(Material.AIR));
        } else {
          ItemStack newSource = source.clone();
          newSource.setAmount(remaining);
          event.setCurrentItem(newSource);
        }

        return;
      }

      // Default bottom policy behavior + optional hook reporting (already present)
      var policy = view.definition().interactionPolicy();

      if (policy.lockBottomInventoryByDefault()) {
        event.setCancelled(true);
      }

      if (policy.notifyBottomInventoryClicks()) {
        var hooksOpt = view.definition().interactionHooks();
        hooksOpt.ifPresent(hooks -> interaction.clickedItem().ifPresent(clicked -> hooks.onBottomInventoryClick(viewer, clicked, interaction.clickAction())));
      }

      return;
    }

    // --- Top click path ---
    event.setCancelled(true);

    int slot = event.getSlot();

    // Anti-leak actions inside menus
    if (interaction.clickAction() == ClickAction.DOUBLE_CLICK
        || interaction.clickAction() == ClickAction.DROP_KEY
        || interaction.clickAction() == ClickAction.CONTROL_DROP_KEY) {
      return;
    }

    if (depositPolicyOpt.isPresent() && depositHandlerOpt.isPresent()) {
      DepositPolicy depositPolicy = depositPolicyOpt.get();
      DepositHandler depositHandler = depositHandlerOpt.get();

      if (depositPolicy.isSlotAllowed(slot)) {

        // 1) Cursor place/remove (LEFT/RIGHT) with stacking + partial transfer
        if (interaction.clickAction() == ClickAction.LEFT_CLICK || interaction.clickAction() == ClickAction.RIGHT_CLICK) {
          ItemStack cursor = event.getCursor();
          ItemStack current = event.getCurrentItem();

          boolean cursorEmpty = isEmpty(cursor);
          boolean currentEmpty = isEmpty(current);

          // Place: cursor -> slot
          if (!cursorEmpty) {
            int moved = tryDepositIntoSlot(view, depositHandler, depositPolicy, top, viewer, slot, cursor, cursor.getAmount(), interaction);
            if (moved <= 0) return;

            int remaining = cursor.getAmount() - moved;
            if (remaining <= 0) {
              setViewCursor(event.getView(), new ItemStack(Material.AIR));
            } else {
              ItemStack newCursor = cursor.clone();
              newCursor.setAmount(remaining);
              setViewCursor(event.getView(), newCursor);
            }
            return;
          }

          // Remove: slot -> cursor (allow stacking into cursor)
          if (!currentEmpty) {
            if (tryRemoveFromSlotToCursor(view, depositHandler, depositPolicy, viewer, slot, current, interaction, event)) return;
          }

          return;
        }

        // 2) Number key swap (full swap + partial/stacking rules)
        if (interaction.clickAction() == ClickAction.NUMBER_KEY_SWAP) {
          int btn = event.getHotbarButton(); // 0..8
          if (btn < 0 || btn > 8) return;

          ItemStack hotbarItem = event.getWhoClicked().getInventory().getItem(btn);
          ItemStack current = event.getCurrentItem();

          boolean hotbarEmpty = isEmpty(hotbarItem);
          boolean currentEmpty = isEmpty(current);

          if (!hotbarEmpty && currentEmpty) {
            // Place hotbar -> empty slot
            int moved = tryDepositIntoSlot(view, depositHandler, depositPolicy, top, viewer, slot, hotbarItem, hotbarItem.getAmount(), interaction);
            if (moved <= 0) return;

            int remaining = hotbarItem.getAmount() - moved;
            if (remaining <= 0) {
              event.getWhoClicked().getInventory().setItem(btn, new ItemStack(Material.AIR));
            } else {
              ItemStack newHotbar = hotbarItem.clone();
              newHotbar.setAmount(remaining);
              event.getWhoClicked().getInventory().setItem(btn, newHotbar);
            }
            return;
          }

          if (hotbarEmpty && !currentEmpty) {
            // Remove slot -> empty hotbar
            tryRemoveFromSlotToHotbar(view, depositHandler, depositPolicy, viewer, slot, current, interaction, btn, event);
            return;
          }

          if (!hotbarEmpty) {
            if (trySwapSlotWithTargetStack(
                view,
                depositHandler,
                viewer,
                slot,
                current,
                interaction,
                hotbarItem,
                merged -> event.getWhoClicked().getInventory().setItem(btn, merged)
            )) {
              return;
            }
            return;
          }

          // both empty -> nothing
          return;
        }

        // 3) Offhand swap (same rules as hotbar)
        if (interaction.clickAction() == ClickAction.OFFHAND_SWAP) {
          ItemStack offhand = event.getWhoClicked().getInventory().getItemInOffHand();
          ItemStack current = event.getCurrentItem();

          boolean offhandEmpty = isEmpty(offhand);
          boolean currentEmpty = isEmpty(current);

          if (!offhandEmpty && currentEmpty) {
            // Place offhand -> empty slot
            int moved = tryDepositIntoSlot(view, depositHandler, depositPolicy, top, viewer, slot, offhand, offhand.getAmount(), interaction);
            if (moved <= 0) return;

            int remaining = offhand.getAmount() - moved;
            if (remaining <= 0) {
              event.getWhoClicked().getInventory().setItemInOffHand(new ItemStack(Material.AIR));
            } else {
              ItemStack newOffhand = offhand.clone();
              newOffhand.setAmount(remaining);
              event.getWhoClicked().getInventory().setItemInOffHand(newOffhand);
            }
            return;
          }

          if (offhandEmpty && !currentEmpty) {
            // Remove slot -> empty offhand
            tryRemoveFromSlotToOffhand(view, depositHandler, depositPolicy, viewer, slot, current, interaction, event);
            return;
          }

          if (!offhandEmpty) {
            if (trySwapSlotWithTargetStack(
                view,
                depositHandler,
                viewer,
                slot,
                current,
                interaction,
                offhand,
                merged -> event.getWhoClicked().getInventory().setItemInOffHand(merged)
            )) {
              return;
            }
            return;
          }

          return;
        }
      }
    }

    // Fallback: regular top slot handler
    var clickHandler = ClickHandlerStore.find(top, slot);
    if (clickHandler == null) return;

    clickHandler.handle(new SimpleClickContext(view, viewer, slot, BukkitInteractionMapper.mapClickAction(event)));
  }

  @EventHandler
  public void onDrag(InventoryDragEvent event) {
    ResolvedMenuContext resolved = resolveActiveMenuContext(event.getView(), event, event.getWhoClicked());
    if (resolved == null) return;

    Inventory top = resolved.top();
    BukkitMenuView view = resolved.view();

    int topSize = top.getSize();
    boolean affectsTop = event.getRawSlots().stream().anyMatch(rawSlot -> rawSlot >= 0 && rawSlot < topSize);

    if (!affectsTop) {
      var policy = view.definition().interactionPolicy();
      if (policy.lockBottomInventoryByDefault()) {
        event.setCancelled(true);
      }
      return;
    }

    Optional<DepositPolicy> depositPolicyOpt = view.definition().interactionPolicy().depositPolicy();
    Optional<DepositHandler> depositHandlerOpt = view.definition().depositHandler();

    if (depositPolicyOpt.isEmpty() || depositHandlerOpt.isEmpty()) {
      event.setCancelled(true);
      return;
    }

    boolean applied = tryApplyDragDeposit(event, view, top, depositPolicyOpt.get(), depositHandlerOpt.get());
    if (!applied) {
      event.setCancelled(true);
    }
  }

  private boolean tryApplyDragDeposit(
      InventoryDragEvent event,
      BukkitMenuView view,
      Inventory top,
      DepositPolicy depositPolicy,
      DepositHandler depositHandler
  ) {
    event.setCancelled(true);

    ViewerRef viewer = ViewerRef.of(event.getWhoClicked().getUniqueId(), event.getWhoClicked().getName());

    int topSize = top.getSize();
    ItemStack oldCursor = event.getOldCursor();
    if (isEmpty(oldCursor)) return false;

    Set<Integer> targetSlots = new HashSet<>();
    for (int raw : event.getRawSlots()) {
      if (raw >= 0 && raw < topSize) targetSlots.add(raw);
    }
    if (targetSlots.isEmpty()) return false;

    Map<Integer, ItemStack> newItems = event.getNewItems();
    if (newItems.isEmpty()) return false;

    DragAction dragAction = (targetSlots.size() == 1) ? DragAction.SINGLE_SLOT : DragAction.MULTI_SLOT;

    var interaction = SimpleInteractionContext.drag(
        viewer,
        InventoryArea.TOP,
        dragAction,
        snapshotAdapter.toMenuItemSnapshot(oldCursor),
        targetSlots
    );

    Map<Integer, MenuItem> resultingBySlot = new HashMap<>();
    int totalPlaced = 0;

    for (int slot : targetSlots) {
      if (!depositPolicy.isSlotAllowed(slot)) return false;

      ItemStack intended = newItems.get(slot);
      if (isEmpty(intended)) continue;

      Optional<MenuItem> offeredOpt = snapshotAdapter.toMenuItemSnapshot(intended);
      if (offeredOpt.isEmpty()) return false;

      MenuItem offered = offeredOpt.get();
      var decision = depositHandler.onPlace(view.key(), viewer, slot, offered, interaction);
      if (!decision.allowed()) return false;

      MenuItem resulting = decision.resultingItem().orElse(offered);

      ItemStack current = top.getItem(slot);
      ItemStack resultStack = resulting.stack();
      if (!canPlaceOrStack(current, resultStack)) return false;

      resultingBySlot.put(slot, resulting);
      totalPlaced += Math.max(0, resulting.stack().getAmount());
    }

    if (resultingBySlot.isEmpty()) return false;

    for (var e : resultingBySlot.entrySet()) {
      int slot = e.getKey();
      MenuItem resulting = e.getValue();

      ItemStack current = top.getItem(slot);
      ItemStack toPlace = resulting.stack();

      if (isEmpty(current)) {
        view.patchSlotNow(slot, resulting);
      } else {
        ItemStack merged = current.clone();
        merged.setAmount(Math.min(merged.getMaxStackSize(), merged.getAmount() + toPlace.getAmount()));
        top.setItem(slot, merged);
        view.patchSlotNow(slot, snapshotAdapter.toMenuItemSnapshot(merged).orElse(resulting));
      }

      view.depositLedger().ifPresent(ledger -> ledger.recordDeposit(slot, resulting));
    }

    int remaining = Math.max(0, oldCursor.getAmount() - totalPlaced);
    if (remaining == 0) {
      setViewCursor(event.getView(), new ItemStack(Material.AIR));
    } else {
      ItemStack newCursor = oldCursor.clone();
      newCursor.setAmount(remaining);
      setViewCursor(event.getView(), newCursor);
    }

    return true;
  }

  @EventHandler
  public void onClose(InventoryCloseEvent event) {
    Inventory top = event.getView().getTopInventory();
    if (!(top.getHolder() instanceof PaperMenuHolder holder)) return;

    BukkitMenuView view = service.findOpenView(holder.viewerId());
    if (view == null) {
      service.clearView(holder.viewerId());
      return;
    }

    // If a new menu was opened for the same viewer, Bukkit may still deliver the close
    // event for the previous inventory. Ignore those stale events so we do not close
    // the newly opened view by accident.
    if (view.inventory() != top || !view.key().equals(holder.menuKey())) return;

    view.close(CloseReason.PLAYER_CLOSED);
  }

  private int tryDepositIntoSlot(
      BukkitMenuView view,
      DepositHandler depositHandler,
      DepositPolicy depositPolicy,
      Inventory top,
      ViewerRef viewer,
      int slot,
      ItemStack sourceStack,
      int maxToMove,
      io.nexstudios.menuservice.common.api.interaction.InteractionContext interaction
  ) {
    if (!inBounds(top, slot) || !depositPolicy.isSlotAllowed(slot) || isEmpty(sourceStack) || maxToMove <= 0) return 0;

    // Determine capacity in target (stackable)
    ItemStack current = top.getItem(slot);
    int capacity = capacityFor(current, sourceStack);
    if (capacity <= 0) return 0;

    int move = Math.min(maxToMove, capacity);

    // Offered is the moved amount
    ItemStack offeredBukkit = sourceStack.clone();
    offeredBukkit.setAmount(move);

    Optional<MenuItem> offeredOpt = snapshotAdapter.toMenuItemSnapshot(offeredBukkit);
    if (offeredOpt.isEmpty()) return 0;
    MenuItem offered = offeredOpt.get();

    var decision = depositHandler.onPlace(view.key(), viewer, slot, offered, interaction);
    if (!decision.allowed()) return 0;

    MenuItem resulting = decision.resultingItem().orElse(offered);

    ItemStack resultingStack = resulting.stack();
    if (isEmpty(current)) {
      view.patchSlotNow(slot, resulting);
    } else {
      if (canStack(current, resultingStack)) {
        ItemStack merged = current.clone();
        merged.setAmount(Math.min(merged.getMaxStackSize(), merged.getAmount() + resultingStack.getAmount()));
        top.setItem(slot, merged);
        view.patchSlotNow(slot, snapshotAdapter.toMenuItemSnapshot(merged).orElse(resulting));
      } else {
        return 0;
      }
    }

    view.depositLedger().ifPresent(ledger -> ledger.recordDeposit(slot, resulting));
    return resulting.stack().getAmount();
  }

  private boolean tryRemoveFromSlotToCursor(
      BukkitMenuView view,
      DepositHandler depositHandler,
      DepositPolicy depositPolicy,
      ViewerRef viewer,
      int slot,
      ItemStack current,
      InteractionContext interaction,
      InventoryClickEvent event
  ) {
    return tryRemoveFromSlotToTargetStack(
        view,
        depositHandler,
        depositPolicy,
        viewer,
        slot,
        current,
        interaction,
        event::getCursor,
        stack -> setViewCursor(event.getView(), stack)
    );
  }

  private void tryRemoveFromSlotToHotbar(
      BukkitMenuView view,
      DepositHandler depositHandler,
      DepositPolicy depositPolicy,
      ViewerRef viewer,
      int slot,
      ItemStack current,
      InteractionContext interaction,
      int hotbarIndex,
      InventoryClickEvent event
  ) {
    tryRemoveFromSlotToTargetStack(
        view,
        depositHandler,
        depositPolicy,
        viewer,
        slot,
        current,
        interaction,
        () -> event.getWhoClicked().getInventory().getItem(hotbarIndex),
        stack -> event.getWhoClicked().getInventory().setItem(hotbarIndex, stack)
    );
  }

  private static void setViewCursor(InventoryView view, ItemStack stack) {
    Objects.requireNonNull(view, "view must not be null");
    view.setCursor(stack);
  }

  private void tryRemoveFromSlotToOffhand(
      BukkitMenuView view,
      DepositHandler depositHandler,
      DepositPolicy depositPolicy,
      ViewerRef viewer,
      int slot,
      ItemStack current,
      InteractionContext interaction,
      InventoryClickEvent event
  ) {
    tryRemoveFromSlotToTargetStack(
        view,
        depositHandler,
        depositPolicy,
        viewer,
        slot,
        current,
        interaction,
        () -> event.getWhoClicked().getInventory().getItemInOffHand(),
        stack -> event.getWhoClicked().getInventory().setItemInOffHand(stack)
    );
  }

  private boolean tryRemoveFromSlotToTargetStack(
      BukkitMenuView view,
      DepositHandler depositHandler,
      DepositPolicy depositPolicy,
      ViewerRef viewer,
      int slot,
      ItemStack current,
      InteractionContext interaction,
      Supplier<ItemStack> targetGetter,
      Consumer<ItemStack> targetSetter
  ) {
    if (!depositPolicy.isSlotAllowed(slot) || isEmpty(current)) return false;

    Optional<MenuItem> currentOpt = snapshotAdapter.toMenuItemSnapshot(current);
    if (currentOpt.isEmpty()) return false;

    var decision = depositHandler.onRemove(view.key(), viewer, slot, currentOpt.get(), interaction);
    if (!decision.allowed()) return false;

    ItemStack target = targetGetter.get();
    if (isEmpty(target)) {
      targetSetter.accept(current.clone());
    } else {
      if (canFullyMergeInto(target, current)) {
        ItemStack merged = target.clone();
        merged.setAmount(target.getAmount() + current.getAmount());
        targetSetter.accept(merged);
      } else {
        return false;
      }
    }

    view.patchSlotNow(slot, null);
    view.depositLedger().ifPresent(ledger -> ledger.clearDeposit(slot));
    return true;
  }

  private boolean trySwapSlotWithTargetStack(
      BukkitMenuView view,
      DepositHandler depositHandler,
      ViewerRef viewer,
      int slot,
      ItemStack current,
      InteractionContext interaction,
      ItemStack target,
      Consumer<ItemStack> targetSetter
  ) {
    if (!canFullyMergeInto(target, current)) return false;

    Optional<MenuItem> currentOpt = snapshotAdapter.toMenuItemSnapshot(current);
    Optional<MenuItem> offeredOpt = snapshotAdapter.toMenuItemSnapshot(target);
    if (currentOpt.isEmpty() || offeredOpt.isEmpty()) return false;

    MenuItem currentItem = currentOpt.get();
    MenuItem offered = offeredOpt.get();

    var removeDecision = depositHandler.onRemove(view.key(), viewer, slot, currentItem, interaction);
    if (!removeDecision.allowed()) return false;

    var placeDecision = depositHandler.onPlace(view.key(), viewer, slot, offered, interaction);
    if (!placeDecision.allowed()) return false;

    ItemStack merged = target.clone();
    merged.setAmount(target.getAmount() + current.getAmount());
    targetSetter.accept(merged);

    MenuItem resulting = placeDecision.resultingItem().orElse(offered);
    view.patchSlotNow(slot, resulting);
    view.depositLedger().ifPresent(ledger -> {
      ledger.clearDeposit(slot);
      ledger.recordDeposit(slot, resulting);
    });
    return true;
  }

  private ResolvedMenuContext resolveActiveMenuContext(InventoryView inventoryView, Cancellable event, HumanEntity actor) {
    Inventory top = inventoryView.getTopInventory();
    if (!(top.getHolder() instanceof PaperMenuHolder holder)) return null;

    BukkitMenuView view = service.findOpenView(holder.viewerId());
    if (view == null || view.isClosed()) {
      event.setCancelled(true);
      return null;
    }

    // Smart throttling: ignore spam clicks/drags (but keep event cancelled inside menus)
    if (!service.allowGuiInteraction(holder.viewerId())) {
      actor.playSound(
          Sound.sound(Key.key("entity.villager.no"), Sound.Source.UI, 1f, 1f),
          Sound.Emitter.self());
      event.setCancelled(true);
      return null;
    }

    return new ResolvedMenuContext(top, holder, view);
  }

  private static boolean inBounds(Inventory inv, int slot) {
    return slot >= 0 && slot < inv.getSize();
  }

  private static boolean isEmpty(ItemStack stack) {
    return stack == null || stack.getType().isAir() || stack.getAmount() <= 0;
  }

  private static boolean canStack(ItemStack a, ItemStack b) {
    return !isEmpty(a) && !isEmpty(b) && a.isSimilar(b);
  }

  private static int capacityFor(ItemStack current, ItemStack incoming) {
    if (isEmpty(incoming)) return 0;

    int max = incoming.getMaxStackSize();
    if (isEmpty(current)) return max;

    if (canStack(current, incoming)) return Math.max(0, max - current.getAmount());
    return 0;
  }

  private static boolean canPlaceOrStack(ItemStack current, ItemStack incoming) {
    return capacityFor(current, incoming) >= incoming.getAmount();
  }

  private static boolean canFullyMergeInto(ItemStack target, ItemStack incoming) {
    if (isEmpty(incoming)) return true;
    if (isEmpty(target)) return incoming.getAmount() <= incoming.getMaxStackSize();
    if (canStack(target, incoming)) {
      int max = target.getMaxStackSize();
      return target.getAmount() + incoming.getAmount() <= max;
    }
    return false;
  }

  private record SimpleClickContext(
      BukkitMenuView view,
      ViewerRef viewer,
      int slot,
      ClickAction action
  ) implements MenuSlot.MenuClickContext {

    @Override public boolean isTopInventory() { return true; }
    @Override public boolean isBottomInventory() { return false; }

    @Override
    public BukkitMenuView view() {
      return view;
    }

    @Override
    public void setCurrentItem(MenuItem item) {
      MenuSlot.requireNonNullItem(item);
      view.patchSlotNow(slot, item);
    }

    @Override
    public void clearCurrentItem() {
      view.patchSlotNow(slot, null);
    }

    @Override
    public void cancel() {
      // Already cancelled by default.
    }
  }

  private record ResolvedMenuContext(
      Inventory top,
      PaperMenuHolder holder,
      BukkitMenuView view
  ) {}
}