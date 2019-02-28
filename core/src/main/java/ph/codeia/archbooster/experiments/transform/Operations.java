package ph.codeia.archbooster.experiments.transform;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.Operation;

public final class Operations {
    private Operations() {}

    public interface LeftComposer<T, U, R> {
        Operation<U, R> fromLeft(Operation<T, R> source);
    }

    public interface RightComposer<T, U, L> {
        Operation<L, U> fromRight(Operation<L, T> source);
    }

    public interface Composer<T, U>
            extends LeftComposer<T, T, U>, RightComposer<U, U, T>
    {
        Operation<T, U> from(Operation<T, U> source);

        @Override
        default Operation<T, U> fromLeft(Operation<T, U> source) {
            return from(source);
        }

        @Override
        default Operation<T, U> fromRight(Operation<T, U> source) {
            return from(source);
        }
    }
}
