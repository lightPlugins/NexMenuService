# 🔍 Paged Menu with Sorting & Filtering - Guide

## 🎯 What is a Filtered/Sorted Paged Menu?

A **Filtered & Sorted Paged Menu** allows dynamic manipulation of page data before display. Perfect for:
- 🔎 **Search & Filter** - Filter items by type/rarity
- 📊 **Sorting** - Sort by price, name, quantity, etc.
- 🎯 **Advanced Search** - Multiple filters combined
- 🏆 **Leaderboards** - Sort players by rank/score
- 💰 **Price Lists** - Filter by category, sort by price

---

## ✅ Features

| Feature | Description |
|---------|-------------|
| **Real-time Filtering** | Filter items instantly |
| **Multiple Sort Options** | Sort ascending/descending |
| **Combined Filters** | Apply multiple filters at once |
| **Control Buttons** | UI buttons for filter/sort selection |
| **Page Caching** | Smart caching of filtered results |
| **State Persistence** | Remember active filters across page changes |

---

## 🚀 Quick Start: Filtered Shop with Sorting

### Step 1: Create Filterable Data Source

```java
public class FilteredShopSource implements PageSource<ShopItem> {
    
    private final Database database;
    private final PageControlStateStore stateStore;
    
    @Override
    public List<ShopItem> load(MenuKey menuKey, ViewerRef viewer) {
        UUID playerId = viewer.getUniqueId();
        
        // Load all items from database
        List<ShopItem> allItems = database.loadShopItems(playerId);
        
        // Apply filters if they exist in state
        allItems = applyFilters(allItems);
        
        // Apply sorting if configured
        allItems = applySorting(allItems);
        
        return allItems;
    }
    
    private List<ShopItem> applyFilters(List<ShopItem> items) {
        // Filter logic (see examples below)
        return items;
    }
    
    private List<ShopItem> applySorting(List<ShopItem> items) {
        // Sorting logic (see examples below)
        return items;
    }
}

record ShopItem(int id, String name, Material material, int price, String category) {}
```

### Step 2: Category Filter Control

```java
public class CategoryFilterControl implements PageControlButton {
    
    private final String categoryId;  // "weapons", "armor", "consumables"
    
    @Override
    public String areaId() {
        return "shop";  // Target paged area
    }
    
    @Override
    public String controlId() {
        return "category-filter";
    }
    
    @Override
    public int slot() {
        return SlotIndex.toSlot(1, 5);  // Row 5, Column 1
    }
    
    @Override
    public MenuItem render(RenderContext ctx) {
        ItemStack icon;
        String displayName;
        
        // Determine if this category is active
        boolean isActive = isActiveCategoryFilter(ctx.stateStore());
        
        if (categoryId.equals("weapons")) {
            icon = new ItemStack(Material.DIAMOND_SWORD);
            displayName = isActive ? "§e§lWeapons (Active)" : "§7Weapons";
        } else if (categoryId.equals("armor")) {
            icon = new ItemStack(Material.DIAMOND_CHESTPLATE);
            displayName = isActive ? "§e§lArmor (Active)" : "§7Armor";
        } else {
            icon = new ItemStack(Material.POTION);
            displayName = isActive ? "§e§lConsumables (Active)" : "§7Consumables";
        }
        
        icon.editMeta(meta -> meta.displayName(Component.text(displayName)));
        return MenuItem.of(icon);
    }
    
    @Override
    public void onClick(ClickContext ctx) {
        // Toggle or set the filter
        if (isActiveCategoryFilter(ctx.stateStore())) {
            // Remove filter
            ctx.stateStore().set(areaId(), controlId(), null);
        } else {
            // Set filter
            ctx.stateStore().set(areaId(), controlId(), categoryId);
        }
        
        // Reset to page 0 when filter changes
        ctx.stateStore().pageState().reset(areaId());
        
        // Request re-render
        ctx.view().requestRender(RenderReason.PAGE_CHANGED);
    }
    
    private boolean isActiveCategoryFilter(PageControlStateStore store) {
        return categoryId.equals(store.get(areaId(), controlId()));
    }
}
```

### Step 3: Sort Button Control

```java
public class SortButtonControl implements PageControlButton {
    
    private enum SortOption {
        PRICE_ASC("price-asc", "Price: Low → High", Material.GOLD_INGOT),
        PRICE_DESC("price-desc", "Price: High → Low", Material.GOLD_BLOCK),
        NAME_ASC("name-asc", "Name: A → Z", Material.OAK_SIGN),
        NAME_DESC("name-desc", "Name: Z → A", Material.BIRCH_SIGN);
        
        final String id;
        final String display;
        final Material icon;
        
        SortOption(String id, String display, Material icon) {
            this.id = id;
            this.display = display;
            this.icon = icon;
        }
    }
    
    private final SortOption sortOption;
    
    @Override
    public String areaId() {
        return "shop";
    }
    
    @Override
    public String controlId() {
        return "sort-control";
    }
    
    @Override
    public int slot() {
        return SlotIndex.toSlot(7, 5);  // Row 5, Column 7
    }
    
    @Override
    public MenuItem render(RenderContext ctx) {
        String currentSort = ctx.stateStore().get(areaId(), controlId());
        boolean isActive = sortOption.id.equals(currentSort);
        
        ItemStack icon = new ItemStack(sortOption.icon);
        icon.editMeta(meta -> {
            String name = isActive ? "§e§l" + sortOption.display : "§7" + sortOption.display;
            meta.displayName(Component.text(name));
        });
        
        return MenuItem.of(icon);
    }
    
    @Override
    public void onClick(ClickContext ctx) {
        // Set the sort option
        ctx.stateStore().set(areaId(), controlId(), sortOption.id);
        
        // Reset to page 0
        ctx.stateStore().pageState().reset(areaId());
        
        // Re-render
        ctx.view().requestRender(RenderReason.PAGE_CHANGED);
    }
}
```

### Step 4: Enhanced Data Source with Filtering

```java
public class FilteredShopSourceAdvanced implements PageSource<ShopItem> {
    
    private final Database database;
    
    @Override
    public List<ShopItem> load(MenuKey menuKey, ViewerRef viewer) {
        UUID playerId = viewer.getUniqueId();
        List<ShopItem> allItems = database.loadShopItems(playerId);
        
        return allItems;
    }
}

// Helper to apply filters in the render pipeline
public class ShopFilterHelper {
    
    public static List<ShopItem> applyFiltersAndSort(
        List<ShopItem> items,
        PageControlStateStore stateStore,
        String areaId
    ) {
        // Get active filters from state
        String categoryFilter = stateStore.get(areaId, "category-filter");
        String sortOption = stateStore.get(areaId, "sort-control");
        
        // Apply category filter
        if (categoryFilter != null) {
            items = items.stream()
                .filter(item -> categoryFilter.equals(item.category()))
                .toList();
        }
        
        // Apply sorting
        if (sortOption != null) {
            items = switch (sortOption) {
                case "price-asc" -> items.stream()
                    .sorted(Comparator.comparingInt(ShopItem::price))
                    .toList();
                case "price-desc" -> items.stream()
                    .sorted(Comparator.comparingInt(ShopItem::price).reversed())
                    .toList();
                case "name-asc" -> items.stream()
                    .sorted(Comparator.comparing(ShopItem::name))
                    .toList();
                case "name-desc" -> items.stream()
                    .sorted(Comparator.comparing(ShopItem::name).reversed())
                    .toList();
                default -> items;
            };
        }
        
        return items;
    }
}
```

### Step 5: Build the Filtered Menu

```java
public class FilteredShopMenuBuilder {
    
    private final BukkitMenuService menuService;
    private final Database database;
    
    public MenuDefinition createFilteredShopMenu() {
        // Paged area for items
        PagedAreaDefinition<ShopItem> pageArea = new PagedAreaDefinition<>(
            "shop",
            new PageBounds(0, 0, 3, 4, PageAlignment.LEFT),  // 3x4 = 12 items per page
            new FilteredShopSource(database),
            new ItemShopRenderer(),
            new PageNavigation(
                OptionalInt.of(SlotIndex.toSlot(0, 5)),  // Previous
                OptionalInt.of(SlotIndex.toSlot(8, 5)),  // Next
                OptionalInt.empty()                        // No refresh
            )
        );
        
        return MenuDefinition.builder("filtered-shop")
            .title("§aFiltered Shop")
            .size(54)  // 6 rows
            
            // Add the paged area
            .addPagedArea(pageArea)
            
            // Add filter/sort control buttons
            .addPageControlButton(new CategoryFilterControl("weapons"))
            .addPageControlButton(new CategoryFilterControl("armor"))
            .addPageControlButton(new CategoryFilterControl("consumables"))
            .addPageControlButton(new SortButtonControl(/* price-asc */))
            .addPageControlButton(new SortButtonControl(/* price-desc */))
            .addPageControlButton(new SortButtonControl(/* name-asc */))
            .addPageControlButton(new SortButtonControl(/* name-desc */))
            
            // Populator for layout
            .populator(ctx -> {
                // Add header/footer decorations
                for (int i = 36; i < 54; i++) {
                    ctx.slot(i).set(MenuItem.of(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
                }
            })
            
            .build();
    }
}
```

---

## 📚 Advanced Filtering Scenarios

### Scenario 1: Multi-Filter with Search Box

```java
public class TextSearchControl implements PageControlButton {
    
    private final String searchText;
    
    @Override
    public void onClick(ClickContext ctx) {
        // Open an anvil GUI to get search input
        // Then store in state store:
        ctx.stateStore().set("shop", "search-text", searchText);
        ctx.stateStore().pageState().reset("shop");
        ctx.view().requestRender(RenderReason.PAGE_CHANGED);
    }
}

// In data source:
String searchText = stateStore.get("shop", "search-text");
if (searchText != null) {
    items = items.stream()
        .filter(item -> item.name().toLowerCase().contains(searchText.toLowerCase()))
        .toList();
}
```

### Scenario 2: Rarity Filter

```java
public enum ItemRarity {
    COMMON, UNCOMMON, RARE, EPIC, LEGENDARY
}

public class RarityFilterControl implements PageControlButton {
    
    private final ItemRarity rarity;
    
    @Override
    public void onClick(ClickContext ctx) {
        ctx.stateStore().set("shop", "rarity-filter", rarity.name());
        ctx.stateStore().pageState().reset("shop");
        ctx.view().requestRender(RenderReason.PAGE_CHANGED);
    }
}

// In data source:
String rarityFilter = stateStore.get("shop", "rarity-filter");
if (rarityFilter != null) {
    items = items.stream()
        .filter(item -> rarityFilter.equals(item.getRarity().name()))
        .toList();
}
```

### Scenario 3: Range Filter (Min-Max Price)

```java
public class PriceRangeFilter {
    
    public static List<ShopItem> filterByPrice(
        List<ShopItem> items,
        int minPrice,
        int maxPrice
    ) {
        return items.stream()
            .filter(item -> item.price() >= minPrice && item.price() <= maxPrice)
            .toList();
    }
}

// Usage:
String priceRange = stateStore.get("shop", "price-range");  // "0-500"
if (priceRange != null) {
    String[] parts = priceRange.split("-");
    int min = Integer.parseInt(parts[0]);
    int max = Integer.parseInt(parts[1]);
    items = PriceRangeFilter.filterByPrice(items, min, max);
}
```

### Scenario 4: Availability Filter

```java
public class AvailabilityControl implements PageControlButton {
    
    @Override
    public void onClick(ClickContext ctx) {
        String current = ctx.stateStore().get("shop", "availability");
        String next = current == null || current.equals("all") ? "available" : "all";
        ctx.stateStore().set("shop", "availability", next);
        ctx.stateStore().pageState().reset("shop");
        ctx.view().requestRender(RenderReason.PAGE_CHANGED);
    }
}

// In data source:
String availability = stateStore.get("shop", "availability");
if ("available".equals(availability)) {
    items = items.stream()
        .filter(item -> item.inStock() > 0)
        .toList();
}
```

---

## 💡 Pro-Tips

1. **Reset Page on Filter Change** - Always reset page to 0 when filters change
2. **Cache Filtered Results** - Cache the filtered list to avoid repeated filtering
3. **Show Filter Count** - Display how many items match the current filters
4. **Persist Filters** - Store filter preferences in database
5. **Combine Filters Efficiently** - Use streams for chained filtering
6. **Visual Feedback** - Show active filters with color/glow effects
7. **Keyboard Support** - Allow shortcuts for common filters
8. **Default Filters** - Set sensible defaults on first visit

---

## 🎨 Filter Display Ideas

```java
// Show active filters as a summary
public String getFilterSummary(PageControlStateStore store, String areaId) {
    StringBuilder summary = new StringBuilder("Active: ");
    
    String category = store.get(areaId, "category-filter");
    if (category != null) {
        summary.append(capitalize(category)).append(" | ");
    }
    
    String sort = store.get(areaId, "sort-control");
    if (sort != null) {
        summary.append("Sorted: ").append(capitalize(sort));
    }
    
    return summary.toString();
}

// Display in menu title
menuService.updateTitle(viewerRef, "§aShop - " + getFilterSummary(...));
```

---

## 🔗 Important Classes

| Class | Purpose |
|-------|---------|
| `PageControlButton` | Filter/sort button implementation |
| `PageControlStateStore` | Stores filter/sort state |
| `PageSource<T>` | Apply filters here |
| `Comparator<T>` | For sorting items |

---

## 📞 Support

Combine the concepts from:
- `PAGED_SYSTEM_GUIDE.md` - Base paging concepts
- `PageControlButton.java` - Control button API
- `PageControlStateStore.java` - State management

