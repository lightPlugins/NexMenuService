# 📚 NexMenuService Guides

Welcome to the comprehensive guide collection for the **NexMenuService** menu API! These guides cover everything you need to build amazing inventory menus.

---

## 📖 Available Guides

### 🎁 [Deposit/Drag-and-Drop System](./DEPOSIT_SYSTEM_GUIDE.md)
**Perfect for: Backpacks, Traders, Storage systems**

Learn how to implement drag-and-drop item mechanics. Players can drop items from their inventory into menu slots with full validation and tracking.

**Key Features:**
- ✅ Shift-Click deposits
- ✅ Drag & drop support
- ✅ Item validation via DepositHandler
- ✅ Item tracking with DepositLedger
- ✅ Coordinate-based slot definition with DepositSlotMapper

**Quick Example:**
```java
Set<Integer> slots = DepositSlotMapper.areaToSlots(0, 0, 9, 3);
DepositPolicy policy = DepositPolicies.slots(slots, DepositReturnStrategy.INVENTORY_THEN_DROP);
```

---

### 📄 [Static Menu System](./STATIC_MENU_GUIDE.md)
**Perfect for: Hubs, Settings, Info panels, Crafting**

Learn how to create fixed, non-paginated menus with custom layouts and click handlers.

**Key Features:**
- ✅ Fixed slot arrangement
- ✅ Custom per-slot click handlers
- ✅ Decorative layouts
- ✅ Dynamic item updates
- ✅ Optional periodic refresh

**Quick Example:**
```java
MenuDefinition.builder("hub")
    .size(27)
    .populator(ctx -> {
        ctx.slot(4).set(MenuItem.of(new ItemStack(Material.DIAMOND)))
            .onClick(clickCtx -> handleDiamondClick(clickCtx));
    })
    .build();
```

---

### 📑 [Paged Menu System](./PAGED_SYSTEM_GUIDE.md)
**Perfect for: Shops, Item lists, Leaderboards, Browse interfaces**

Learn how to paginate through large amounts of content with navigation buttons.

**Key Features:**
- ✅ Automatic pagination
- ✅ Navigation buttons (Previous, Next, Refresh)
- ✅ Custom item rendering
- ✅ Multiple paged areas in one menu
- ✅ Coordinate-based page bounds

**Quick Example:**
```java
PagedAreaDefinition<ShopItem> pageArea = new PagedAreaDefinition<>(
    "shop",
    new PageBounds(0, 0, 3, 3, PageAlignment.LEFT),
    new ShopDataSource(),
    new ShopItemRenderer(),
    new PageNavigation(OptionalInt.of(27), OptionalInt.of(35), OptionalInt.empty())
);
```

---

### 🔍 [Paged Menu with Filtering & Sorting](./PAGED_FILTERING_GUIDE.md)
**Perfect for: Advanced shops, Search interfaces, Customizable lists**

Learn how to add filtering and sorting controls to paged menus.

**Key Features:**
- ✅ Real-time filtering
- ✅ Multiple sort options
- ✅ Control buttons for filter selection
- ✅ Combined multi-filter support
- ✅ State persistence across page changes

**Quick Example:**
```java
public class CategoryFilterControl implements PageControlButton {
    @Override
    public void onClick(ClickContext ctx) {
        ctx.stateStore().set("shop", "category", "weapons");
        ctx.view().requestRender(RenderReason.PAGE_CHANGED);
    }
}
```

---

## 🎯 Choosing the Right System

```
┌─────────────────────────────────────┐
│  What do you want to build?         │
└─────────────────────────────────────┘
              │
    ┌─────────┼─────────┬──────────────┐
    │         │         │              │
    ▼         ▼         ▼              ▼
 Fixed?    Paginate?  Drag-Drop?   Filter?
   │         │          │            │
   ▼         ▼          ▼            ▼
STATIC    PAGED      DEPOSIT      FILTERED
```

---

## 📊 System Comparison

| Feature | Static | Paged | Deposit | Filtered |
|---------|--------|-------|---------|----------|
| Fixed Layout | ✅ | ✅ | ✅ | ✅ |
| Large Data | ❌ | ✅ | ✅ | ✅ |
| Pagination | ❌ | ✅ | ❌ | ✅ |
| Drag & Drop | ❌ | ❌ | ✅ | ❌ |
| Filtering | ❌ | ❌ | ❌ | ✅ |
| Sorting | ❌ | ❌ | ❌ | ✅ |
| Item Validation | ❌ | ❌ | ✅ | ❌ |

---

## 🚀 Quick Start Examples

### Example 1: Simple Hub Menu
```java
// See: STATIC_MENU_GUIDE.md - Hub Menu Example

MenuDefinition hub = MenuDefinition.builder("hub")
    .size(27)
    .title("§e§lServer Hub")
    .populator(ctx -> {
        ctx.slot(4).set(createShopButton())
            .onClick(clickCtx -> openShop(clickCtx));
    })
    .build();
```

### Example 2: Item Shop
```java
// See: PAGED_SYSTEM_GUIDE.md - Item Shop Example

PagedAreaDefinition<ShopItem> pageArea = new PagedAreaDefinition<>(
    "shop",
    new PageBounds(0, 0, 3, 3, PageAlignment.LEFT),
    new ShopDataSource(),
    new ShopItemRenderer(),
    nav
);

MenuDefinition shop = MenuDefinition.builder("shop")
    .addPagedArea(pageArea)
    .build();
```

### Example 3: Backpack
```java
// See: DEPOSIT_SYSTEM_GUIDE.md - Backpack Plugin Example

Set<Integer> backpackSlots = DepositSlotMapper.areaToSlots(0, 0, 9, 3);
DepositPolicy policy = DepositPolicies.slots(backpackSlots, DepositReturnStrategy.INVENTORY_THEN_DROP);

MenuDefinition backpack = MenuDefinition.builder("backpack")
    .interactionPolicy(InteractionPolicies.deposits(policy))
    .depositHandler(new BackpackDepositHandler())
    .build();
```

### Example 4: Filtered Shop
```java
// See: PAGED_FILTERING_GUIDE.md - Filtered Shop Example

MenuDefinition filteredShop = MenuDefinition.builder("filtered-shop")
    .addPagedArea(pageArea)
    .addPageControlButton(new CategoryFilterControl("weapons"))
    .addPageControlButton(new SortButtonControl())
    .build();
```

---

## 🎨 Layout & Coordinates

All guides use the **coordinate system**:

```
X (column):  0 1 2 3 4 5 6 7 8
Y (row):     ╔═════════════════╗
      0      ║ │ │ │ │ │ │ │ │ ║
      1      ║ │ │ │ │ │ │ │ │ ║
      2      ║ │ │ │ │ │ │ │ │ ║
      3      ║ │ │ │ │ │ │ │ │ ║
      4      ║ │ │ │ │ │ │ │ │ ║
      5      ║ │ │ │ │ │ │ │ │ ║
             ╚═════════════════╝
```

**Convert to slot number:**
```java
int slot = SlotIndex.toSlot(x, y);  // (2, 3) → slot 21
```

**Define areas with coordinates:**
```java
// 3x3 area at (0, 0)
Set<Integer> slots = DepositSlotMapper.areaToSlots(0, 0, 3, 3);
```

---

## 🔧 Common Patterns

### Pattern: Combining Systems
```java
// Static menu with deposit area
MenuDefinition.builder("storage")
    .size(36)
    .populator(ctx -> {
        // Add static header
        ctx.slot(4).set(createHeader());
    })
    .interactionPolicy(InteractionPolicies.deposits(policy))
    .depositHandler(handler)
    .build();
```

### Pattern: Nested Menus
```java
// Click in menu A opens menu B
ctx.slot(10).onClick(clickCtx -> {
    menuService.open(clickCtx.viewer(), menuB);
});
```

### Pattern: Dynamic Updates
```java
// Refresh every 5 seconds
MenuDefinition.builder("live")
    .refreshInterval(Duration.ofSeconds(5))
    .populator(ctx -> {
        // Gets called every 5 seconds
        ctx.slot(0).set(getLiveData());
    })
    .build();
```

---

## 📚 Additional Resources

### Core Classes Reference
- `MenuDefinition` - Main menu definition
- `MenuDefinitionBuilder` - Fluent builder API
- `MenuItem` - Immutable item wrapper
- `MenuSlot` - Slot interface with click handler
- `RenderPopulateContext` - Population context
- `SlotIndex` - Coordinate ↔ Slot conversion
- `DepositSlotMapper` - Coordinate-based slot definition

### Data Structures
- `PageBounds` - Page area rectangle
- `PageBounds` - Navigation button configuration
- `PageSource<T>` - Data loading interface
- `PageItemRenderer<T>` - Item rendering
- `PageClickHandler<T>` - Item click handling
- `DepositPolicy` - Deposit configuration
- `DepositHandler` - Deposit validation

### API Entry Points
- `BukkitMenuService.open(ViewerRef, MenuDefinition)` - Open menu
- `BukkitMenuView.updateSlot(int, MenuItem)` - Update slot
- `BukkitMenuView.requestRender(RenderReason)` - Request render
- `PageControlStateStore` - Store filter/sort state

---

## 💡 Tips & Best Practices

1. **Think in Coordinates** - Always plan menus using x/y coordinates
2. **Use DepositSlotMapper** - Define areas clearly and readably
3. **Cache Data** - Avoid unnecessary database queries in populators
4. **Test Layouts** - Verify menu appearance at different scales
5. **Provide Feedback** - Use messages/sounds to confirm player actions
6. **Organize Code** - Separate UI (menus) from business logic
7. **Use Named Areas** - Give paged areas meaningful IDs
8. **Document Complex Logic** - Explain custom renderers/handlers

---

## 🤝 Contributing

Have questions or found an issue? Check the main [README.md](../README.md)

---

## 📞 Support

Each guide includes:
- ✅ Quick start examples
- ✅ Advanced scenarios
- ✅ Common patterns
- ✅ Pro-tips
- ✅ API reference

**Start with the guide that matches your use case!**

