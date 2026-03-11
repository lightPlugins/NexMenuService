package io.nexstudios.menuservice.bukkit.render;


import io.nexstudios.menuservice.bukkit.adapter.BukkitMenuItemAdapter;
import io.nexstudios.menuservice.bukkit.service.menu.BukkitMenuView;
import io.nexstudios.menuservice.bukkit.service.menu.ClickHandlerStore;
import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuSlot;
import io.nexstudios.menuservice.common.api.MenuDefaults;
import io.nexstudios.menuservice.common.api.page.*;
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
import io.nexstudios.menuservice.common.api.item.MenuItem;

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

    RenderPopulateContext ctx = new RenderPopulateContext(view.key(), view.viewer(), size);
    def.populator().populate(ctx);

    Optional<String> pageTarget = Optional.empty();
    if (reason == RenderReason.PAGE_CHANGED) {
      pageTarget = view.consumePageAreaRenderTarget();
    }

    PagingBundle paging = renderPaging(view, pageTarget);

    RenderResult base = ctx.toRenderResult();
    RenderResult merged = merge(base, paging.result());

    if (reason != RenderReason.PAGE_CHANGED) {
      merged = injectDefaultPagingNavItemsIfEmpty(view, merged);
    }

    merged = applyEmptySlotFiller(def, size, merged);

    Map<Integer, MenuSlot.MenuClickHandler> mergedHandlers = new HashMap<>(ctx.clickHandlers());
    mergedHandlers.putAll(paging.handlers());

    if (reason == RenderReason.PAGE_CHANGED) {
      return new RenderBundle(paging.result(), Map.copyOf(mergedHandlers));
    }

    return new RenderBundle(merged, Map.copyOf(mergedHandlers));
  }

  private static RenderResult applyEmptySlotFiller(MenuDefinition def, int size, RenderResult input) {
    Objects.requireNonNull(def, "def must not be null");
    Objects.requireNonNull(input, "input must not be null");
    if (size < 1) return input;

    MenuItem filler = def.emptySlotFiller().orElse(MenuDefaults.defaultEmptySlotFiller());

    Map<Integer, MenuItem> items = new HashMap<>(input.slotsToItems());
    Set<Integer> cleared = new HashSet<>(input.clearedSlots());

    for (int slot = 0; slot < size; slot++) {
      if (items.containsKey(slot)) continue;

      // If the slot would end up empty (whether it was "cleared" or never set),
      // we decorate/fill it.
      items.put(slot, filler);
      cleared.remove(slot);
    }

    return new RenderResult(Map.copyOf(items), Set.copyOf(cleared));
  }

  private static RenderResult injectDefaultPagingNavItemsIfEmpty(BukkitMenuView view, RenderResult input) {
    var pagedOpt = view.definition().pagedAreas();
    if (pagedOpt.isEmpty()) return input;

    Map<Integer, MenuItem> items = new HashMap<>(input.slotsToItems());
    Set<Integer> cleared = new HashSet<>(input.clearedSlots());

    for (var area : pagedOpt.get()) {
      var nav = area.navigation();

      nav.previousSlot().ifPresent(slot -> items.putIfAbsent(slot,
          MenuItem.builder("minecraft:arrow").displayName("Previous").amount(1).build()
      ));
      nav.nextSlot().ifPresent(slot -> items.putIfAbsent(slot,
          MenuItem.builder("minecraft:arrow").displayName("Next").amount(1).build()
      ));
      nav.refreshSlot().ifPresent(slot -> items.putIfAbsent(slot,
          MenuItem.builder("minecraft:paper").displayName("Refresh").amount(1).build()
      ));
    }

    cleared.removeAll(items.keySet());
    return new RenderResult(Map.copyOf(items), Set.copyOf(cleared));
  }

  private PagingBundle renderPaging(BukkitMenuView view, Optional<String> targetAreaId) {
    var pagedOpt = view.definition().pagedAreas();
    var pageStateOpt = view.pageState();
    if (pagedOpt.isEmpty() || pageStateOpt.isEmpty()) return PagingBundle.empty();

    PageState state = pageStateOpt.get();

    Map<Integer, io.nexstudios.menuservice.common.api.item.MenuItem> items = new HashMap<>();
    Set<Integer> cleared = new HashSet<>();
    Map<Integer, MenuSlot.MenuClickHandler> handlers = new HashMap<>();

    for (PagedAreaDefinition<?> raw : pagedOpt.get()) {
      if (targetAreaId.isPresent() && !raw.id().equals(targetAreaId.get())) {
        continue;
      }

      @SuppressWarnings("unchecked")
      PagedAreaDefinition<Object> area = (PagedAreaDefinition<Object>) raw;

      List<Object> elements = area.load(view.key(), view.viewer());

      PageModel model = area.model();
      int pageCount = model.pageCountFor(elements.size());
      view.updatePageCount(area.id(), pageCount);

      int requestedIndex = state.getIndex(area.id());
      int clampedIndex = model.clampPageIndex(requestedIndex, elements.size());
      if (clampedIndex != requestedIndex) {
        state.setIndex(area.id(), clampedIndex);
      }

      RenderResult page = PageRenderer.renderPage(area, clampedIndex, elements);
      items.putAll(page.slotsToItems());
      cleared.addAll(page.clearedSlots());

      // Build click handlers for the currently rendered slice (if configured)
      area.clickHandler().ifPresent(ch -> {
        int start = model.startIndex(clampedIndex);
        int end = model.endExclusiveIndex(clampedIndex, elements.size());
        List<Object> slice = elements.subList(start, end);

        List<Integer> targetSlots = PageSlotMapper.slotsFor(area.bounds(), slice.size());
        for (int i = 0; i < slice.size(); i++) {
          Object element = slice.get(i);
          int globalIndex = start + i;
          int slot = targetSlots.get(i);

          handlers.put(slot, ctx -> ch.onClick(element, globalIndex, ctx));
        }
      });
    }

    return new PagingBundle(
        new RenderResult(Map.copyOf(items), Set.copyOf(cleared)),
        Map.copyOf(handlers)
    );
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
    filtered = filterOutStickyOverrides(view, filtered);

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

  private static RenderResult filterOutStickyOverrides(BukkitMenuView view, RenderResult input) {
    Objects.requireNonNull(view, "view must not be null");
    Objects.requireNonNull(input, "input must not be null");

    Map<Integer, MenuItem> sticky = view.stickyOverridesSnapshot();
    if (sticky.isEmpty()) return input;

    Map<Integer, MenuItem> items = new HashMap<>(input.slotsToItems());
    Set<Integer> cleared = new HashSet<>(input.clearedSlots());

    // If a slot is sticky, never touch it during refresh renders:
    // - don't override it with rendered items
    // - don't clear it
    for (int slot : sticky.keySet()) {
      items.remove(slot);
      cleared.remove(slot);
    }

    return new RenderResult(Map.copyOf(items), Set.copyOf(cleared));
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
        view.targetPageAreaRender(raw.id());
        view.requestRender(RenderReason.PAGE_CHANGED);
      }));

      nav.nextSlot().ifPresent(nextSlot -> handlers.put(nextSlot, ctx -> {
        int current = state.getIndex(raw.id());
        int pageCount = view.cachedPageCount(raw.id());
        int maxIndex = Math.max(0, pageCount - 1);
        if (current >= maxIndex) return;

        state.setIndex(raw.id(), current + 1);
        view.targetPageAreaRender(raw.id());
        view.requestRender(RenderReason.PAGE_CHANGED);
      }));

      nav.refreshSlot().ifPresent(refreshSlot -> handlers.put(refreshSlot, ctx -> {
        view.targetPageAreaRender(raw.id());
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

  private record PagingBundle(RenderResult result, Map<Integer, MenuSlot.MenuClickHandler> handlers) {
    private PagingBundle {
      Objects.requireNonNull(result, "result must not be null");
      Objects.requireNonNull(handlers, "handlers must not be null");
    }

    static PagingBundle empty() {
      return new PagingBundle(RenderResult.empty(), Map.of());
    }
  }
}