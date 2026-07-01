package mining;

import cartago.Artifact;
import cartago.OPERATION;
import jason.asSyntax.Atom;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

public class RadioArtifact extends Artifact {

    private static final int NUM_MINERS = 4;

    private int range = 25;
    private int msgId = 0;

    @OPERATION
    void init(int radioRange) {
        range = radioRange;
        defineObsProperty("radio_range", range);

        for (int receiverId = 0; receiverId < NUM_MINERS; receiverId++) {
            defineObsProperty("radio_ore_" + receiverId, -1, new Atom("none"), -1, -1, 0, 0);
        }

        defineObsProperty("signal", -1, -1, 0);
    }

    @OPERATION
    void registerTeam(int minerId, String team) {
        // Compatibility with agents that may register teams. The default radio
        // broadcasts to every miner in range, regardless of team.
    }

    @OPERATION
    void checkSignal(int senderId, int receiverId) {
        GridWorldModel model = getActiveModel();

        Location senderPos = model.getAgPos(senderId);
        Location receiverPos = model.getAgPos(receiverId);

        int distance = senderPos.distance(receiverPos);

        getObsProperty("signal").updateValues(senderId, receiverId, distance <= range ? 1 : 0);
    }

    @OPERATION
    void broadcastGold(int senderId, int goldX, int goldY) {
        broadcastOre(senderId, "gold", goldX, goldY, 3);
    }

    @OPERATION
    void broadcastOre(int senderId, String oreType, int oreX, int oreY, int oreValue) {
        GridWorldModel model = getActiveModel();
        Location senderPos = model.getAgPos(senderId);

        for (int receiverId = 0; receiverId < NUM_MINERS; receiverId++) {
            if (receiverId == senderId) continue;

            Location receiverPos = model.getAgPos(receiverId);
            int distance = senderPos.distance(receiverPos);

            if (distance <= range) {
                msgId++;
                getObsProperty("radio_ore_" + receiverId)
                    .updateValues(senderId, new Atom(oreType), oreX, oreY, oreValue, msgId);
            }
        }
    }

    private GridWorldModel getActiveModel() {
        GridWorldModel model = WorldModel.get();
        if (model == null) {
            model = WorldModelA.get();
        }
        if (model == null) {
            model = WorldModelB.get();
        }
        return model;
    }
}
