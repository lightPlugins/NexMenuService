# 📖 Paged Menu System - Guide

## 🎯 What is a Paged Menu?

A **Paged Menu** displays dynamic content across multiple pages. Perfect for:
- 📋 **Item Lists** - Paginate through items in a list
- 🏪 **Shop Interfaces** - Show products page by page
- 👥 **Player Lists** - Browse through online players
- 🎁 **Rewards Display** - Show available rewards with pagination

---

## ✅ Features

| Feature | Description |
|---------|-------------|
| **Automatic Pagination** | Content split into pages automatically |
| **Navigation Buttons** | Previous, Next, and Refresh buttons |
| **Item Rendering** | Custom renderer for each item |
| **Page Navigation** | Seamless page switching |
| **Click Handlers** | Custom click logic per item |
| **Coordinates Support** | Define page areas using x/y coordinates |
| **Page State** | Remember current page index |

---

## 🚀 Quick Start: Paginated Item Shop

### Step 1: Create a Data Source

```java
public class ItemShopSource implements PageSource<ShopItem> {
    
    private final Database database;
    
    @Override
    public List<ShopItem> load(MenuKey menuKey, ViewerRef viewer) {
        // Load items from database
        UUID playerId = viewer.getUniqueId();
        return database.loadShopItems(playerId);
    }
}

record ShopItem(int id, String name, Material material, int price) {}
```

### Step 2: Create an Item Renderer

```java
public class ItemShopRenderer implements PageItemRenderer<ShopItem> {
    
    @Override
    public MenuItemSupplier render(ShopItem item, int index) {
        return ctx -> {
            ItemStack stack = new ItemStack(item.material());
            stack.editMeta(meta -> {
                meta.displayName(Component.text(item.name())
                    .color(NamedTextColor.GOLD));
                
                meta.lore(List.of(
                    Component.text("Price: $" + item.price())
                        .color(NamedTextColor.GRAY),
                    Component.text("Click to buy")
                        .color(NamedTextColor.YELLOW)
                ));
            });
            
            return MenuItem.of(stack);
        };
    }
}
```

### Step 3: Define Page Area with Coordinates

```java
// Define a 3x3 page area at coordinates (0, 0)
PageBounds bounds = new PageBounds(
    0, 0,           // x, y (top-left corner)
    3, 3,           // width, height (3 columns x 3 rows = 9 items per page)
    PageAlignment.LEFT
);

// Define navigation buttons
PageNavigation nav = new PageNavigation(
    OptionalInt.of(SlotIndex.toSlot(0, 4)),  // Previous button at (0, 4)
    OptionalInt.of(SlotIndex.toSlot(8, 4)),  // Next button at (8, 4)
    OptionalInt.of(SlotIndex.toSlot(4, 4))   // Refresh button at (4, 4)
);

// Create the paged area
PagedAreaDefinition<ShopItem> pageArea = new PagedAreaDefinition<>(
    "shop",                              // Unique area ID
    bounds,
    new ItemShopSource(),                // Data source
    new ItemShopRenderer(),              // Item renderer
    nav
);
```

### Step 4: Add Click Handler (Optional)

```java
PageClickHandler<ShopItem> clickHandler = (menuKey, viewer, item, slot, interaction) -> {
    // Handle item click
    if (interaction.clickAction() == ClickAction.LEFT_CLICK) {
        // Buy item
        int playerId = viewer.getUniqueId();
        database.purchaseItem(playerId, item.id(), item.price());
        
        viewer.sendMessage(Component.text("Purchased: " + item.name())
            .color(NamedTextColor.GREEN));
    }
};

// Create paged area with click handler
PagedAreaDefinition<ShopItem> pageArea = new PagedAreaDefinition<>(
    "shop",
    bounds,
    new ItemShopSource(),
    new ItemShopRenderer(),
    nav,
    Optional.of(clickHandler)
);
```

### Step 5: Build the Menu

```java
public class ShopMenuBuilder {
    
    private final BukkitMenuService menuService;
    private final Database database;
    
    public MenuDefinition createShopMenu() {
        PagedAreaDefinition<ShopItem> pageArea = new PagedAreaDefinition<>(
            "shop",
            new PageBounds(0, 0, 3, 3, PageAlignment.LEFT),
            new ItemShopSource(),
            new ItemShopRenderer(),
            new PageNavigation(
                OptionalInt.of(27),
                OptionalInt.of(35),
                OptionalInt.of(31)
            )
        );
        
        return MenuDefinition.builder("shop")
            .title("§aItem Shop")
            .size(54) // 6 rows for shop + buttons
            
            // Add the paged area
            .addPagedArea(pageArea)
            
            // Optional: Add decorations
            .populator(ctx -> {
                // Add background/decorative items
                for (int i = 36; i < 45; i++) {
                    ctx.slot(i).set(MenuItem.of(new ItemStack(Material.BLACK_STAINED_GLASS_PANE)));
                }
            })
            
            .build();
    }
}
```

### Step 6: Open the Menu

```java
BukkitPlayer player = /* player */;
MenuDefinition shopMenu = shopMenuBuilder.createShopMenu();

menuService.open(ViewerRef.of(player), shopMenu);
```

---

## 📚 Advanced Features

### Using DepositSlotMapper for Page Bounds

```java
// Instead of manual coordinates:
PageBounds bounds = new PageBounds(0, 0, 3, 3, PageAlignment.LEFT);

// Use DepositSlotMapper to define the area:
Set<Integer> pageSlots = DepositSlotMapper.areaToSlots(0, 0, 3, 3);
int width = 3;
int height = 3;

PageBounds bounds = new PageBounds(0, 0, width, height, PageAlignment.LEFT);
```

### Centered Page Layout

```java
// Center the items within the page area
PageBounds bounds = new PageBounds(
    1, 0,           // Start at (1, 0)
    7, 3,           // 7 columns x 3 rows
    PageAlignment.CENTER  // Center items within this area
);
```

### Right-Aligned Layout

```java
PageBounds bounds = new PageBounds(
    0, 0,
    9, 3,
    PageAlignment.RIGHT  // Align items to the right
);
```

### Different Page Sizes

```java
// Small page: 2 items per row, 2 rows = 4 items per page
PageBounds smallPage = new PageBounds(3, 0, 2, 2, PageAlignment.CENTER);

// Medium page: 3 items per row, 3 rows = 9 items per page
PageBounds mediumPage = new PageBounds(0, 0, 3, 3, PageAlignment.LEFT);

// Large page: 5 items per row, 5 rows = 25 items per page
PageBounds largePage = new PageBounds(0, 0, 5, 5, PageAlignment.LEFT);
```

---

## 🎯 Navigation Button Placement

```java
// Using SlotIndex for precise coordinate-based positioning
int previousSlot = SlotIndex.toSlot(0, 4);  // x=0, y=4
int nextSlot = SlotIndex.toSlot(8, 4);      // x=8, y=4
int refreshSlot = SlotIndex.toSlot(4, 4);   // x=4, y=4 (center)

PageNavigation nav = new PageNavigation(
    OptionalInt.of(previousSlot),
    OptionalInt.of(nextSlot),
    OptionalInt.of(refreshSlot)
);

// Or: disable certain buttons
PageNavigation withoutRefresh = new PageNavigation(
    OptionalInt.of(previousSlot),
    OptionalInt.of(nextSlot),
    OptionalInt.empty()  // No refresh button
);
```

---

## 🔄 Multiple Paged Areas

```java
// Create two different paged areas in one menu
PagedAreaDefinition<ShopItem> topArea = new PagedAreaDefinition<>(
    "shop-top",
    new PageBounds(0, 0, 9, 2, PageAlignment.LEFT),
    new ItemShopSource(),
    new ItemShopRenderer(),
    PageNavigation.none()
);

PagedAreaDefinition<Equipment> bottomArea = new PagedAreaDefinition<>(
    "shop-bottom",
    new PageBounds(0, 3, 9, 2, PageAlignment.LEFT),
    new EquipmentSource(),
    new EquipmentRenderer(),
    new PageNavigation(OptionalInt.of(27), OptionalInt.of(35), OptionalInt.empty())
);

MenuDefinition menu = MenuDefinition.builder("dual-shop")
    .title("§aShop")
    .size(45)
    .addPagedArea(topArea)
    .addPagedArea(bottomArea)
    .build();
```

---

## 💡 Pro-Tips

1. **Use PageSource Caching** - Cache data to avoid repeated database queries
2. **Lazy Load Data** - Only load data when page is viewed
3. **Track Page State** - Current page index is automatically saved per area ID
4. **Combine with Deposits** - Mix paged items with deposit zones
5. **Custom Navigation Buttons** - Override button appearance and behavior
6. **Empty State Handling** - Show placeholder when no items available

---

## 🔗 Important Classes

| Class | Purpose |
|-------|---------|
| `PagedAreaDefinition<T>` | Defines a paged content area |
| `PageSource<T>` | Loads data for a page area |
| `PageItemRenderer<T>` | Renders individual items |
| `PageBounds` | Defines page area bounds |
| `PageNavigation` | Defines navigation button locations |
| `PageClickHandler<T>` | Handles item clicks |
| `PageModel` | Paging calculations |
| `PageRenderer` | Renders page content |
| `DepositSlotMapper` | Helper for coordinate-based bounds |

---

## 📊 Page Model Calculations

```java
// Get paging information
PageModel model = new PageModel(bounds);

int capacity = model.capacity();              // Items per page
int pageCount = model.pageCountFor(100);      // Total pages for 100 items
int startIndex = model.startIndex(0);         // First item index on page 0
int endIndex = model.endExclusiveIndex(0, 100); // Last item index on page 0
```

---

## 📞 Support

Check these files for more details:
- `PagedAreaDefinition.java` - Area definition API
- `PageSource.java` - Data loading interface
- `PageItemRenderer.java` - Item rendering interface
- `PageNavigation.java` - Navigation configuration

