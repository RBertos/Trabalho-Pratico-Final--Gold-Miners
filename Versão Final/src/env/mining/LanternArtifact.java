package mining;

import java.util.HashSet;
import java.util.Set;

import cartago.Artifact;
import cartago.OPERATION;

public class LanternArtifact extends Artifact {

    private static final Set<Integer> minersWithLantern = new HashSet<Integer>();

    private int visionRadius = 2;

    @OPERATION
    void init(int radius) {
        visionRadius = radius;
        defineObsProperty("lantern_radius", visionRadius);
    }

    @OPERATION
    void takeLantern(int minerId) {
        minersWithLantern.add(minerId);
    }

    public static boolean hasLantern(int minerId) {
        return minersWithLantern.contains(minerId);
    }

    public static void clearLanterns() {
        minersWithLantern.clear();
    }
}
