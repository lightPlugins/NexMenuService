# 📦 Deposit/Drag-and-Drop System - Guide

## 🎯 What is the Deposit System?

The **Deposit System** allows players to drag and drop items directly from their inventory into defined menu areas. Perfect for:
- 🎒 **Backpack Plugins** - Drag items into a container
- 🏪 **Trading Systems** - Drop items for selling
- 📦 **Storage Interfaces** - Sort and store items
- 🔧 **Crafting Menus** - Drop materials for crafting

## ✅ Features

| Feature | Description |
|---------|-------------|
| **Shift-Click Deposit** | Shift-click from inventory → menu slots |
| **Drag & Drop** | Drag multiple items at once |
| **Cursor Placement** | Left/right click to place/remove |
| **Number-Key Support** | Move hotbar 0-8 to slots |
| **Offhand Support** | Put offhand item in slots |
| **Stack-Merging** | Automatically combine items |
| **Partial Transfer** | If slot is full, remainder stays in cursor |
| **Item-Validation** | Custom rules via `DepositHandler` |
| **Item-Tracking** | Save/load via `DepositLedger` |
| **Auto-Return** | Automatically return items on menu close |

---

## 🚀 Quick Start: Backpack Plugin

### Option A: Define Slots by Coordinates (Recommended)

```java
// Define a 3x3 deposit area at coordinates (0, 0)
// Much more intuitive than manual slot numbers!
Set<Integer> backpackSlots = DepositSlotMapper.areaToSlots(0, 0, 3, 3);
// Results in slots: 0, 1, 2, 9, 10, 11, 18, 19, 20

// Create DepositPolicy
DepositPolicy policy = DepositPolicies.slots(
    backpackSlots,
    DepositReturnStrategy.INVENTORY_THEN_DROP
);
```

### Option B: Multiple Deposit Areas

```java
// Combine multiple areas (left side + right side)
Set<Integer> depositSlots = DepositSlotMapper.multiAreaToSlots(
    0, 0, 3, 3,  // Left side: 3x3 area at (0, 0)
    6, 0, 3, 3   // Right side: 3x3 area at (6, 0)
);

DepositPolicy policy = DepositPolicies.slots(
    depositSlots,
    DepositReturnStrategy.INVENTORY_THEN_DROP
);
```

### Option C: Using Helper Methods

```java
// Full row (all 9 columns)
Set<Integer> topRow = DepositSlotMapper.fullRowToSlots(0);

// Single row from column 1-7
Set<Integer> middleRow = DepositSlotMapper.rowToSlots(1, 1, 7);

// Single column
Set<Integer> leftColumn = DepositSlotMapper.columnToSlots(0, 0, 3);

// Individual coordinates
Set<Integer> corners = DepositSlotMapper.coordinatesToSlots(
    0, 0,  // Top-left
    8, 0,  // Top-right
    0, 2,  // Bottom-left
    8, 2   // Bottom-right
);
```

### Option D: Traditional Manual Slots (Still Works)

```java
// The old way - still works fine
Set<Integer> backpackSlots = Set.of(
    0, 1, 2, 3, 4, 5, 6, 7, 8,       // Row 1
    9, 10, 11, 12, 13, 14, 15, 16, 17, 18,  // Row 2
    19, 20, 21, 22, 23, 24, 25, 26   // Row 3
);

DepositPolicy policy = DepositPolicies.slots(
    backpackSlots,
    DepositReturnStrategy.INVENTORY_THEN_DROP
);
```

---

### Step 1: Define Deposit Slots

```java
// Using coordinates (recommended) - 3 rows of 9 columns each
Set<Integer> backpackSlots = DepositSlotMapper.areaToSlots(0, 0, 9, 3);

// Create DepositPolicy
DepositPolicy policy = DepositPolicies.slots(
    backpackSlots,
    DepositReturnStrategy.INVENTORY_THEN_DROP
);
```

### Step 2: Implement DepositHandler

```java
public class BackpackDepositHandler implements DepositHandler {

    @Override
    public DepositDecision onPlace(MenuKey menuKey, ViewerRef viewer, int slot, 
                                   MenuItem offered, InteractionContext interaction) {
        // Optional: Define blocked items
        Material type = offered.stack().getType();
        if (type == Material.BEDROCK || type == Material.BARRIER) {
            return DepositDecision.deny(); // Item not allowed
        }
        
        // Accept item (without changes)
        return DepositDecision.accept();
        
        // Or: Transform item
        // ItemStack transformed = offered.stack().clone();
        // transformed.editMeta(meta -> meta.setDisplayName("§7Backpack Item"));
        // return DepositDecision.acceptTransformed(MenuItem.of(transformed));
    }

    @Override
    public RemovalDecision onRemove(MenuKey menuKey, ViewerRef viewer, int slot, 
                                    MenuItem current, InteractionContext interaction) {
        // Player can take items from backpack
        return RemovalDecision.allow();
    }
}
```

### Step 3: Menu Definition with Deposits

```java
public class BackpackMenuBuilder {
    
    public MenuDefinition createBackpackMenu(BukkitMenuService menuService) {
        // Use coordinates: 3x3 area at position (0, 0)
        Set<Integer> backpackSlots = DepositSlotMapper.areaToSlots(0, 0, 9, 3);
        
        DepositPolicy policy = DepositPolicies.slots(
            backpackSlots,
            DepositReturnStrategy.INVENTORY_THEN_DROP
        );
        
        DepositHandler handler = new BackpackDepositHandler();
        
        return MenuDefinition.builder("backpack")
            .title("§8Backpack")
            .size(27) // 3 rows
            
            // ✨ Activate Deposit System
            .interactionPolicy(InteractionPolicies.deposits(policy))
            .depositHandler(handler)
            
            // Populator: Load initial items
            .populator(ctx -> {
                // Load saved items from DB
                UUID playerId = ctx.viewer().getUniqueId();
                List<BackpackItemData> saved = loadBackpackFromDatabase(playerId);
                
                for (BackpackItemData data : saved) {
                    MenuItem item = MenuItem.of(data.itemStack());
                    ctx.set(data.slot(), item);
                }
            })
            
            .build();
    }
}
```

### Step 4: Save Items on Close

```java
public class BackpackCloseListener {
    
    private final Database database;
    
    @EventHandler
    public void onMenuClose(MenuCloseEvent event) {
        if (!event.menu().key().id().equals("backpack")) return;
        
        BukkitMenuView view = event.view();
        UUID playerId = view.viewer().getUniqueId();
        
        // ✨ Read items from DepositLedger
        Optional<DepositLedger> ledgerOpt = view.depositLedger();
        if (ledgerOpt.isEmpty()) return;
        
        DepositLedger ledger = ledgerOpt.get();
        DepositLedgerSnapshot snapshot = ledger.snapshot();
        
        // Save to database
        for (DepositEntry entry : snapshot.entries()) {
            int slot = entry.slot();
            MenuItem item = entry.item();
            ItemStack stack = item.stack();
            
            database.saveBackpackItem(playerId, slot, stack);
        }
        
        Bukkit.getLogger().info(String.format(
            "✅ Backpack of %s saved (%d items)",
            view.viewer().getName(),
            snapshot.entries().size()
        ));
    }
}
```

---

## 📚 Advanced Scenarios

### Scenario 0: Complex Deposit Layouts with Coordinates

```java
// Example: Backpack with separate sections using coordinates

// Main inventory area: 5 rows x 9 columns
Set<Integer> mainArea = DepositSlotMapper.areaToSlots(0, 0, 9, 5);

// Create a 3x3 grid for special items (top-right)
Set<Integer> specialArea = DepositSlotMapper.areaToSlots(6, 0, 3, 3);

// Combine both areas
Set<Integer> allDepositSlots = new HashSet<>(mainArea);
allDepositSlots.addAll(specialArea);

DepositPolicy policy = DepositPolicies.slots(
    allDepositSlots,
    DepositReturnStrategy.INVENTORY_THEN_DROP
);
```

### Scenario 1: Item Limit per Slot

```java
@Override
public DepositDecision onPlace(MenuKey menuKey, ViewerRef viewer, int slot, 
                               MenuItem offered, InteractionContext interaction) {
    // Only 1 stack per slot allowed
    if (offered.stack().getAmount() > 64) {
        return DepositDecision.deny();
    }
    
    return DepositDecision.accept();
}
```

### Scenario 2: Only Allow Certain Materials

```java
private static final Set<Material> ALLOWED_MATERIALS = Set.of(
    Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT,
    Material.IRON_INGOT, Material.COPPER_INGOT
);

@Override
public DepositDecision onPlace(MenuKey menuKey, ViewerRef viewer, int slot, 
                               MenuItem offered, InteractionContext interaction) {
    if (!ALLOWED_MATERIALS.contains(offered.stack().getType())) {
        return DepositDecision.deny();
    }
    
    return DepositDecision.accept();
}
```

### Scenario 3: Track Items with Metadata

```java
@Override
public DepositDecision onPlace(MenuKey menuKey, ViewerRef viewer, int slot, 
                               MenuItem offered, InteractionContext interaction) {
    ItemStack stack = offered.stack().clone();
    
    // Add metadata for serialization
    stack.editMeta(meta -> {
        meta.setCustomModelData(12345);
        meta.setDisplayName("§6Backpack Item");
        meta.setLore(List.of(
            "§7Slot: " + slot,
            "§7Player: " + viewer.getName()
        ));
    });
    
    return DepositDecision.acceptTransformed(MenuItem.of(stack));
}
```

### Scenario 4: Size-based Slots

```java
@Override
public DepositDecision onPlace(MenuKey menuKey, ViewerRef viewer, int slot, 
                               MenuItem offered, InteractionContext interaction) {
    Material type = offered.stack().getType();
    
    // Large items: max 2 stacks
    if (type == Material.DIAMOND_BLOCK) {
        if (offered.stack().getAmount() > 2 * 64) {
            return DepositDecision.deny();
        }
    }
    
    return DepositDecision.accept();
}
```

---

## 🔄 Database Integration (Example)

```java
public class BackpackRepository {
    
    private final Database db;
    
    public void saveBackpackItems(UUID playerId, DepositLedgerSnapshot snapshot) {
        // Delete old items
        db.executeUpdate(
            "DELETE FROM backpack_items WHERE player_id = ?",
            playerId
        );
        
        // Save new items
        for (DepositEntry entry : snapshot.entries()) {
            byte[] itemData = serializeItemStack(entry.item().stack());
            
            db.executeUpdate(
                "INSERT INTO backpack_items (player_id, slot, item_data) VALUES (?, ?, ?)",
                playerId,
                entry.slot(),
                itemData
            );
        }
    }
    
    public List<BackpackItemData> loadBackpackItems(UUID playerId) {
        return db.query(
            "SELECT slot, item_data FROM backpack_items WHERE player_id = ?",
            playerId,
            rs -> {
                ItemStack stack = deserializeItemStack(rs.getBytes("item_data"));
                return new BackpackItemData(rs.getInt("slot"), stack);
            }
        );
    }
    
    private byte[] serializeItemStack(ItemStack stack) {
        // Use Bukkit serialization (e.g., ConfigurationSection)
        return null; // Implementation
    }
    
    private ItemStack deserializeItemStack(byte[] data) {
        // Deserialization
        return null; // Implementation
    }
}

record BackpackItemData(int slot, ItemStack itemStack) {}
```

---

## 🎯 Coordinate System Explained

The `DepositSlotMapper` uses a **grid-based coordinate system** similar to the Page system:

```
X → (column: 0-8)
Y (row: 0-∞)

Grid Layout (9 columns x 6 rows max):
┌─┬─┬─┬─┬─┬─┬─┬─┬─┐
│0│1│2│3│4│5│6│7│8│  Y=0 (Row 0)
├─┼─┼─┼─┼─┼─┼─┼─┼─┤
│9│10│11│12│13│14│15│16│17│  Y=1 (Row 1)
├─┼─┼─┼─┼─┼─┼─┼─┼─┤
│18│19│20│21│22│23│24│25│26│  Y=2 (Row 2)
└─┴─┴─┴─┴─┴─┴─┴─┴─┘
```

### Examples

**3x3 Area at top-left (0,0):**
```java
DepositSlotMapper.areaToSlots(0, 0, 3, 3);
// Slots: 0,1,2, 9,10,11, 18,19,20
```

**Single Row (Row 1, from column 2 to 7):**
```java
DepositSlotMapper.rowToSlots(2, 1, 5);
// Slots: 11,12,13,14,15
```

**Four Corners:**
```java
DepositSlotMapper.coordinatesToSlots(
    0, 0,    // Top-left
    8, 0,    // Top-right
    0, 2,    // Bottom-left
    8, 2     // Bottom-right
);
// Slots: 0, 8, 18, 26
```

### INVENTORY_THEN_DROP (Default)

```java
DepositPolicy policy = DepositPolicies.slots(
    backpackSlots,
    DepositReturnStrategy.INVENTORY_THEN_DROP  // ← Default
);
```

**Behavior:** When menu closes:
1. Return items to player inventory
2. If inventory is full → Drop items at player

### DROP_ONLY

```java
DepositPolicy policy = DepositPolicies.slots(
    backpackSlots,
    DepositReturnStrategy.DROP_ONLY
);
```

**Behavior:** Items are always dropped at the player (even if space in inventory)

---

## ⚙️ Interactions in Detail

### Supported Interactions

```java
public enum InteractionAction {
    LEFT_CLICK,          // Normal click
    RIGHT_CLICK,         // Right click
    SHIFT_LEFT_CLICK,    // Shift + left
    SHIFT_RIGHT_CLICK,   // Shift + right
    NUMBER_KEY_SWAP,     // Hotbar 0-8
    OFFHAND_SWAP,        // F key (Offhand)
    DRAG,                // Drag operation
    // ...
}
```

All of these are **automatically** handled by the Deposit System! ✨

---

## 🐛 Debugging

### Print DepositLedger Contents

```java
DepositLedger ledger = view.depositLedger().get();
DepositLedgerSnapshot snapshot = ledger.snapshot();

System.out.println("=== Backpack Contents ===");
for (DepositEntry entry : snapshot.entries()) {
    System.out.printf("Slot %d: %s x%d%n",
        entry.slot(),
        entry.item().stack().getType(),
        entry.item().stack().getAmount()
    );
}
```

### Test Policy

```java
DepositPolicy policy = DepositPolicies.slots(Set.of(0, 1, 2, 3, 4));

System.out.println("Slot 0 allowed? " + policy.isSlotAllowed(0));  // true
System.out.println("Slot 5 allowed? " + policy.isSlotAllowed(5));  // false
```

---

## 📋 Checklist

- ✅ Create `DepositPolicy` with allowed slots
- ✅ Implement `DepositHandler` for validation
- ✅ Set `InteractionPolicy.deposits()`
- ✅ Set `.depositHandler()` in MenuDefinition
- ✅ Read `DepositLedger` in close listener
- ✅ Save items to database
- ✅ Load items when opening menu

---

## 🔗 Important Classes

| Class | Purpose |
|-------|---------|
| `DepositPolicy` | Defines allowed slots |
| `DepositHandler` | Validates items |
| `DepositLedger` | Tracks deposited items |
| `DepositEntry` | One item in a slot |
| `DepositReturnStrategy` | What happens on close |
| `InteractionPolicies` | Factory for policies |
| `DepositPolicies` | Factory for DepositPolicy |
| `DepositSlotMapper` | Convert coordinates to slots |

---

## 💡 Pro-Tips

1. **Save items regularly** - Use a tick-based scheduler to save items every 5 minutes
2. **Validate on reload** - When server restarts, validate that no items are lost
3. **Use custom data** - Save slot + metadata together to track complex data
4. **Test edge cases** - Server crash while menu is open → Are items lost?
5. **Set limits** - Use `onPlace()` to enforce a backpack size limit

---

## 📞 Support

Questions? Check these files:
- `MenuInventoryListeners.java` - Event processing
- `DepositHandler.java` - Interface documentation
- `DepositLedger.java` - Item tracking documentation
















