package mining.expanded;

public enum EquipmentType {
    LANTERN("lanterna", 1),
    CART("carrinho", 1),
    BACKPACK("mochila", 1);

    private final String atom;
    private final int slots;

    EquipmentType(String atom, int slots) {
        this.atom = atom;
        this.slots = slots;
    }

    public String atom() {
        return atom;
    }

    public int slots() {
        return slots;
    }

    public static EquipmentType fromAtom(String atom) {
        for (EquipmentType type : values()) {
            if (type.atom.equalsIgnoreCase(atom) || type.name().equalsIgnoreCase(atom)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown equipment: " + atom);
    }
}
