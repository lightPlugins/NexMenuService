package io.nexstudios.menuservice.bukkit.render;

import io.nexstudios.menuservice.bukkit.service.menu.BukkitMenuView;
import io.nexstudios.menuservice.bukkit.service.menu.ClickHandlerStore;
import io.nexstudios.menuservice.common.api.MenuDefinition;
import io.nexstudios.menuservice.common.api.MenuDefaults;
import io.nexstudios.menuservice.common.api.MenuSlot;
import io.nexstudios.menuservice.common.api.deposit.DepositLedger;
import io.nexstudios.menuservice.common.api.deposit.DepositPolicy;
import io.nexstudios.menuservice.common.api.item.MenuItem;
import io.nexstudios.menuservice.common.api.item.MenuItemSupplier;
import io.nexstudios.menuservice.common.api.page.PageRenderer;
import io.nexstudios.menuservice.common.api.page.PageSlotMapper;
import io.nexstudios.menuservice.common.api.page.PagedAreaDefinition;
import io.nexstudios.menuservice.common.api.page.PageState;
import io.nexstudios.menuservice.common.api.page.control.PageControlBinding;
import io.nexstudios.menuservice.common.api.page.control.PageControlButton;
import io.nexstudios.menuservice.common.api.page.control.PageControlStateStore;
import io.nexstudios.menuservice.common.api.page.control.PageFilterControl;
import io.nexstudios.menuservice.common.api.page.control.PageSortControl;
import io.nexstudios.menuservice.common.api.render.RenderDiff;
import io.nexstudios.menuservice.common.api.render.RenderPatch;
import io.nexstudios.menuservice.common.api.render.RenderPlan;
import io.nexstudios.menuservice.common.api.render.RenderReason;
import io.nexstudios.menuservice.common.api.render.RenderResult;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
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
import io.nexstudios.menuservice.common.api.render.*;

public final class AsyncMenuRenderEngine {

  private final Plugin plugin;

  private final ExecutorService executor;

  private final ConcurrentMap<BukkitMenuView, RenderGate> gates = new ConcurrentHashMap<>();

  private final Duration defaultInterval = Duration.ofSeconds(1);

  public AsyncMenuRenderEngine(Plugin plugin) {
    this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");

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

  public void clearGate(BukkitMenuView view) {
    if (view != null) {
      gates.remove(view);
    }
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

    // Populator is allowed to enqueue planned items (suppliers).
    def.populator().populate(ctx);

    Optional<String> pageTarget = Optional.empty();
    if (reason == RenderReason.PAGE_CHANGED) {
      pageTarget = view.consumePageAreaRenderTarget();
      pageTarget.ifPresent(areaId -> clearStickyForPagedArea(view, areaId));
    }

    PagingBundle paging = renderPaging(view, pageTarget);
    ButtonBundle buttons = renderControlButtons(view);

    RenderPlan base = ctx.toRenderPlan();
    RenderPlan merged = merge(base, paging.plan());
    merged = merge(merged, buttons.plan());

    Map<Integer, MenuSlot.MenuClickHandler> mergedHandlers = new HashMap<>(ctx.clickHandlers());
    mergedHandlers.putAll(paging.handlers());
    mergedHandlers.putAll(buttons.handlers());

    return new RenderBundle(merged, Map.copyOf(mergedHandlers));
  }

  private static RenderPlan merge(RenderPlan a, RenderPlan b) {
    if (a == null || a.slotsToItems().isEmpty() && a.clearedSlots().isEmpty()) return b;
    if (b == null || b.slotsToItems().isEmpty() && b.clearedSlots().isEmpty()) return a;

    Map<Integer, MenuItemSupplier> items = new HashMap<>(a.slotsToItems());
    items.putAll(b.slotsToItems());

    Set<Integer> cleared = new HashSet<>(a.clearedSlots());
    cleared.addAll(b.clearedSlots());

    cleared.removeAll(items.keySet());

    return new RenderPlan(Map.copyOf(items), Set.copyOf(cleared));
  }

  private void apply(BukkitMenuView view, RenderBundle bundle) {
    if (!Bukkit.isPrimaryThread()) {
      Bukkit.getScheduler().runTask(plugin, () -> apply(view, bundle));
      return;
    }
    if (view.isClosed()) return;

    Inventory inv = view.inventory();

    // Materialize planned items on the main thread
    RenderResult result = materialize(bundle.plan());

    // Main-thread only: inject defaults + apply decorations (touch Bukkit internals)
    result = injectDefaultPagingNavItemsIfEmpty(view, result);
    result = applyEmptySlotFiller(view.definition(), inv.getSize(), result);

    RenderResult filtered = filterOutActiveDepositSlots(view, result);
    filtered = filterOutStickyOverrides(view, filtered);

    RenderPatch patch;
    synchronized (view.renderStateLock()) {
      patch = RenderDiff.diff(view.renderState(), filtered);
    }

    if (!patch.isEmpty()) {
      applyPatchToInventoryFast(inv, patch);
    }

    Map<Integer, MenuSlot.MenuClickHandler> handlers = new HashMap<>(bundle.handlers());
    injectPagingNavigationHandlers(view, handlers);
    ClickHandlerStore.attach(inv, handlers);
  }

  private void applyPatchToInventoryFast(Inventory inv, RenderPatch patch) {
    int size = inv.getSize();
    ItemStack[] contents = inv.getContents();

    // Apply clears
    for (int slot : patch.clearedSlots()) {
      if (slot >= 0 && slot < size) {
        contents[slot] = null;
      }
    }

    // Apply changes
    for (var e : patch.changedSlots().entrySet()) {
      int slot = e.getKey();
      if (slot < 0 || slot >= size) continue;
      contents[slot] = e.getValue().stack();
    }

    // Bulk-apply to inventory (much faster than individual setItem calls)
    inv.setContents(contents);
  }

  private static RenderResult materialize(RenderPlan plan) {
    if (plan == null) return RenderResult.empty();

    Map<Integer, MenuItem> items = new HashMap<>();
    for (var e : plan.slotsToItems().entrySet()) {
      int slot = e.getKey();
      MenuItemSupplier supplier = e.getValue();
      if (supplier == null) continue;

      MenuItem item = supplier.get();
      if (item == null) continue;

      items.put(slot, item);
    }

    return new RenderResult(Map.copyOf(items), Set.copyOf(plan.clearedSlots()));
  }

  private PagingBundle renderPaging(BukkitMenuView view, Optional<String> targetAreaId) {
    var pagedOpt = view.definition().pagedAreas();
    var pageStateOpt = view.pageState();
    if (pagedOpt.isEmpty() || pageStateOpt.isEmpty()) return PagingBundle.empty();

    PageState state = pageStateOpt.get();

    Map<Integer, MenuItemSupplier> items = new HashMap<>();
    Set<Integer> cleared = new HashSet<>();
    Map<Integer, MenuSlot.MenuClickHandler> handlers = new HashMap<>();

    List<PageControlBinding> controls = view.definition().pageControls().orElse(List.of());
    PageControlStateStore stateStore = view.pageControlStateStore();

    for (PagedAreaDefinition<?> raw : pagedOpt.get()) {
      if (targetAreaId.isPresent() && !raw.id().equals(targetAreaId.get())) {
        continue;
      }

      @SuppressWarnings("unchecked")
      PagedAreaDefinition<Object> area = (PagedAreaDefinition<Object>) raw;

      List<Object> elements = new ArrayList<>(area.load(view.key(), view.viewer()));
      elements = applyControlsForArea(controls, stateStore, view, area.id(), elements);

      var model = area.model();
      int pageCount = model.pageCountFor(elements.size());
      view.updatePageCount(area.id(), pageCount);

      int requestedIndex = state.getIndex(area.id());
      int clampedIndex = model.clampPageIndex(requestedIndex, elements.size());
      if (clampedIndex != requestedIndex) {
        state.setIndex(area.id(), clampedIndex);
      }

      RenderPlan page = PageRenderer.renderPage(area, clampedIndex, elements);
      items.putAll(page.slotsToItems());
      cleared.addAll(page.clearedSlots());

      final List<Object> finalElements = elements;

      area.clickHandler().ifPresent(ch -> {
        int start = model.startIndex(clampedIndex);
        int end = model.endExclusiveIndex(clampedIndex, finalElements.size());
        List<Object> slice = finalElements.subList(start, end);

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
        new RenderPlan(Map.copyOf(items), Set.copyOf(cleared)),
        Map.copyOf(handlers)
    );
  }

  private ButtonBundle renderControlButtons(BukkitMenuView view) {
    var buttonsOpt = view.definition().pageControlButtons();
    if (buttonsOpt.isEmpty()) return ButtonBundle.empty();

    List<PageControlBinding> controls = view.definition().pageControls().orElse(List.of());
    PageControlStateStore stateStore = view.pageControlStateStore();

    Map<Integer, MenuItemSupplier> items = new HashMap<>();
    Map<Integer, MenuSlot.MenuClickHandler> handlers = new HashMap<>();

    for (PageControlButton btn : buttonsOpt.get()) {
      PageControlBinding binding = null;
      for (PageControlBinding b : controls) {
        if (!b.areaId().equals(btn.areaId())) continue;
        if (!b.controlId().equals(btn.controlId())) continue;
        binding = b;
        break;
      }
      if (binding == null) continue;

      var control = binding.control();
      var activeModeId = stateStore.getActiveModeId(view.viewer(), view.key(), btn.areaId(), control.controlId());

      items.put(btn.slot(), () -> btn.render(new PageControlButton.RenderContext(
          view.key(),
          view.viewer(),
          btn.areaId(),
          control,
          activeModeId
      )));

      handlers.put(btn.slot(), ctx -> btn.onClick(new PageControlButton.ClickContext(
          ctx.view(),
          view.key(),
          view.viewer(),
          btn.areaId(),
          control,
          stateStore
      )));
    }

    return new ButtonBundle(
        new RenderPlan(Map.copyOf(items), Set.of()),
        Map.copyOf(handlers)
    );
  }

  // Default nav injection stays RenderResult-based and runs in apply(...) on main thread
  private static RenderResult injectDefaultPagingNavItemsIfEmpty(BukkitMenuView view, RenderResult input) {
    var pagedOpt = view.definition().pagedAreas();
    if (pagedOpt.isEmpty()) return input;

    Map<Integer, MenuItem> items = new HashMap<>(input.slotsToItems());
    Set<Integer> cleared = new HashSet<>(input.clearedSlots());

    for (var area : pagedOpt.get()) {
      var nav = area.navigation();

      nav.previousSlot().ifPresent(slot -> items.putIfAbsent(slot,
          MenuItem.of(new ItemStack(Material.ARROW, 1))
      ));
      nav.nextSlot().ifPresent(slot -> items.putIfAbsent(slot,
          MenuItem.of(new ItemStack(Material.ARROW, 1))
      ));
      nav.refreshSlot().ifPresent(slot -> items.putIfAbsent(slot,
          MenuItem.of(new ItemStack(Material.PAPER, 1))
      ));
    }

    cleared.removeAll(items.keySet());
    return new RenderResult(Map.copyOf(items), Set.copyOf(cleared));
  }

  private static RenderResult applyEmptySlotFiller(MenuDefinition def, int size, RenderResult input) {
    Objects.requireNonNull(def, "def must not be null");
    Objects.requireNonNull(input, "input must not be null");
    if (size < 1) return input;

    if (!def.decorationsEnabled()) {
      return input;
    }

    MenuItem filler = def.emptySlotFiller().orElse(MenuDefaults.defaultEmptySlotFiller());

    Map<Integer, MenuItem> items = new HashMap<>(input.slotsToItems());
    Set<Integer> cleared = new HashSet<>(input.clearedSlots());

    for (int slot = 0; slot < size; slot++) {
      if (items.containsKey(slot)) continue;
      items.put(slot, filler);
      cleared.remove(slot);
    }

    return new RenderResult(Map.copyOf(items), Set.copyOf(cleared));
  }

  private void clearStickyForPagedArea(BukkitMenuView view, String areaId) {
    Objects.requireNonNull(view, "view must not be null");
    if (areaId == null || areaId.isBlank()) return;

    var pagedOpt = view.definition().pagedAreas();
    if (pagedOpt.isEmpty()) return;

    for (var area : pagedOpt.get()) {
      if (!area.id().equals(areaId)) continue;

      int capacity = area.bounds().capacity();
      var slots = PageSlotMapper.slotsFor(area.bounds(), capacity);
      view.clearStickySlots(Set.copyOf(slots));
      return;
    }
  }

  private static List<Object> applyControlsForArea(
      List<PageControlBinding> controls,
      PageControlStateStore stateStore,
      BukkitMenuView view,
      String areaId,
      List<Object> elements
  ) {
    if (controls == null || controls.isEmpty()) return elements;

    // 1) Filters
    for (PageControlBinding b : controls) {
      if (!b.areaId().equals(areaId)) continue;
      if (!(b.control() instanceof PageFilterControl<?> filterRaw)) continue;

      @SuppressWarnings("unchecked")
      PageFilterControl<Object> filter = (PageFilterControl<Object>) filterRaw;

      String modeId = stateStore
          .getActiveModeId(view.viewer(), view.key(), areaId, filter.controlId())
          .orElse(filter.defaultModeId());

      var pred = filter.predicateFor(modeId, view.key(), view.viewer());
      elements = elements.stream().filter(pred).toList();
    }

    // 2) Sorts (stable chaining in registration order; external can control order by how they add controls)
    java.util.Comparator<Object> combined = null;

    for (PageControlBinding b : controls) {
      if (!b.areaId().equals(areaId)) continue;
      if (!(b.control() instanceof PageSortControl<?> sortRaw)) continue;

      @SuppressWarnings("unchecked")
      PageSortControl<Object> sort = (PageSortControl<Object>) sortRaw;

      String modeId = stateStore
          .getActiveModeId(view.viewer(), view.key(), areaId, sort.controlId())
          .orElse(sort.defaultModeId());

      java.util.Comparator<Object> cmp = sort.comparatorFor(modeId, view.key(), view.viewer());
      combined = (combined == null) ? cmp : combined.thenComparing(cmp);
    }

    if (combined != null) {
      elements = elements.stream().sorted(combined).toList();
    }

    return elements;
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

  public Duration resolveInterval(MenuDefinition def) {
    return def.refreshInterval().orElse(defaultInterval);
  }

  private static final class RenderGate {
    final AtomicBoolean rendering = new AtomicBoolean(false);
    final AtomicBoolean pending = new AtomicBoolean(false);
  }

  private record RenderBundle(RenderPlan plan, Map<Integer, MenuSlot.MenuClickHandler> handlers) {
    private RenderBundle {
      Objects.requireNonNull(plan, "plan must not be null");
      Objects.requireNonNull(handlers, "handlers must not be null");
    }
  }

  private record PagingBundle(RenderPlan plan, Map<Integer, MenuSlot.MenuClickHandler> handlers) {
    private PagingBundle {
      Objects.requireNonNull(plan, "plan must not be null");
      Objects.requireNonNull(handlers, "handlers must not be null");
    }

    static PagingBundle empty() {
      return new PagingBundle(RenderPlan.empty(), Map.of());
    }
  }

  private record ButtonBundle(RenderPlan plan, Map<Integer, MenuSlot.MenuClickHandler> handlers) {
    private ButtonBundle {
      Objects.requireNonNull(plan, "plan must not be null");
      Objects.requireNonNull(handlers, "handlers must not be null");
    }

    static ButtonBundle empty() {
      return new ButtonBundle(RenderPlan.empty(), Map.of());
    }
  }
}