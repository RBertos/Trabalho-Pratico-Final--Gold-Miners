package mining;

import cartago.Artifact;
import cartago.OPERATION;
import jason.asSyntax.Atom;
import jason.environment.grid.GridWorldModel;
import jason.environment.grid.Location;

import java.util.HashMap;
import java.util.Map;

public class RadioArtifactA extends Artifact {

    private static final int NUM_MINERS = 9;

    private int range = 25;
    private int msgId = 0;
    private Map<Integer,String> minerTeams = new HashMap<Integer,String>();

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
        minerTeams.put(minerId, team);
    }

    @OPERATION
    void checkSignal(int senderId, int receiverId) {
        GridWorldModel model = getActiveModel();

        Location senderPos = model.getAgPos(senderId);
        Location receiverPos = model.getAgPos(receiverId);

        int distance = senderPos.distance(receiverPos);

        getObsProperty("signal").updateValues(senderId, receiverId, distance <= range && isSameTeam(senderId, receiverId) ? 1 : 0);
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

            if (distance <= range && isSameTeam(senderId, receiverId)) {
                msgId++;
                getObsProperty("radio_ore_" + receiverId)
                    .updateValues(senderId, new Atom(oreType), oreX, oreY, oreValue, msgId);
            }
        }
    }

    private boolean isSameTeam(int senderId, int receiverId) {
        String senderTeam = minerTeams.get(senderId);
        String receiverTeam = minerTeams.get(receiverId);
        return senderTeam != null && senderTeam.equals(receiverTeam);
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
