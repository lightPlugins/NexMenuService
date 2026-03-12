# NexMenuService ✨🧩

**Async-first menus for Paper/Spigot** — clean definitions, smooth rendering, and powerful paging.

---

## Overview 🚀

**NexMenuService** is a hosted **menu service** for Paper/Spigot plugins that helps you build modern inventory GUIs using a **definition-based architecture**.

Instead of rebuilding menus ad hoc, you define them once with a stable `MenuKey` and open them for any viewer through `MenuService.open(...)`. The service takes care of the heavy lifting, including:

- asynchronous rendering
- render-state diffing
- safe application on the main server thread

This keeps menu code clean, scalable, and responsive — even for frequently refreshed menus or large paginated datasets.

> The render/populator pipeline is designed to keep your menu logic **thread-safe**, **predictable**, and largely **platform-agnostic**.

---

## Features ✅

### Core API

#### `MenuService`
The central API for managing menu views.

- Register menus through a registry
- Open menus by `MenuKey`
- Resolve or find the currently open menu view of a player/viewer

#### Definition-based menus
Menus are built around stable, reusable definitions.

- Central menu blueprints via `MenuDefinition`
- Stable identifiers via `MenuKey`
- Strong validation and predictable runtime behavior

---

### Performance & User Experience

#### Async render pipeline
Menus are rendered with performance in mind.

- Populate menu state off the main thread
- Compute a **diff** against the previous render result
- Apply only changed slots on the main thread

This reduces unnecessary work and helps minimize flicker during updates.

#### Refresh support
Menus can update automatically or on demand.

- Configurable auto-refresh intervals
- Manual refresh support

---

### Interaction

#### Slot click handlers
Attach actions directly to individual slots.

- Define click handlers per slot
- Executed safely on the server main thread

#### Menu lifecycle handling
Menu views are managed safely across typical edge cases.

- Player closes the inventory
- Player disconnects
- Another menu is opened
- Plugin shuts down

Optional lifecycle hooks are also supported, such as:

- before-close hooks
- after-close hooks

---

## Optional Modules

### Paging 📄

Render large collections inside a bounded inventory area with built-in paging support.

- Define rectangular bounds within the inventory grid
- Render slices of larger datasets into that area
- Built-in navigation slots:
    - previous page
    - next page
    - refresh
- Optional element click handling  
  Maps a clicked slot to its corresponding element and global index

Perfect for player lists, shop entries, auction items, and similar datasets.

---

### Deposits 📥

Support item deposits directly into menu slots.

- Mark top-inventory slots as deposit-enabled
- Allow players to move items from their inventory into those slots
- Track deposited items per menu view
- Automatically return deposited items when the menu closes
    - inventory-first
    - drop fallback if inventory is full

Useful for trading, crafting previews, upgrade systems, or input-driven menus.

---

### Controls 🎛️

Built-in controls for filtering, sorting, and interactive state switching.

#### Filter controls
Support multiple filter modes, for example:

- `All`
- `Only Online`
- `Only Favorites`

Each mode can map to its own predicate.

#### Sort controls
Support multiple sort strategies, for example:

- `A–Z`
- `Z–A`

Each mode can map to its own comparator.

#### Control buttons
Render dedicated control items inside the menu.

- Click to cycle modes
- Refresh affected paged areas automatically

---

## Thread Safety Notes ⚠️

When writing populators or data sources, avoid accessing Bukkit/Paper objects off the main thread.

Recommended approach:

- load data asynchronously
- transform it into immutable snapshots, DTOs, or plain lists
- let the menu pipeline render from that snapshot

> Do **not** interact with live Bukkit state asynchronously unless you explicitly know it is safe.

---

## Why NexMenuService? 💡

NexMenuService is built for plugins that need more than static inventories.

It is especially useful when you want:

- reusable menu definitions
- clean separation between data and rendering
- smooth updates with minimal slot changes
- scalable paginated menus
- safer async-first GUI workflows

---

## Summary

With **NexMenuService**, you define menus once and let the service handle rendering, refreshing, diffing, and lifecycle management for you.

The result is a cleaner API, better performance, and a much more maintainable way to build advanced inventory GUIs on Paper/Spigot.