# 🎨 Static Menu System - Guide

## 🎯 What is a Static Menu?

A **Static Menu** displays fixed, non-paginated content. Perfect for:
- ⚙️ **Settings Interfaces** - Configuration menus
- 🎮 **Main Menus** - Home/hub interfaces
- 📋 **Info Displays** - Static information panels
- 🔧 **Crafting Menus** - Fixed slot layouts
- 🛠️ **Admin Panels** - Management interfaces
- 💰 **Currency Display** - Player stats/balance views

---

## ✅ Features

| Feature | Description |
|---------|-------------|
| **Fixed Layout** | Static slot arrangement |
| **Custom Clicks** | Per-slot click handlers |
| **Item Updates** | Dynamically update items |
| **Decorations** | Easy styling with glass panes, etc. |
| **Refresh Interval** | Optional periodic updates |
| **No Pagination** | Simple, straightforward interface |

---

## 🚀 Quick Start: Simple Static Menu

### Step 1: Create a Basic Menu

```java
public class BasicMenuBuilder {
    
    public MenuDefinition createBasicMenu() {
        return MenuDefinition.builder("basic")
            .title("§aBasic Menu")
            .size(27)  // 3 rows
            
            // Define the menu layout
            .populator(ctx -> {
                // Set items in specific slots
                ctx.slot(0).set(MenuItem.of(new ItemStack(Material.APPLE)));
                ctx.slot(4).set(MenuItem.of(new ItemStack(Material.DIAMOND)));
                ctx.slot(8).set(MenuItem.of(new ItemStack(Material.GOLD_INGOT)));
            })
            
            .build();
    }
}
```

---

## 🎨 Static Menu with Advanced Layout

### Step 1: Design the Layout Using Coordinates

```java
public class HubMenuBuilder {
    
    public MenuDefinition createHubMenu() {
        return MenuDefinition.builder("hub")
            .title("§e§lServer Hub")
            .size(45)  // 5 rows
            
            .populator(ctx -> {
                // Add decorative border (row 0 and row 4)
                addBorder(ctx);
                
                // Main menu items (row 1-3)
                addMainItems(ctx);
                
                // Footer info (row 4)
                addFooter(ctx);
            })
            
            .build();
    }
    
    private void addBorder(RenderPopulateContext ctx) {
        ItemStack border = new ItemStack(Material.DARK_OAK_WOOD);
        border.editMeta(meta -> meta.displayName(Component.empty()));
        
        // Top border
        for (int x = 0; x < 9; x++) {
            int slot = SlotIndex.toSlot(x, 0);
            ctx.slot(slot).set(MenuItem.of(border));
        }
        
        // Bottom border
        for (int x = 0; x < 9; x++) {
            int slot = SlotIndex.toSlot(x, 4);
            ctx.slot(slot).set(MenuItem.of(border));
        }
    }
    
    private void addMainItems(RenderPopulateContext ctx) {
        // Shop button - center top
        ItemStack shopIcon = new ItemStack(Material.EMERALD);
        shopIcon.editMeta(meta -> {
            meta.displayName(Component.text("§a§lShop")
                .decoration(TextDecoration.BOLD, true));
            meta.lore(List.of(
                Component.text("§7Click to open shop")
            ));
        });
        
        int shopSlot = SlotIndex.toSlot(1, 2);  // (1, 2)
        ctx.slot(shopSlot).set(MenuItem.of(shopIcon))
            .onClick(clickCtx -> {
                // Handle shop click
                clickCtx.viewer().sendMessage(
                    Component.text("Opening shop...").color(NamedTextColor.GREEN)
                );
                // Open shop menu
            });
        
        // PvP button
        ItemStack pvpIcon = new ItemStack(Material.DIAMOND_SWORD);
        pvpIcon.editMeta(meta -> {
            meta.displayName(Component.text("§c§lPvP Arena")
                .decoration(TextDecoration.BOLD, true));
            meta.lore(List.of(
                Component.text("§7Click to join PvP")
            ));
        });
        
        int pvpSlot = SlotIndex.toSlot(4, 2);  // Center (4, 2)
        ctx.slot(pvpSlot).set(MenuItem.of(pvpIcon))
            .onClick(clickCtx -> {
                // Handle PvP click
                clickCtx.viewer().sendMessage(
                    Component.text("Teleporting to PvP arena...").color(NamedTextColor.RED)
                );
            });
        
        // Games button
        ItemStack gamesIcon = new ItemStack(Material.LEAD);
        gamesIcon.editMeta(meta -> {
            meta.displayName(Component.text("§b§lGames")
                .decoration(TextDecoration.BOLD, true));
            meta.lore(List.of(
                Component.text("§7Click to view games")
            ));
        });
        
        int gamesSlot = SlotIndex.toSlot(7, 2);  // (7, 2)
        ctx.slot(gamesSlot).set(MenuItem.of(gamesIcon))
            .onClick(clickCtx -> {
                // Handle games click
            });
    }
    
    private void addFooter(RenderPopulateContext ctx) {
        ItemStack info = new ItemStack(Material.PAPER);
        info.editMeta(meta -> {
            meta.displayName(Component.text("§6Server Info")
                .color(NamedTextColor.GOLD));
            meta.lore(List.of(
                Component.text("§7Players Online: 42"),
                Component.text("§7Uptime: 5d 3h 21m")
            ));
        });
        
        int infoSlot = SlotIndex.toSlot(4, 4);  // Center bottom
        ctx.slot(infoSlot).set(MenuItem.of(info));
    }
}
```

---

## 🖼️ Layout Patterns

### Pattern 1: 3x3 Grid with Center Focus

```java
public MenuDefinition create3x3GridMenu() {
    return MenuDefinition.builder("grid-3x3")
        .title("§a3x3 Grid")
        .size(27)
        
        .populator(ctx -> {
            // Create a 3x3 grid in the center with decorations around
            ItemStack gridItem = new ItemStack(Material.STONE_BUTTON);
            
            // Top-left corner (0, 0)
            ctx.slot(SlotIndex.toSlot(0, 0)).set(MenuItem.of(gridItem));
            
            // Top-center (1, 0)
            ctx.slot(SlotIndex.toSlot(1, 0)).set(MenuItem.of(gridItem));
            
            // Top-right (2, 0)
            ctx.slot(SlotIndex.toSlot(2, 0)).set(MenuItem.of(gridItem));
            
            // ... and so on for all 9 positions
            
            // Add click handler for each
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    int slot = SlotIndex.toSlot(x, y);
                    ctx.slot(slot).onClick(clickCtx -> {
                        clickCtx.viewer().sendMessage(
                            Component.text("Clicked grid position: " + x + ", " + y)
                        );
                    });
                }
            }
        })
        
        .build();
}
```

### Pattern 2: List View (Vertical)

```java
public MenuDefinition createListViewMenu() {
    return MenuDefinition.builder("list-view")
        .title("§aList View")
        .size(54)  // 6 rows
        
        .populator(ctx -> {
            List<String> items = List.of("Item 1", "Item 2", "Item 3", "Item 4", "Item 5");
            
            int startSlot = SlotIndex.toSlot(1, 1);  // Start at (1, 1)
            
            for (int i = 0; i < items.size(); i++) {
                String itemName = items.get(i);
                int itemSlot = startSlot + (i * 9);  // Each item on a new row
                
                ItemStack stack = new ItemStack(Material.NAME_TAG);
                stack.editMeta(meta -> {
                    meta.displayName(Component.text("§e" + itemName));
                });
                
                ctx.slot(itemSlot).set(MenuItem.of(stack))
                    .onClick(clickCtx -> {
                        clickCtx.viewer().sendMessage(
                            Component.text("You selected: " + itemName)
                        );
                    });
            }
        })
        
        .build();
}
```

### Pattern 3: Form/Input Menu

```java
public MenuDefinition createFormMenu() {
    return MenuDefinition.builder("form")
        .title("§aSettings")
        .size(27)
        
        .populator(ctx -> {
            // Title/Header
            ItemStack header = new ItemStack(Material.PLAYER_HEAD);
            header.editMeta(meta -> {
                meta.displayName(Component.text("§6§lPlayer Settings"));
            });
            ctx.slot(SlotIndex.toSlot(4, 0)).set(MenuItem.of(header));
            
            // Setting 1: PvP Mode
            ItemStack pvpToggle = new ItemStack(Material.REDSTONE);  // ON
            pvpToggle.editMeta(meta -> {
                meta.displayName(Component.text("§aPvP: Enabled"));
                meta.lore(List.of(Component.text("§7Click to toggle")));
            });
            ctx.slot(SlotIndex.toSlot(1, 1)).set(MenuItem.of(pvpToggle))
                .onClick(clickCtx -> togglePvP(clickCtx));
            
            // Setting 2: Notifications
            ItemStack notificationToggle = new ItemStack(Material.BELL);
            notificationToggle.editMeta(meta -> {
                meta.displayName(Component.text("§aNotifications: On"));
                meta.lore(List.of(Component.text("§7Click to toggle")));
            });
            ctx.slot(SlotIndex.toSlot(4, 1)).set(MenuItem.of(notificationToggle))
                .onClick(clickCtx -> toggleNotifications(clickCtx));
            
            // Setting 3: Privacy
            ItemStack privacySettings = new ItemStack(Material.DARK_OAK_DOOR);
            privacySettings.editMeta(meta -> {
                meta.displayName(Component.text("§aPrivacy Settings"));
            });
            ctx.slot(SlotIndex.toSlot(7, 1)).set(MenuItem.of(privacySettings))
                .onClick(clickCtx -> openPrivacyMenu(clickCtx));
            
            // Close button
            ItemStack closeBtn = new ItemStack(Material.BARRIER);
            closeBtn.editMeta(meta -> {
                meta.displayName(Component.text("§c§lClose"));
            });
            ctx.slot(SlotIndex.toSlot(4, 2)).set(MenuItem.of(closeBtn))
                .onClick(clickCtx -> clickCtx.viewer().closeMenu());
        })
        
        .build();
}

private void togglePvP(MenuSlot.MenuClickContext ctx) {
    // Toggle PvP for player
    ctx.viewer().sendMessage(Component.text("PvP toggled!").color(NamedTextColor.GREEN));
}

private void toggleNotifications(MenuSlot.MenuClickContext ctx) {
    // Toggle notifications
    ctx.viewer().sendMessage(Component.text("Notifications toggled!").color(NamedTextColor.GREEN));
}

private void openPrivacyMenu(MenuSlot.MenuClickContext ctx) {
    // Open privacy submenu
}
```

---

## 🔄 Dynamic Updates

### Periodic Refresh

```java
public MenuDefinition createLiveStatsMenu() {
    return MenuDefinition.builder("stats")
        .title("§6§lLive Stats")
        .size(27)
        
        // Refresh every 5 seconds
        .refreshInterval(Duration.ofSeconds(5))
        
        .populator(ctx -> {
            UUID playerId = ctx.viewer().getUniqueId();
            
            // Get live data
            int balance = getPlayerBalance(playerId);
            int kills = getPlayerKills(playerId);
            int deaths = getPlayerDeaths(playerId);
            
            // Display balance
            ItemStack balanceIcon = new ItemStack(Material.GOLD_INGOT);
            balanceIcon.editMeta(meta -> {
                meta.displayName(Component.text("§6Balance")
                    .color(NamedTextColor.GOLD));
                meta.lore(List.of(
                    Component.text("§e$" + balance)
                        .color(NamedTextColor.YELLOW)
                ));
            });
            ctx.slot(0).set(MenuItem.of(balanceIcon));
            
            // Display K/D ratio
            ItemStack statsIcon = new ItemStack(Material.DIAMOND_SWORD);
            statsIcon.editMeta(meta -> {
                meta.displayName(Component.text("§cK/D Stats")
                    .color(NamedTextColor.RED));
                meta.lore(List.of(
                    Component.text("§7Kills: §c" + kills),
                    Component.text("§7Deaths: §c" + deaths)
                ));
            });
            ctx.slot(4).set(MenuItem.of(statsIcon));
        })
        
        .build();
}

private int getPlayerBalance(UUID playerId) {
    // Query from database
    return 0;
}

private int getPlayerKills(UUID playerId) {
    // Query from database
    return 0;
}

private int getPlayerDeaths(UUID playerId) {
    // Query from database
    return 0;
}
```

### On-Demand Update

```java
public class InventoryManager {
    
    private final BukkitMenuService menuService;
    
    public void updatePlayerBalanceDisplay(ViewerRef viewer, int newBalance) {
        BukkitMenuView view = menuService.findOpenView(viewer);
        if (view == null) return;
        
        // Update specific slot without full re-render
        ItemStack balanceIcon = new ItemStack(Material.GOLD_INGOT);
        balanceIcon.editMeta(meta -> {
            meta.displayName(Component.text("§6Balance: $" + newBalance)
                .color(NamedTextColor.GOLD));
        });
        
        view.updateSlot(0, MenuItem.of(balanceIcon));
    }
}
```

---

## 💡 Pro-Tips

1. **Use Grid Coordinates** - Always think in (x, y) coordinates, not slot numbers
2. **Add Visual Hierarchy** - Use colors and materials to guide the eye
3. **Provide Feedback** - Use click handlers to confirm player actions
4. **Consider Accessibility** - Use clear item names and lore
5. **Optimize Updates** - Use refresh intervals wisely
6. **Test Layout** - Always test with different display resolutions
7. **Add Decorations** - Use glass panes, wool, etc. for visual separation
8. **Use Grid Overlay** - Mentally divide menu into regions

---

## 📐 Common Dimensions

| Use Case | Rows | Columns | Slots |
|----------|------|---------|-------|
| Small Menu | 3 | 9 | 27 |
| Medium Menu | 4 | 9 | 36 |
| Large Menu | 5 | 9 | 45 |
| Full Screen | 6 | 9 | 54 |

---

## 🎨 Styling Tips

```java
// Decorative glass panes for borders
ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE);
glass.editMeta(meta -> meta.displayName(Component.empty()));

// Button-like appearance
ItemStack button = new ItemStack(Material.STONE_BUTTON);
button.editMeta(meta -> {
    meta.displayName(Component.text("§eClick Me"));
});

// Separator
ItemStack separator = new ItemStack(Material.STRUCTURE_VOID);
separator.editMeta(meta -> meta.displayName(Component.empty()));

// Highlight
ItemStack highlight = new ItemStack(Material.GOLD_BLOCK);
```

---

## 🔗 Important Classes

| Class | Purpose |
|-------|---------|
| `MenuDefinition` | Defines the menu |
| `MenuDefinitionBuilder` | Fluent API for building |
| `RenderPopulateContext` | Context during population |
| `MenuSlot` | Slot interface with click handler |
| `MenuItem` | Immutable item wrapper |
| `SlotIndex` | Convert between coordinates and slots |
| `DepositSlotMapper` | Helper for coordinate areas |

---

## 📞 Support

For more details, check:
- `MenuDefinition.java` - Menu API
- `MenuSlot.java` - Slot interaction
- `RenderPopulateContext.java` - Population context
- `SlotIndex.java` - Coordinate system

