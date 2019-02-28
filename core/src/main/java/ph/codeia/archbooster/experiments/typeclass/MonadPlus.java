package ph.codeia.archbooster.experiments.typeclass;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.Higher;


public interface MonadPlus<M> extends Monad<M> {
    <T> Higher<M, T> zero();

    <T> Higher<M, T> plus(Higher<M, T> left, Higher<M, T> right);

    default Higher<M, Void> guard(boolean canContinue) {
        return canContinue ? unit(null) : zero();
    }
}