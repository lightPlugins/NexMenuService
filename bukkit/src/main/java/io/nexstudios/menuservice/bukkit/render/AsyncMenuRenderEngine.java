package io.nexstudios.menuservice.bukkit.render;

import io.nexstudios.menuservice.bukkit.adapter.BukkitMenuItemAdapter;
import io.nexstudios.menuservice.bukkit.service.menu.BukkitMenuView;
import io.nexstudios.menuservice.bukkit.service.menu.ClickHandlerStore;
import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuSlot;
import io.nexstudios.menuservice.common.api.page.PagedAreaDefinition;
import io.nexstudios.menuservice.common.api.page.PageModel;
import io.nexstudios.menuservice.common.api.page.PageRenderer;
import io.nexstudios.menuservice.common.api.page.PageState;
import io.nexstudios.menuservice.common.api.deposit.DepositLedger;
import io.nexstudios.menuservice.common.api.deposit.DepositPolicy;
import io.nexstudios.menuservice.common.api.render.RenderDiff;
import io.nexstudios.menuservice.common.api.render.RenderPatch;
import io.nexstudios.menuservice.common.api.render.RenderReason;
import io.nexstudios.menuservice.common.api.render.RenderResult;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Async render -> diff -> main-thread apply pipeline.
 *
 * Phase: base runtime (paging/deposits integrated later).
 */
public final class AsyncMenuRenderEngine {

  private final Plugin plugin;
  private final BukkitMenuItemAdapter itemAdapter;

  private final ExecutorService executor;

  // Per-view coalescing: one render at a time, re-run once if requested while rendering.
  private final ConcurrentMap<BukkitMenuView, RenderGate> gates = new ConcurrentHashMap<>();

  private final Duration defaultInterval = Duration.ofSeconds(1);

  public AsyncMenuRenderEngine(Plugin plugin, BukkitMenuItemAdapter itemAdapter) {
    this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
    this.itemAdapter = Objects.requireNonNull(itemAdapter, "itemAdapter must not be null");

    this.executor = Executors.newFixedThreadPool(
        Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
        r -> {
          Thread t = new Thread(r, "nex-menu-render");
          t.setDaemon(true);
          return t;
        }
    );
  }

  public Plugin plugin() {
    return plugin;
  }

  public BukkitMenuItemAdapter itemAdapter() {
    return itemAdapter;
  }

  public void shutdown() {
    executor.shutdownNow();
    gates.clear();
  }

  public void requestRender(BukkitMenuView view, RenderReason reason) {
    if (view == null || view.isClosed()) return;

    RenderGate gate = gates.computeIfAbsent(view, v -> new RenderGate());
    gate.pending.set(true);
    tryStart(view, gate, reason);
  }

  private void tryStart(BukkitMenuView view, RenderGate gate, RenderReason reason) {
    if (!gate.rendering.compareAndSet(false, true)) return;

    gate.pending.set(false);

    CompletableFuture
        .supplyAsync(() -> render(view, reason), executor)
        .whenComplete((bundle, err) -> Bukkit.getScheduler().runTask(plugin, () -> {
          try {
            if (err != null || bundle == null || view.isClosed()) return;
            apply(view, bundle);
          } finally {
            gate.rendering.set(false);
            if (gate.pending.get() && !view.isClosed()) {
              tryStart(view, gate, RenderReason.UNKNOWN);
            }
          }
        }));
  }

  private RenderBundle render(BukkitMenuView view, RenderReason reason) {
    MenuDefinition def = view.definition();
    int size = view.inventory().getSize();

    // Always run populator to keep click handlers up-to-date (even for PAGE_CHANGED partial renders).
    RenderPopulateContext ctx = new RenderPopulateContext(view.key(), view.viewer(), size);
    def.populator().populate(ctx);

    // Add paging overlay + navigation handlers
    RenderResult paging = renderPaging(view, reason);

    RenderResult base = ctx.toRenderResult();
    RenderResult merged = merge(base, paging);

    // For PAGE_CHANGED: only output page-area changes (and clears) to keep rest untouched.
    if (reason == RenderReason.PAGE_CHANGED) {
      return new RenderBundle(paging, ctx.clickHandlers());
    }

    return new RenderBundle(merged, ctx.clickHandlers());
  }

  private RenderResult renderPaging(BukkitMenuView view, RenderReason reason) {
    var pagedOpt = view.definition().pagedAreas();
    var pageStateOpt = view.pageState();
    if (pagedOpt.isEmpty() || pageStateOpt.isEmpty()) return RenderResult.empty();

    PageState state = pageStateOpt.get();

    Map<Integer, io.nexstudios.menuservice.common.api.item.MenuItem> items = new HashMap<>();
    Set<Integer> cleared = new HashSet<>();

    for (PagedAreaDefinition<?> raw : pagedOpt.get()) {
      @SuppressWarnings("unchecked")
      PagedAreaDefinition<Object> area = (PagedAreaDefinition<Object>) raw;

      List<Object> elements = area.load(view.key(), view.viewer()); // async thread, OK

      PageModel model = area.model();
      int pageCount = model.pageCountFor(elements.size());
      view.updatePageCount(area.id(), pageCount);

      int requestedIndex = state.getIndex(area.id());
      int clampedIndex = model.clampPageIndex(requestedIndex, elements.size());

      // Keep state consistent (prevents drifting to huge indices)
      if (clampedIndex != requestedIndex) {
        state.setIndex(area.id(), clampedIndex);
      }

      RenderResult page = PageRenderer.renderPage(area, clampedIndex, elements);
      items.putAll(page.slotsToItems());
      cleared.addAll(page.clearedSlots());
    }

    return new RenderResult(Map.copyOf(items), Set.copyOf(cleared));
  }

  private static RenderResult merge(RenderResult a, RenderResult b) {
    if (a == null || a.slotsToItems().isEmpty() && a.clearedSlots().isEmpty()) return b;
    if (b == null || b.slotsToItems().isEmpty() && b.clearedSlots().isEmpty()) return a;

    Map<Integer, io.nexstudios.menuservice.common.api.item.MenuItem> items = new HashMap<>(a.slotsToItems());
    items.putAll(b.slotsToItems());

    Set<Integer> cleared = new HashSet<>(a.clearedSlots());
    cleared.addAll(b.clearedSlots());

    // If a slot is set, it shouldn't be cleared.
    cleared.removeAll(items.keySet());

    return new RenderResult(Map.copyOf(items), Set.copyOf(cleared));
  }

  private void apply(BukkitMenuView view, RenderBundle bundle) {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, () -> apply(view, bundle));
      return;
    }
    if (view.isClosed()) return;

    Inventory inv = view.inventory();
    RenderResult result = bundle.result();

    RenderResult filtered = filterOutActiveDepositSlots(view, result);

    RenderPatch patch;
    synchronized (view.renderStateLock()) {
      patch = RenderDiff.diff(view.renderState(), filtered);
    }

    if (!patch.isEmpty()) {
      for (int slot : patch.clearedSlots()) {
        if (slot >= 0 && slot < inv.getSize()) inv.clear(slot);
      }
      for (var e : patch.changedSlots().entrySet()) {
        int slot = e.getKey();
        if (slot < 0 || slot >= inv.getSize()) continue;
        inv.setItem(slot, itemAdapter.toItemStack(e.getValue()));
      }
    }

    Map<Integer, MenuSlot.MenuClickHandler> handlers = new HashMap<>(bundle.handlers());
    injectPagingNavigationHandlers(view, handlers);
    ClickHandlerStore.attach(inv, handlers);
  }

  private void injectPagingNavigationHandlers(BukkitMenuView view, Map<Integer, MenuSlot.MenuClickHandler> handlers) {
    var pagedOpt = view.definition().pagedAreas();
    var stateOpt = view.pageState();
    if (pagedOpt.isEmpty() || stateOpt.isEmpty()) return;

    PageState state = stateOpt.get();

    for (var raw : pagedOpt.get()) {
      var nav = raw.navigation();

      nav.previousSlot().ifPresent(prevSlot -> handlers.put(prevSlot, ctx -> {
        int current = state.getIndex(raw.id());
        if (current <= 0) return;

        state.setIndex(raw.id(), current - 1);
        view.requestRender(RenderReason.PAGE_CHANGED);
      }));

      nav.nextSlot().ifPresent(nextSlot -> handlers.put(nextSlot, ctx -> {
        int current = state.getIndex(raw.id());
        int pageCount = view.cachedPageCount(raw.id());
        int maxIndex = Math.max(0, pageCount - 1);
        if (current >= maxIndex) return;

        state.setIndex(raw.id(), current + 1);
        view.requestRender(RenderReason.PAGE_CHANGED);
      }));

      nav.refreshSlot().ifPresent(refreshSlot -> handlers.put(refreshSlot, ctx -> {
        view.requestRender(RenderReason.PAGE_CHANGED);
      }));
    }
  }

  private static RenderResult filterOutActiveDepositSlots(BukkitMenuView view, RenderResult input) {
    Objects.requireNonNull(view, "view must not be null");
    Objects.requireNonNull(input, "input must not be null");

    var policyOpt = view.definition().interactionPolicy().depositPolicy();
    var ledgerOpt = view.depositLedger();
    if (policyOpt.isEmpty() || ledgerOpt.isEmpty()) return input;

    DepositPolicy policy = policyOpt.get();
    DepositLedger ledger = ledgerOpt.get();

    // Collect active deposit slots (tracked in ledger AND within allowedTopSlots)
    Set<Integer> protectedSlots = new HashSet<>();
    for (int slot : policy.allowedTopSlots()) {
      if (ledger.findDepositedItem(slot).isPresent()) {
        protectedSlots.add(slot);
      }
    }
    if (protectedSlots.isEmpty()) return input;

    Map<Integer, io.nexstudios.menuservice.common.api.item.MenuItem> items = new HashMap<>(input.slotsToItems());
    for (int slot : protectedSlots) items.remove(slot);

    Set<Integer> cleared = new HashSet<>(input.clearedSlots());
    cleared.removeAll(protectedSlots);

    return new RenderResult(Map.copyOf(items), Set.copyOf(cleared));
  }

  public Duration resolveInterval(MenuDefinition def) {
    return def.refreshInterval().orElse(defaultInterval);
  }

  private static final class RenderGate {
    final AtomicBoolean rendering = new AtomicBoolean(false);
    final AtomicBoolean pending = new AtomicBoolean(false);
  }

  private record RenderBundle(RenderResult result, Map<Integer, MenuSlot.MenuClickHandler> handlers) {
    private RenderBundle {
      Objects.requireNonNull(result, "result must not be null");
      Objects.requireNonNull(handlers, "handlers must not be null");
    }
  }
}