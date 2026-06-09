package jia_ext;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.Atom;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;
import mining.expanded.ExpandedWorldModel;

public class get_direction extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) {
        try {
            int fromX = (int) ((NumberTerm) terms[0]).solve();
            int fromY = (int) ((NumberTerm) terms[1]).solve();
            int toX = (int) ((NumberTerm) terms[2]).solve();
            int toY = (int) ((NumberTerm) terms[3]).solve();
            String action = nextAction(fromX, fromY, toX, toY);
            return un.unifies(terms[4], new Atom(action));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private String nextAction(int fromX, int fromY, int toX, int toY) {
        ExpandedWorldModel model = ExpandedWorldModel.get();
        if (model == null || !model.inGrid(toX, toY)) {
            return "skip";
        }
        Location start = new Location(fromX, fromY);
        Location target = new Location(toX, toY);
        if (start.equals(target)) {
            return "skip";
        }

        Queue<Location> open = new ArrayDeque<>();
        Map<Location, Step> previous = new HashMap<>();
        open.add(start);
        previous.put(start, new Step(null, "skip"));

        int[][] deltas = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        String[] actions = {"left", "right", "up", "down"};

        while (!open.isEmpty()) {
            Location current = open.remove();
            if (current.equals(target)) {
                break;
            }
            for (int i = 0; i < deltas.length; i++) {
                Location next = new Location(current.x + deltas[i][0], current.y + deltas[i][1]);
                if (!model.inGrid(next.x, next.y) || previous.containsKey(next)) {
                    continue;
                }
                if (model.hasObject(ExpandedWorldModel.OBSTACLE, next.x, next.y)) {
                    continue;
                }
                previous.put(next, new Step(current, actions[i]));
                open.add(next);
            }
        }

        if (!previous.containsKey(target)) {
            return "skip";
        }

        Location cursor = target;
        Step step = previous.get(cursor);
        while (step != null && step.previous != null && !step.previous.equals(start)) {
            cursor = step.previous;
            step = previous.get(cursor);
        }
        return step == null ? "skip" : step.action;
    }

    private static class Step {
        private final Location previous;
        private final String action;

        private Step(Location previous, String action) {
            this.previous = previous;
            this.action = action;
        }
    }
}
