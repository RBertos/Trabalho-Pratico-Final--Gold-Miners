package jia_ext;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class neighbour extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) {
        try {
            int x1 = (int) ((NumberTerm) terms[0]).solve();
            int y1 = (int) ((NumberTerm) terms[1]).solve();
            int x2 = (int) ((NumberTerm) terms[2]).solve();
            int y2 = (int) ((NumberTerm) terms[3]).solve();
            return new Location(x1, y1).isNeigbour(new Location(x2, y2));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
