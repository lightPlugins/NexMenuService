package io.nexstudios.menuservice.bukkit.service.menu;

import io.nexstudios.menuservice.bukkit.adapter.BukkitMenuItemAdapter;
import io.nexstudios.menuservice.bukkit.inventory.PaperMenuHolder;
import io.nexstudios.menuservice.bukkit.listener.MenuInventoryListeners;
import io.nexstudios.menuservice.bukkit.listener.MenuLifecycleListeners;
import io.nexstudios.menuservice.bukkit.render.AsyncMenuRenderEngine;
import io.nexstudios.menuservice.common.api.*;
import io.nexstudios.menuservice.common.api.registry.MenuNotRegisteredException;
import io.nexstudios.menuservice.common.api.render.RenderReason;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bukkit/Paper implementation of {@link MenuService}.
 *
 * Lifecycle is controlled by a host plugin via {@code BukkitMenuServiceModule}.
 */
public final class BukkitMenuService implements MenuService {

  private final Plugin plugin;
  private final MenuRegistry registry;
  private final BukkitMenuItemAdapter itemAdapter = new BukkitMenuItemAdapter();

  private final Map<UUID, BukkitMenuView> openViews = new ConcurrentHashMap<>();
  private final MenuInventoryListeners inventoryListeners;
  private final MenuLifecycleListeners lifecycleListeners;

  private final AsyncMenuRenderEngine renderEngine;

  private BukkitTask refreshTask;

  public BukkitMenuService(Plugin plugin, MenuRegistry registry) {
    this.plugin = Objects.requireNonNull(plugin, "plugin must not be null");
    this.registry = Objects.requireNonNull(registry, "registry must not be null");

    this.renderEngine = new AsyncMenuRenderEngine(plugin, itemAdapter);

    this.inventoryListeners = new MenuInventoryListeners(this, itemAdapter);
    this.lifecycleListeners = new MenuLifecycleListeners(this, plugin);
  }

  public void bootstrap() {
    plugin.getServer().getPluginManager().registerEvents(inventoryListeners, plugin);
    plugin.getServer().getPluginManager().registerEvents(lifecycleListeners, plugin);

    // Global refresh loop (every 10 ticks = 0.5s). Individual menus still control their own interval.
    if (refreshTask == null) {
      refreshTask = Bukkit.getScheduler().runTaskTimer(plugin, this::tickRefresh, 10L, 10L);
    }
  }

  private void tickRefresh() {
    long nowMillis = System.currentTimeMillis();

    for (BukkitMenuView view : openViews.values()) {
      if (view == null || view.isClosed()) continue;

      long nextAt = view.nextAutoRefreshAtMillis();
      if (nextAt == 0L) {
        Duration interval = renderEngine.resolveInterval(view.definition());
        long scheduled = nowMillis + Math.max(50L, interval.toMillis());
        view.scheduleNextAutoRefreshAtMillis(scheduled);
        continue;
      }

      if (nowMillis >= nextAt) {
        Duration interval = renderEngine.resolveInterval(view.definition());
        long scheduled = nowMillis + Math.max(50L, interval.toMillis());
        view.scheduleNextAutoRefreshAtMillis(scheduled);

        renderEngine.requestRender(view, RenderReason.TICK);
      }
    }
  }

  public void shutdown() {
    if (refreshTask != null) {
      refreshTask.cancel();
      refreshTask = null;
    }

    // Close all views and return deposits later (deposit feature will plug into view.close()).
    for (BukkitMenuView view : openViews.values()) {
      try {
        view.close(CloseReason.PLUGIN_DISABLED);
      } catch (Exception ignored) {
        // Avoid failing shutdown due to one bad view.
      }
    }
    openViews.clear();

    renderEngine.shutdown();
  }

  @Override
  public MenuRegistry registry() {
    return registry;
  }

  @Override
  public void open(ViewerRef viewer, MenuKey key) {
    Objects.requireNonNull(viewer, "viewer must not be null");
    Objects.requireNonNull(key, "key must not be null");

    MenuDefinition def = registry.find(key).orElseThrow(() -> new MenuNotRegisteredException(key));

    Player player = Bukkit.getPlayer(viewer.uniqueId());
    if (player == null) {
      throw new IllegalStateException("Viewer is not online: " + viewer.uniqueId());
    }

    // Close previous view if present
    BukkitMenuView existing = openViews.get(viewer.uniqueId());
    if (existing != null) {
      existing.close(CloseReason.OPENED_OTHER_MENU);
    }

    int size = def.rows() * 9;
    Inventory inv = Bukkit.createInventory(
        new PaperMenuHolder(viewer.uniqueId(), key),
        size,
        Component.text(def.title())
    );

    BukkitMenuView view = new BukkitMenuView(
        this,
        key,
        viewer,
        def,
        inv,
        Instant.now(),
        renderEngine
    );

    // schedule first auto refresh relative to now
    Duration interval = renderEngine.resolveInterval(def);
    view.scheduleNextAutoRefreshAtMillis(System.currentTimeMillis() + Math.max(50L, interval.toMillis()));

    openViews.put(viewer.uniqueId(), view);

    player.openInventory(inv);

    // Initial render async -> diff -> apply on main thread
    view.requestOpenRender();
  }

  @Override
  public Optional<MenuView> findOpenView(ViewerRef viewer) {
    Objects.requireNonNull(viewer, "viewer must not be null");
    return Optional.ofNullable(openViews.get(viewer.uniqueId()));
  }

  public BukkitMenuView findOpenView(UUID viewerId) {
    return openViews.get(viewerId);
  }

  public void clearView(UUID viewerId) {
    openViews.remove(viewerId);
  }

  public void clearViewIfSame(UUID viewerId, BukkitMenuView view) {
    openViews.remove(viewerId, view);
  }
}