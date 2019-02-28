package ph.codeia.archbooster.experiments.transform;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.Emitter;
import ph.codeia.archbooster.Operation;

public class Memoize<T, U> implements Operation<T, U> {
    private T left;
    private U right;
    private volatile boolean hasLeft;
    private volatile boolean hasRight;
    private final Operation<T, U> source;

    public Memoize(Operation<T, U> source) {
        this.source = source;
    }

    @Override
    public void run(Emitter<? super T> states, Emitter<? super U> events) {
        if (hasLeft) {
            states.emit(left);
        }
        else if (hasRight) {
            events.emit(right);
        }
        else {
            source.run(t -> {
                left = t;
                hasLeft = true;
                states.emit(t);
            }, u -> {
                right = u;
                hasRight = true;
                events.emit(u);
            });
        }
    }
}
