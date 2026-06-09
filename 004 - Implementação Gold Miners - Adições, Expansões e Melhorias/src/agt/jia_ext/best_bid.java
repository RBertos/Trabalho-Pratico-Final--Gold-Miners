package jia_ext;

import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

public class best_bid extends DefaultInternalAction {
    @Override
    public Object execute(TransitionSystem ts, Unifier un, Term[] terms) {
        try {
            int winner = 1;
            int bestDistance = Integer.MAX_VALUE;
            for (int i = 0; i < 4; i++) {
                int distance = (int) ((NumberTerm) terms[i]).solve();
                int minerId = i + 1;
                if (distance < bestDistance || (distance == bestDistance && minerId < winner)) {
                    bestDistance = distance;
                    winner = minerId;
                }
            }
            return un.unifies(terms[4], new NumberTermImpl(winner));
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
