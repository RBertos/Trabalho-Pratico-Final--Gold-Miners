package jia_ext;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;
import jason.environment.grid.Location;

public class dist extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) {
        try {
            int x1 = (int) ((NumberTerm) terms[0]).solve();
            int y1 = (int) ((NumberTerm) terms[1]).solve();
            int x2 = (int) ((NumberTerm) terms[2]).solve();
            int y2 = (int) ((NumberTerm) terms[3]).solve();
            int dist = new Location(x1, y1).distance(new Location(x2, y2));
            return un.unifies(terms[4], new NumberTermImpl(dist));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
