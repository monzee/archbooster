package ph.codeia.archbooster.experiments.typeclass;

/*
 * This file is a part of the arch-booster project.
 */

import ph.codeia.archbooster.experiments.Higher;
import ph.codeia.archbooster.function.Function0;
import ph.codeia.archbooster.function.Function1;


public interface Functor<F> {
    <T, U> Higher<F, U> map(
            Higher<F, T> source,
            Function1<? super T, ? extends U> f
    );
}