package mining;

import cartago.Artifact;
import cartago.INTERNAL_OPERATION;
import cartago.OPERATION;

public class ClockArtifact extends Artifact {

    private int remaining;

    @OPERATION
    void init(int duration) {
        remaining = duration;
        defineObsProperty("time_left", remaining);
        execInternalOp("countdown");
    }

    @INTERNAL_OPERATION
    void countdown() {
        while (remaining > 0) {
            await_time(1000);
            remaining--;
            updateTimeLeft();
        }

        defineObsProperty("time_over");
    }

    private void updateTimeLeft() {
        try {
            removeObsProperty("time_left");
        } catch (IllegalArgumentException e) {}
        defineObsProperty("time_left", remaining);
    }
}
