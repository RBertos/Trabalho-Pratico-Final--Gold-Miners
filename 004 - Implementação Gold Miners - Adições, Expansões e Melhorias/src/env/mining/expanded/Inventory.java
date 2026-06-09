package mining.expanded;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Inventory {
    private final EnumSet<EquipmentType> equipment = EnumSet.noneOf(EquipmentType.class);
    private int carriedGold;

    public boolean has(EquipmentType type) {
        return equipment.contains(type);
    }

    public boolean equip(EquipmentType type) {
        if (has(type) || usedSlots() + type.slots() > Config.BASE_EQUIPMENT_SLOTS) {
            return false;
        }
        return equipment.add(type);
    }

    public boolean unequip(EquipmentType type) {
        return equipment.remove(type);
    }

    public int usedSlots() {
        int used = 0;
        for (EquipmentType type : equipment) {
            used += type.slots();
        }
        return used;
    }

    public int freeSlots() {
        return Config.BASE_EQUIPMENT_SLOTS - usedSlots();
    }

    public int goldCapacity() {
        int capacity = Config.BASE_GOLD_CAPACITY;
        if (has(EquipmentType.BACKPACK)) {
            capacity += Config.BACKPACK_GOLD_BONUS;
        }
        if (has(EquipmentType.CART)) {
            capacity += Config.CART_GOLD_BONUS;
        }
        return capacity;
    }

    public int viewRadius() {
        return has(EquipmentType.LANTERN) ? Config.LANTERN_VIEW_RADIUS : Config.BASE_VIEW_RADIUS;
    }

    public int moveCost() {
        return has(EquipmentType.CART) ? Config.CART_MOVE_COST : Config.MOVE_COST;
    }

    public int carriedGold() {
        return carriedGold;
    }

    public boolean canCarryMoreGold() {
        return carriedGold < goldCapacity();
    }

    public void addGold() {
        if (!canCarryMoreGold()) {
            throw new IllegalStateException("Gold capacity exceeded");
        }
        carriedGold++;
    }

    public int dropAllGold() {
        int dropped = carriedGold;
        carriedGold = 0;
        return dropped;
    }

    public Set<EquipmentType> equipment() {
        return EnumSet.copyOf(equipment);
    }

    public String equipmentSummary() {
        if (equipment.isEmpty()) {
            return "-";
        }
        return equipment.stream().map(EquipmentType::atom).sorted().collect(Collectors.joining(","));
    }
}
