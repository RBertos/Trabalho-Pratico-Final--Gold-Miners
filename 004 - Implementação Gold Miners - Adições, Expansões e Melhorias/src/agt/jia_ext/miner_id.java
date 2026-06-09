package jia_ext;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

public class miner_id extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) {
        String name = ts.getUserAgArch().getAgName();
        int id = 999;
        if (name != null && name.startsWith("miner")) {
            try {
                id = Integer.parseInt(name.substring("miner".length()));
            } catch (NumberFormatException e) {
                id = 999;
            }
        }
        return un.unifies(terms[0], new NumberTermImpl(id));
    }
}
