package jia_ext;

import jason.JasonException;
import jason.asSemantics.DefaultInternalAction;
import jason.asSemantics.TransitionSystem;
import jason.asSemantics.Unifier;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Term;

import java.util.Iterator;
import java.util.Random;

public class random extends DefaultInternalAction {
    private final Random random = new Random();

    @Override
    public Object execute(final TransitionSystem ts, final Unifier un, final Term[] args) throws Exception {
        try {
            if (!args[0].isVar()) {
                throw new JasonException("The first argument of jia_ext.random must be a variable.");
            }
            if (!args[1].isNumeric()) {
                throw new JasonException("The second argument of jia_ext.random must be numeric.");
            }
            final int max = Math.max(1, (int) ((NumberTerm) args[1]).solve());
            final int maxIter = args.length < 3 ? Integer.MAX_VALUE : (int) ((NumberTerm) args[2]).solve();

            return new Iterator<Unifier>() {
                private int i;

                public boolean hasNext() {
                    return i < maxIter && ts.getUserAgArch().isRunning();
                }

                public Unifier next() {
                    i++;
                    Unifier copy = un.clone();
                    copy.unifies(args[0], new NumberTermImpl(random.nextInt(max)));
                    return copy;
                }

                public void remove() {
                }
            };
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new JasonException("The internal action jia_ext.random has not received enough arguments.");
        }
    }
}
