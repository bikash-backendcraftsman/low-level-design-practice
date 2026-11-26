# Why `List<ParkingSlot>` in Parking Lot Design?

A parking lot is not one slot. It has many slots, often across floors. The design needs a collection that can hold those `ParkingSlot` objects and support the operations the lot actually performs.

```text
ParkingLot
   ├── Floor 1 → [Slot1, Slot2, Slot3, ...]
   ├── Floor 2 → [Slot4, Slot5, Slot6, ...]
   └── Floor 3 → [Slot7, Slot8, Slot9, ...]
```

The current classes use the name `ParkingSpot`. The collection question is the same for `ParkingSlot`.

---

## 1. Why a collection at all?

One field of type `ParkingSlot` can store one slot. The lot grows, slots are added per floor, and the display board has to show all of them. A collection is the structure that holds many slots and lets the lot walk, filter, and count them.

---

## 2. Why `List` specifically?

| Operation | Why `List` fits |
| --- | --- |
| Find a free slot | Walk the slots from first to last. A list gives sequential access. |
| Add new slots | The lot grows. `ArrayList` resizes as slots are added. |
| Same slot label on two floors | Slot `A-1` on Floor 1 and slot `A-1` on Floor 2 are different objects. A list allows that. A `Set` would collapse equal elements. |
| Order matters | Parking often fills slot 1, then 2, then 3. A list keeps insertion order. |
| Filter by type | "All compact slots" is `stream().filter(...)` over the list. |

`Set` fails the duplicate and "first free slot in order" requirements. A raw array has a fixed length, so adding a floor means allocating a new array and copying. `List` is the simple structure that matches these operations.

---

## 3. Code example

```java
public class ParkingLot {
    private String name;
    private List<ParkingSlot> parkingSlots;

    public ParkingLot(String name, int capacity) {
        this.name = name;
        this.parkingSlots = new ArrayList<>();
        initializeSlots(capacity);
    }

    // First free slot of this type. Order is the list order.
    public ParkingSlot getFreeSlot(ParkingSlotType type) {
        for (ParkingSlot slot : parkingSlots) {
            if (slot.isFree() && slot.getType() == type) {
                return slot;
            }
        }
        return null;
    }

    public int getAvailableCount(ParkingSlotType type) {
        return (int) parkingSlots.stream()
                .filter(s -> s.isFree() && s.getType() == type)
                .count();
    }
}
```

`getFreeSlot` and `getAvailableCount` both scan the list. That scan is the reason the next section exists.

---

## 4. Is `List` always the best choice?

For a small lot, and for admin screens that must show every slot, yes. Finding a free slot by scanning is **O(n)**. A lot with 10,000 slots does that work on every entry.

Booking and releasing want a structure where the next free slot of a type is already waiting. A queue does that in **O(1)**. One queue per slot type means the lot does not search compact slots to park a bike.

```java
public class ParkingLot {
    // Next free slot of each type is at the front of that queue.
    private Map<ParkingSlotType, Queue<ParkingSlot>> freeSlotsByType;

    public ParkingLot() {
        freeSlotsByType = new EnumMap<>(ParkingSlotType.class);
        for (ParkingSlotType type : ParkingSlotType.values()) {
            freeSlotsByType.put(type, new ArrayDeque<>());
        }
    }

    public ParkingSlot getFreeSlot(ParkingSlotType type) {
        Queue<ParkingSlot> free = freeSlotsByType.get(type);
        if (free == null || free.isEmpty()) {
            return null;
        }
        return free.poll();
    }

    public void releaseSlot(ParkingSlot slot) {
        slot.markFree();
        freeSlotsByType.get(slot.getType()).offer(slot);
    }
}
```

| Structure | Book a free slot | Release a slot | Show every slot in order |
| --- | --- | --- | --- |
| `List<ParkingSlot>` | O(n) scan | O(1) if the slot object is already known | Natural. The list is the full picture. |
| `Map<Type, Queue<Slot>>` | O(1) `poll` | O(1) `offer` | Weak. A queue only holds free slots, and it does not remember floors in display order. |

### Hybrid used in a real design

Keep both. The list is the source of truth for display and reports. The map of queues is an index for booking.

```java
public class ParkingLot {
    private List<ParkingSlot> parkingSlots;
    private Map<ParkingSlotType, Queue<ParkingSlot>> freeSlotsByType;

    public ParkingSlot book(ParkingSlotType type) {
        Queue<ParkingSlot> free = freeSlotsByType.get(type);
        if (free == null || free.isEmpty()) {
            return null;
        }
        ParkingSlot slot = free.poll();
        slot.markOccupied();
        return slot;
    }

    public void release(ParkingSlot slot) {
        slot.markFree();
        freeSlotsByType.get(slot.getType()).offer(slot);
    }

    public List<ParkingSlot> allSlots() {
        return parkingSlots;
    }
}
```

When a slot is created it is added to `parkingSlots` and offered to the queue for its type. `book` only talks to the queue. The display board still walks `parkingSlots`. The current `DisPlayBoard` is already aimed at this split: it stores `Map<String, List<ParkingSpot>>`, a list of spots grouped by type.

`HashMap` lookup is amortized O(1). `ArrayDeque.poll` and `offer` are O(1). The extra memory is one queue entry per free slot, which is O(n) and worth it when entry rate matters more than a few extra references.

---

## 5. Summary

| Question | Answer |
| --- | --- |
| Why a collection? | The lot has many slots. |
| Why `List`? | Ordered, allows two slots with the same label, easy to scan, grows as the lot grows. |
| Is `List` optimal? | Right size for a small lot and for displaying every slot. |
| What is better for booking at scale? | `Map<ParkingSlotType, Queue<ParkingSlot>>` so taking and returning a free slot is O(1). |
| What should the interview design use? | Both. List for the full ordered set. Map of queues for free-slot lookup. |

### Interview line

Do not stop at "I used a list."

> I keep `List<ParkingSlot>` so every slot stays in order for the display board and for reports. For booking I also keep `Map<ParkingSlotType, Queue<ParkingSlot>>`. `poll` and `offer` are O(1). The list would make every entry an O(n) scan.

That sentence shows the operation first, then the structure, then the cost.

### Keywords

`List`, `ArrayList`, insertion order, sequential scan, O(n), `HashMap`, `Queue`, `ArrayDeque`, FIFO, amortized O(1), index for lookup, space-time tradeoff.
