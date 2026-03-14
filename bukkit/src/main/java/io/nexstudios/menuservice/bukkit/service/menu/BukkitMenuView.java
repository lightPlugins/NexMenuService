package io.nexstudios.menuservice.bukkit.service.menu;

import io.nexstudios.menuservice.bukkit.deposit.BukkitDepositLedger;
import io.nexstudios.menuservice.bukkit.deposit.DepositReturner;
import io.nexstudios.menuservice.bukkit.render.AsyncMenuRenderEngine;
import io.nexstudios.menuservice.common.api.ClosePhase;
import io.nexstudios.menuservice.common.api.CloseReason;
import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuKey;
import io.nexstudios.menuservice.common.api.MenuView;
import io.nexstudios.menuservice.common.api.ViewerRef;
import io.nexstudios.menuservice.common.api.deposit.DepositLedger;
import io.nexstudios.menuservice.common.api.deposit.DepositReturnStrategy;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.page.PageState;
import io.nexstudios.menuservice.common.api.page.control.PageControlStateStore;
import io.nexstudios.menuservice.common.api.render.MenuItemFingerprinter;
import io.nexstudios.menuservice.common.api.render.RenderReason;
import io.nexstudios.menuservice.common.api.render.RenderState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Bukkit menu view instance for a single viewer.
 */
public final class BukkitMenuView implements MenuView {

  private final BukkitMenuService service;

  private final MenuKey key;
  private final ViewerRef viewer;
  private final MenuDefinition definition;
  private final Inventory inventory;
  private final Instant openedAt;

  private final AsyncMenuRenderEngine renderEngine;

  // Render/diff state; guarded by stateLock because RenderDiff mutates RenderState
  private final Object stateLock = new Object();
  private final RenderState renderState = new RenderState();

  private final AtomicBoolean closed = new AtomicBoolean(false);

  /**
   * Next time (epoch millis) when an automatic TICK refresh should happen.
   * 0 means "not scheduled yet".
   */
  private final AtomicLong nextAutoRefreshAtMillis = new AtomicLong(0L);

  private final Optional<DepositLedger> depositLedger;
  private final DepositReturner depositReturner;

  private final Optional<PageState> pageState;

  /**
   * Cached page count per paged area id, updated during async rendering.
   */
  private final ConcurrentMap<String, Integer> pageCountByAreaId = new ConcurrentHashMap<>();

  /**
   * If set, PAGE_CHANGED renders only this area. Consumed by render engine.
   */
  private final AtomicReference<String> pageRenderTargetAreaId = new AtomicReference<>(null);

  /**
   * Sticky/manual slot overrides applied via click handlers.
   * These must survive refresh renders until the view is closed.
   */
  private final ConcurrentMap<Integer, MenuItem> stickyOverrides = new ConcurrentHashMap<>();

  private final PageControlStateStore pageControlStateStore;


  BukkitMenuView(
      BukkitMenuService service,
      MenuKey key,
      ViewerRef viewer,
      MenuDefinition definition,
      Inventory inventory,
      Instant openedAt,
      AsyncMenuRenderEngine renderEngine,
      PageControlStateStore pageControlStateStore
  ) {
    this.service = Objects.requireNonNull(service, "service must not be null");
    this.key = Objects.requireNonNull(key, "key must not be null");
    this.viewer = Objects.requireNonNull(viewer, "viewer must not be null");
    this.definition = Objects.requireNonNull(definition, "definition must not be null");
    this.inventory = Objects.requireNonNull(inventory, "inventory must not be null");
    this.openedAt = Objects.requireNonNull(openedAt, "openedAt must not be null");
    this.renderEngine = Objects.requireNonNull(renderEngine, "renderEngine must not be null");
    this.pageControlStateStore = Objects.requireNonNull(pageControlStateStore, "pageControlStateStore must not be null");

    this.depositLedger = definition.interactionPolicy().depositPolicy().isPresent()
        ? Optional.of(new BukkitDepositLedger())
        : Optional.empty();

    this.depositReturner = new DepositReturner();

    this.pageState = definition.pagedAreas().isPresent()
        ? Optional.of(new PageState())
        : Optional.empty();
  }

  public PageControlStateStore pageControlStateStore() {
    return pageControlStateStore;
  }

  @Override
  public void requestPagedAreaRefresh(String areaId) {
    if (closed.get()) return;
    targetPageAreaRender(areaId);
    requestRender(RenderReason.PAGE_CHANGED);
  }

  public Optional<DepositLedger> depositLedger() {
    return depositLedger;
  }

  public Optional<PageState> pageState() {
    return pageState;
  }

  public Map<Integer, MenuItem> stickyOverridesSnapshot() {
    return Map.copyOf(stickyOverrides);
  }

  public boolean isStickySlot(int slot) {
    return stickyOverrides.containsKey(slot);
  }

  public void clearStickySlot(int slot) {
    stickyOverrides.remove(slot);
  }

  private void setStickySlot(int slot, MenuItem itemOrNull) {
    if (itemOrNull == null) {
      stickyOverrides.remove(slot);
      return;
    }
    stickyOverrides.put(slot, itemOrNull);
  }

  public void updatePageCount(String areaId, int pageCount) {
    if (areaId == null || areaId.isBlank()) throw new IllegalArgumentException("areaId must not be blank");
    if (pageCount < 1) pageCount = 1;
    pageCountByAreaId.put(areaId, pageCount);
  }

  public int cachedPageCount(String areaId) {
    if (areaId == null || areaId.isBlank()) throw new IllegalArgumentException("areaId must not be blank");
    return pageCountByAreaId.getOrDefault(areaId, 1);
  }

  public void targetPageAreaRender(String areaId) {
    if (areaId == null || areaId.isBlank()) throw new IllegalArgumentException("areaId must not be blank");
    pageRenderTargetAreaId.set(areaId);
  }

  public Optional<String> consumePageAreaRenderTarget() {
    return Optional.ofNullable(pageRenderTargetAreaId.getAndSet(null));
  }

  public void requestRender(RenderReason reason) {
    if (closed.get()) return;
    renderEngine.requestRender(this, reason);
  }

  public void clearStickySlots(Set<Integer> slots) {
    Objects.requireNonNull(slots, "slots must not be null");
    for (int slot : slots) {
      stickyOverrides.remove(slot);
    }
  }

  @Override
  public MenuKey key() {
    return key;
  }

  @Override
  public ViewerRef viewer() {
    return viewer;
  }

  public MenuDefinition definition() {
    return definition;
  }

  public Inventory inventory() {
    return inventory;
  }

  @Override
  public Instant openedAt() {
    return openedAt;
  }

  public boolean isClosed() {
    return closed.get();
  }

  public long nextAutoRefreshAtMillis() {
    return nextAutoRefreshAtMillis.get();
  }

  public void scheduleNextAutoRefreshAtMillis(long epochMillis) {
    nextAutoRefreshAtMillis.set(epochMillis);
  }

  public Object renderStateLock() {
    return stateLock;
  }

  public RenderState renderState() {
    return renderState;
  }

  @Override
  public void requestRefresh() {
    requestRender(RenderReason.MANUAL_REFRESH);
  }

  void requestOpenRender() {
    requestRender(RenderReason.OPEN);
  }

  /**
   * Click-handler fast path: patch a single slot immediately (main thread),
   * and keep RenderState consistent to avoid "reverting" on the next diff.
   *
   * Additionally marks the slot as sticky so future refresh renders won't overwrite it.
   */
  public void patchSlotNow(int slot, MenuItem itemOrNull) {
    if (closed.get()) return;

    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(renderEngine.plugin(), () -> patchSlotNow(slot, itemOrNull));
      return;
    }

    if (slot < 0 || slot >= inventory.getSize()) return;

    if (itemOrNull == null) {
      inventory.clear(slot);
      setStickySlot(slot, null);
      synchronized (stateLock) {
        renderState.clearSlot(slot);
      }
      return;
    }

    inventory.setItem(slot, itemOrNull.stack());
    setStickySlot(slot, itemOrNull);

    long fp = MenuItemFingerprinter.fingerprint(itemOrNull);
    synchronized (stateLock) {
      renderState.setFingerprint(slot, fp);
    }
  }

  @Override
  public void close(CloseReason reason) {
    if (!closed.compareAndSet(false, true)) return;

    // detach from service first to avoid races with InventoryCloseEvent
    service.clearViewIfSame(viewer.uniqueId(), this);

    // Close hook (BEFORE)
    definition.interactionHooks().ifPresent(hooks -> {
      try {
        hooks.onClose(key, viewer, reason, ClosePhase.BEFORE_CLOSE);
      } catch (Exception ignored) {
        // do not fail close because of hook exceptions
      }
    });

    Player player = Bukkit.getPlayer(viewer.uniqueId());
    if (player != null) {
      // Return deposits safely (if enabled)
      definition.interactionPolicy().depositPolicy().ifPresent(policy -> {
        depositLedger.ifPresent(ledger -> {
          var snap = ledger.snapshot();
          if (!snap.isEmpty()) {
            DepositReturnStrategy strategy = policy.returnStrategy();
            depositReturner.returnDeposits(player, snap, strategy);
            ledger.clearAll();
          }
        });
      });

      if (player.isOnline()) {
        player.closeInventory();
      }
    }

    // Close hook (AFTER)
    definition.interactionHooks().ifPresent(hooks -> {
      try {
        hooks.onClose(key, viewer, reason, ClosePhase.AFTER_CLOSE);
      } catch (Exception ignored) {
        // do not fail close because of hook exceptions
      }
    });
  }
}