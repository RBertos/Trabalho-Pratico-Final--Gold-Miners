package mining;

import java.util.HashMap;
import java.util.Map;

import cartago.Artifact;
import cartago.OPERATION;

public class BackpackArtifact extends Artifact {

    private static final Map<Integer,Integer> capacities = new HashMap<Integer,Integer>();

    private int capacity = 3;

    @OPERATION
    void init(int backpackCapacity) {
        capacity = backpackCapacity;
        defineObsProperty("backpack_max", capacity);
    }

    @OPERATION
    void takeBackpack(int minerId) {
        capacities.put(minerId, capacity);
    }

    public static int getCapacity(int minerId) {
        Integer minerCapacity = capacities.get(minerId);
        return minerCapacity == null ? 1 : minerCapacity;
    }

    public static void clearBackpacks() {
        capacities.clear();
    }
}
